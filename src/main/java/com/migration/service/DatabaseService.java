package main.java.com.migration.service;

import com.migration.model.DatabaseConnection;
import com.migration.model.TableInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Service
@Slf4j
public class DatabaseService {

    /**
     * Tests the connection to the database using the provided connection details
     */
    public boolean testConnection(DatabaseConnection connection) {
        String jdbcUrl = connection.buildJdbcUrl();
        try {
            Class.forName(connection.getDriverClassName());
            try (Connection conn = getConnection(connection)) {
                log.info("Successfully connected to {} at {}", connection.getDbType(), jdbcUrl);
                return true;
            }
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Failed to connect to database: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to connect to database: " + e.getMessage(), e);
        }
    }

    /**
     * Gets a list of all tables in the database
     */
    public List<String> getAllTables(DatabaseConnection connection) {
        List<String> tables = new ArrayList<>();
        
        try (Connection conn = getConnection(connection)) {
            DatabaseMetaData metaData = conn.getMetaData();
            String catalog = connection.getDatabaseName();
            String schemaPattern = null;
            
            // For some databases like Oracle, PostgreSQL, SAP HANA, schema is important
            if ("oracle".equalsIgnoreCase(connection.getDbType()) || 
                "postgresql".equalsIgnoreCase(connection.getDbType()) ||
                "hana".equalsIgnoreCase(connection.getDbType())) {
                schemaPattern = connection.getUsername().toUpperCase(); // Default schema is usually username
            }
            
            try (ResultSet rs = metaData.getTables(catalog, schemaPattern, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    tables.add(rs.getString("TABLE_NAME"));
                }
            }
            
            // Special handling for SAP HANA
            if ("hana".equalsIgnoreCase(connection.getDbType()) && tables.isEmpty()) {
                // Try with a direct query for HANA
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT TABLE_NAME FROM SYS.TABLES WHERE SCHEMA_NAME = CURRENT_SCHEMA")) {
                    while (rs.next()) {
                        tables.add(rs.getString("TABLE_NAME"));
                    }
                }
            }
            
            return tables;
        } catch (SQLException e) {
            log.error("Failed to get tables: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get tables: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the structure of a table
     */
    public TableInfo getTableInfo(DatabaseConnection connection, String tableName) {
        try (Connection conn = getConnection(connection)) {
            DatabaseMetaData metaData = conn.getMetaData();
            List<TableInfo.ColumnInfo> columns = new ArrayList<>();
            List<String> primaryKeys = new ArrayList<>();
            
            // Get primary keys
            try (ResultSet pks = metaData.getPrimaryKeys(connection.getDatabaseName(), null, tableName)) {
                while (pks.next()) {
                    primaryKeys.add(pks.getString("COLUMN_NAME"));
                }
            }
            
            // Get column information
            try (ResultSet rs = metaData.getColumns(connection.getDatabaseName(), null, tableName, "%")) {
                while (rs.next()) {
                    String columnName = rs.getString("COLUMN_NAME");
                    String dataType = rs.getString("TYPE_NAME");
                    int nullable = rs.getInt("NULLABLE");
                    boolean isPrimaryKey = primaryKeys.contains(columnName);
                    
                    columns.add(new TableInfo.ColumnInfo(
                            columnName,
                            dataType,
                            isPrimaryKey,
                            nullable == DatabaseMetaData.columnNullable
                    ));
                }
            }
            
            // Special handling for SAP HANA to get more detailed column info
            if ("hana".equalsIgnoreCase(connection.getDbType()) && columns.isEmpty()) {
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(
                             "SELECT COLUMN_NAME, DATA_TYPE_NAME, IS_NULLABLE FROM SYS.TABLE_COLUMNS " +
                             "WHERE TABLE_NAME = '" + tableName + "' AND SCHEMA_NAME = CURRENT_SCHEMA")) {
                    while (rs.next()) {
                        String columnName = rs.getString("COLUMN_NAME");
                        String dataType = rs.getString("DATA_TYPE_NAME");
                        String isNullable = rs.getString("IS_NULLABLE");
                        
                        columns.add(new TableInfo.ColumnInfo(
                                columnName,
                                dataType,
                                primaryKeys.contains(columnName),
                                "TRUE".equalsIgnoreCase(isNullable)
                        ));
                    }
                }
            }
            
            return new TableInfo(tableName, columns);
        } catch (SQLException e) {
            log.error("Failed to get table information: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to get table information: " + e.getMessage(), e);
        }
    }

    /**
     * Establishes a connection to the database using the provided connection details
     */
    public Connection getConnection(DatabaseConnection connection) throws SQLException {
        String jdbcUrl = connection.buildJdbcUrl();
        Properties props = new Properties();
        props.setProperty("user", connection.getUsername());
        
        if (connection.getPassword() != null && !connection.getPassword().isEmpty()) {
            props.setProperty("password", connection.getPassword());
        }
        
        // Special settings for SAP HANA
        if ("hana".equalsIgnoreCase(connection.getDbType())) {
            // Add any special properties for HANA if needed
            props.setProperty("databaseName", connection.getDatabaseName());
        }
        
        return DriverManager.getConnection(jdbcUrl, props);
    }
}
