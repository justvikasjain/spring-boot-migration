package main.java.com.migration.service;

import com.migration.model.DatabaseConnection;
import com.migration.model.MigrationRequest;
import com.migration.model.MigrationResult;
import com.migration.model.TableInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MigrationService {

    private final DatabaseService databaseService;

    /**
     * Migrates data from source to target database based on the migration request
     */
    public MigrationResult migrateData(MigrationRequest request) {
        long startTime = System.currentTimeMillis();
        int totalRecords = 0;
        int migratedRecords = 0;
        int failedRecords = 0;
        
        try {
            // Get table structure information
            TableInfo sourceTableInfo = databaseService.getTableInfo(request.getSourceConnection(), request.getSourceTable());
            
            // Determine which columns to migrate
            List<String> columnsToMigrate = request.getSelectedColumns() != null && !request.getSelectedColumns().isEmpty() 
                    ? request.getSelectedColumns() 
                    : sourceTableInfo.getColumns().stream()
                        .map(TableInfo.ColumnInfo::getName)
                        .collect(Collectors.toList());
            
            // Create target table if requested
            if (request.isCreateTargetTable()) {
                createTargetTable(request.getTargetConnection(), request.getTargetTable(), sourceTableInfo, columnsToMigrate);
            }
            
            // Truncate target table if requested
            if (request.isTruncateTargetTable()) {
                truncateTargetTable(request.getTargetConnection(), request.getTargetTable());
            }
            
            // Start the migration
            try (Connection sourceConn = databaseService.getConnection(request.getSourceConnection());
                 Connection targetConn = databaseService.getConnection(request.getTargetConnection())) {
                
                // Disable auto-commit for batch operations
                targetConn.setAutoCommit(false);
                
                // Create the query for selecting data from source
                String selectQuery = buildSelectQuery(request.getSourceTable(), columnsToMigrate, request.getWhereClause());
                
                // Create the prepared statement for inserting into target
                String insertQuery = buildInsertQuery(request.getTargetTable(), columnsToMigrate);
                
                try (Statement sourceStmt = sourceConn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                     ResultSet rs = sourceStmt.executeQuery(selectQuery);
                     PreparedStatement targetPstmt = targetConn.prepareStatement(insertQuery)) {
                    
                    // Get metadata for the result set
                    ResultSetMetaData rsMetaData = rs.getMetaData();
                    int columnCount = rsMetaData.getColumnCount();
                    
                    // Process records in batches
                    int batchCounter = 0;
                    while (rs.next()) {
                        totalRecords++;
                        try {
                            // Set values in the prepared statement
                            for (int i = 1; i <= columnCount; i++) {
                                targetPstmt.setObject(i, rs.getObject(i));
                            }
                            targetPstmt.addBatch();
                            batchCounter++;
                            
                            // Execute batch when it reaches the batch size
                            if (batchCounter >= request.getBatchSize()) {
                                int[] results = targetPstmt.executeBatch();
                                targetConn.commit();
                                migratedRecords += Arrays.stream(results).sum();
                                batchCounter = 0;
                            }
                        } catch (SQLException e) {
                            failedRecords++;
                            log.warn("Failed to process record: {}", e.getMessage());
                        }
                    }
                    
                    // Process any remaining records in the batch
                    if (batchCounter > 0) {
                        int[] results = targetPstmt.executeBatch();
                        targetConn.commit();
                        migratedRecords += Arrays.stream(results).sum();
                    }
                }
                
                long executionTime = System.currentTimeMillis() - startTime;
                return new MigrationResult(
                        true,
                        totalRecords,
                        migratedRecords,
                        failedRecords,
                        executionTime,
                        "Migration completed successfully",
                        null
                );
            }
        } catch (Exception e) {
            log.error("Migration failed: {}", e.getMessage(), e);
            long executionTime = System.currentTimeMillis() - startTime;
            return new MigrationResult(
                    false,
                    totalRecords,
                    migratedRecords,
                    failedRecords,
                    executionTime,
                    "Migration failed: " + e.getMessage(),
                    e
            );
        }
    }

    /**
     * Creates a target table based on the source table structure
     */    private void createTargetTable(DatabaseConnection targetConnection, String targetTable, 
                                   TableInfo sourceTableInfo, List<String> columnsToMigrate) throws SQLException {
        // Filter columns to include only the selected ones
        List<TableInfo.ColumnInfo> filteredColumns = sourceTableInfo.getColumns().stream()
                .filter(col -> columnsToMigrate.contains(col.getName()))
                .collect(Collectors.toList());
        
        // Build the CREATE TABLE statement
        StringBuilder createTableSql = new StringBuilder();
        createTableSql.append("CREATE TABLE ").append(targetTable).append(" (");
        
        for (int i = 0; i < filteredColumns.size(); i++) {
            TableInfo.ColumnInfo column = filteredColumns.get(i);
            
            createTableSql.append(column.getName()).append(" ").append(
                mapDataType(column.getDataType(), targetConnection.getDbType(), 
                    request.getSourceConnection().getDbType())
            );
            
            if (!column.isNullable()) {
                createTableSql.append(" NOT NULL");
            }
            
            if (i < filteredColumns.size() - 1) {
                createTableSql.append(", ");
            }
        }
        
        // Add primary key constraint if available
        List<String> primaryKeyColumns = filteredColumns.stream()
                .filter(TableInfo.ColumnInfo::isPrimaryKey)
                .map(TableInfo.ColumnInfo::getName)
                .collect(Collectors.toList());
        
        if (!primaryKeyColumns.isEmpty()) {
            createTableSql.append(", PRIMARY KEY (")
                    .append(String.join(", ", primaryKeyColumns))
                    .append(")");
        }
        
        createTableSql.append(")");
        
        // Special handling for SAP HANA
        if ("hana".equalsIgnoreCase(targetConnection.getDbType())) {
            // Add any special HANA syntax if needed
        }
        
        // Execute the CREATE TABLE statement
        try (Connection conn = databaseService.getConnection(targetConnection);
             Statement stmt = conn.createStatement()) {
            log.info("Creating target table with SQL: {}", createTableSql);
            stmt.executeUpdate(createTableSql.toString());
            log.info("Target table created successfully");
        } catch (SQLException e) {
            log.error("Failed to create target table: {}", e.getMessage(), e);
            throw e;
        }
    }    /**
     * Maps a data type from the source database to the target database
     */
    private String mapDataType(String sourceDataType, String targetDbType, String sourceDbType) {
        // Use the DatabaseDialectConverter utility to handle data type conversion
        return com.migration.util.DatabaseDialectConverter.convertDataType(
                sourceDbType, 
                targetDbType, 
                sourceDataType);
    }

    /**
     * Truncates the target table
     */
    private void truncateTargetTable(DatabaseConnection targetConnection, String targetTable) throws SQLException {
        try (Connection conn = databaseService.getConnection(targetConnection);
             Statement stmt = conn.createStatement()) {
            String truncateSql = "TRUNCATE TABLE " + targetTable;
            log.info("Truncating target table with SQL: {}", truncateSql);
            stmt.executeUpdate(truncateSql);
            log.info("Target table truncated successfully");
        } catch (SQLException e) {
            log.error("Failed to truncate target table: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Builds the SELECT query for the source table
     */
    private String buildSelectQuery(String sourceTable, List<String> columns, String whereClause) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");
        query.append(String.join(", ", columns));
        query.append(" FROM ").append(sourceTable);
        
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            query.append(" WHERE ").append(whereClause);
        }
        
        return query.toString();
    }

    /**
     * Builds the INSERT query for the target table
     */
    private String buildInsertQuery(String targetTable, List<String> columns) {
        StringBuilder query = new StringBuilder();
        query.append("INSERT INTO ").append(targetTable).append(" (");
        query.append(String.join(", ", columns));
        query.append(") VALUES (");
        
        for (int i = 0; i < columns.size(); i++) {
            query.append("?");
            if (i < columns.size() - 1) {
                query.append(", ");
            }
        }
        
        query.append(")");
        return query.toString();
    }

    /**
     * Counts the number of records in a table
     */
    public int countRecords(DatabaseConnection connection, String table, String whereClause) {
        String countQuery = "SELECT COUNT(*) FROM " + table;
        if (whereClause != null && !whereClause.trim().isEmpty()) {
            countQuery += " WHERE " + whereClause;
        }
        
        try (Connection conn = databaseService.getConnection(connection);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countQuery)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            log.error("Failed to count records: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to count records: " + e.getMessage(), e);
        }
    }
}
