package main.java.com.migration.model;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class DatabaseConnection {

    @NotEmpty(message = "Database type is required")
    private String dbType;
    
    @NotEmpty(message = "Host is required")
    private String host;
    
    private int port;
    
    @NotEmpty(message = "Database name is required")
    private String databaseName;
    
    @NotEmpty(message = "Username is required")
    private String username;
    
    private String password;
    
    private String jdbcUrl;
    
    // Additional fields for SAP HANA
    private boolean isSapHana;
    private String instanceNumber;
    
    /**
     * Builds the JDBC URL based on the database type
     */
    public String buildJdbcUrl() {
        if (jdbcUrl != null && !jdbcUrl.isEmpty()) {
            return jdbcUrl;
        }
        
        switch (dbType.toLowerCase()) {
            case "mysql":
                return String.format("jdbc:mysql://%s:%d/%s", host, port, databaseName);
            case "postgresql":
                return String.format("jdbc:postgresql://%s:%d/%s", host, port, databaseName);
            case "sqlserver":
                return String.format("jdbc:sqlserver://%s:%d;databaseName=%s", host, port, databaseName);
            case "oracle":
                return String.format("jdbc:oracle:thin:@%s:%d:%s", host, port, databaseName);
            case "hana":
                // SAP HANA JDBC URL format
                if (instanceNumber != null && !instanceNumber.isEmpty()) {
                    return String.format("jdbc:sap://%s:%d/?databaseName=%s", host, port, databaseName);
                } else {
                    return String.format("jdbc:sap://%s:%d/?databaseName=%s", host, port, databaseName);
                }
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
    
    /**
     * Gets the driver class name based on the database type
     */
    public String getDriverClassName() {
        switch (dbType.toLowerCase()) {
            case "mysql":
                return "com.mysql.cj.jdbc.Driver";
            case "postgresql":
                return "org.postgresql.Driver";
            case "sqlserver":
                return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
            case "oracle":
                return "oracle.jdbc.OracleDriver";
            case "hana":
                return "com.sap.db.jdbc.Driver";
            default:
                throw new IllegalArgumentException("Unsupported database type: " + dbType);
        }
    }
}
