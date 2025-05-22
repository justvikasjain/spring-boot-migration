package main.java.com.migration.util;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for database type mapping and conversion
 */
@Slf4j
public class DatabaseDialectConverter {

    // Maps for data type conversion between different database systems
    private static final Map<String, Map<String, String>> TYPE_CONVERSION_MAP = new HashMap<>();
    
    static {
        // MySQL to other databases
        Map<String, String> mysqlToOthers = new HashMap<>();
        mysqlToOthers.put("hana.varchar", "NVARCHAR");
        mysqlToOthers.put("hana.char", "NCHAR");
        mysqlToOthers.put("hana.int", "INTEGER");
        mysqlToOthers.put("hana.bigint", "BIGINT");
        mysqlToOthers.put("hana.tinyint", "TINYINT");
        mysqlToOthers.put("hana.smallint", "SMALLINT");
        mysqlToOthers.put("hana.decimal", "DECIMAL");
        mysqlToOthers.put("hana.float", "REAL");
        mysqlToOthers.put("hana.double", "DOUBLE");
        mysqlToOthers.put("hana.datetime", "TIMESTAMP");
        mysqlToOthers.put("hana.timestamp", "TIMESTAMP");
        mysqlToOthers.put("hana.date", "DATE");
        mysqlToOthers.put("hana.time", "TIME");
        mysqlToOthers.put("hana.text", "NCLOB");
        mysqlToOthers.put("hana.longtext", "NCLOB");
        mysqlToOthers.put("hana.blob", "BLOB");
        mysqlToOthers.put("hana.boolean", "BOOLEAN");
        
        // PostgreSQL to other databases
        Map<String, String> postgresqlToOthers = new HashMap<>();
        postgresqlToOthers.put("hana.varchar", "NVARCHAR");
        postgresqlToOthers.put("hana.character varying", "NVARCHAR");
        postgresqlToOthers.put("hana.char", "NCHAR");
        postgresqlToOthers.put("hana.character", "NCHAR");
        postgresqlToOthers.put("hana.integer", "INTEGER");
        postgresqlToOthers.put("hana.int4", "INTEGER");
        postgresqlToOthers.put("hana.bigint", "BIGINT");
        postgresqlToOthers.put("hana.int8", "BIGINT");
        postgresqlToOthers.put("hana.smallint", "SMALLINT");
        postgresqlToOthers.put("hana.int2", "SMALLINT");
        postgresqlToOthers.put("hana.decimal", "DECIMAL");
        postgresqlToOthers.put("hana.numeric", "DECIMAL");
        postgresqlToOthers.put("hana.real", "REAL");
        postgresqlToOthers.put("hana.float4", "REAL");
        postgresqlToOthers.put("hana.double precision", "DOUBLE");
        postgresqlToOthers.put("hana.float8", "DOUBLE");
        postgresqlToOthers.put("hana.timestamp", "TIMESTAMP");
        postgresqlToOthers.put("hana.timestamptz", "TIMESTAMP");
        postgresqlToOthers.put("hana.date", "DATE");
        postgresqlToOthers.put("hana.time", "TIME");
        postgresqlToOthers.put("hana.timetz", "TIME");
        postgresqlToOthers.put("hana.text", "NCLOB");
        postgresqlToOthers.put("hana.bytea", "BLOB");
        postgresqlToOthers.put("hana.boolean", "BOOLEAN");
        postgresqlToOthers.put("hana.bool", "BOOLEAN");
        
        // SQL Server to other databases
        Map<String, String> sqlserverToOthers = new HashMap<>();
        sqlserverToOthers.put("hana.varchar", "NVARCHAR");
        sqlserverToOthers.put("hana.nvarchar", "NVARCHAR");
        sqlserverToOthers.put("hana.char", "NCHAR");
        sqlserverToOthers.put("hana.nchar", "NCHAR");
        sqlserverToOthers.put("hana.int", "INTEGER");
        sqlserverToOthers.put("hana.bigint", "BIGINT");
        sqlserverToOthers.put("hana.smallint", "SMALLINT");
        sqlserverToOthers.put("hana.tinyint", "TINYINT");
        sqlserverToOthers.put("hana.decimal", "DECIMAL");
        sqlserverToOthers.put("hana.numeric", "DECIMAL");
        sqlserverToOthers.put("hana.float", "DOUBLE");
        sqlserverToOthers.put("hana.real", "REAL");
        sqlserverToOthers.put("hana.datetime", "TIMESTAMP");
        sqlserverToOthers.put("hana.datetime2", "TIMESTAMP");
        sqlserverToOthers.put("hana.date", "DATE");
        sqlserverToOthers.put("hana.time", "TIME");
        sqlserverToOthers.put("hana.text", "NCLOB");
        sqlserverToOthers.put("hana.ntext", "NCLOB");
        sqlserverToOthers.put("hana.image", "BLOB");
        sqlserverToOthers.put("hana.varbinary", "BLOB");
        sqlserverToOthers.put("hana.bit", "BOOLEAN");
        
        // Oracle to other databases
        Map<String, String> oracleToOthers = new HashMap<>();
        oracleToOthers.put("hana.varchar2", "NVARCHAR");
        oracleToOthers.put("hana.nvarchar2", "NVARCHAR");
        oracleToOthers.put("hana.char", "NCHAR");
        oracleToOthers.put("hana.nchar", "NCHAR");
        oracleToOthers.put("hana.number", "DECIMAL");
        oracleToOthers.put("hana.integer", "INTEGER");
        oracleToOthers.put("hana.float", "DOUBLE");
        oracleToOthers.put("hana.binary_float", "REAL");
        oracleToOthers.put("hana.binary_double", "DOUBLE");
        oracleToOthers.put("hana.date", "TIMESTAMP");
        oracleToOthers.put("hana.timestamp", "TIMESTAMP");
        oracleToOthers.put("hana.clob", "NCLOB");
        oracleToOthers.put("hana.nclob", "NCLOB");
        oracleToOthers.put("hana.blob", "BLOB");
        oracleToOthers.put("hana.raw", "VARBINARY");
        
        // HANA to other databases
        Map<String, String> hanaToOthers = new HashMap<>();
        hanaToOthers.put("mysql.nvarchar", "VARCHAR");
        hanaToOthers.put("mysql.varchar", "VARCHAR");
        hanaToOthers.put("mysql.nchar", "CHAR");
        hanaToOthers.put("mysql.char", "CHAR");
        hanaToOthers.put("mysql.integer", "INT");
        hanaToOthers.put("mysql.bigint", "BIGINT");
        hanaToOthers.put("mysql.smallint", "SMALLINT");
        hanaToOthers.put("mysql.tinyint", "TINYINT");
        hanaToOthers.put("mysql.decimal", "DECIMAL");
        hanaToOthers.put("mysql.real", "FLOAT");
        hanaToOthers.put("mysql.double", "DOUBLE");
        hanaToOthers.put("mysql.timestamp", "DATETIME");
        hanaToOthers.put("mysql.date", "DATE");
        hanaToOthers.put("mysql.time", "TIME");
        hanaToOthers.put("mysql.nclob", "LONGTEXT");
        hanaToOthers.put("mysql.blob", "BLOB");
        hanaToOthers.put("mysql.boolean", "TINYINT(1)");
        
        // Add the maps to the main conversion map
        TYPE_CONVERSION_MAP.put("mysql", mysqlToOthers);
        TYPE_CONVERSION_MAP.put("postgresql", postgresqlToOthers);
        TYPE_CONVERSION_MAP.put("sqlserver", sqlserverToOthers);
        TYPE_CONVERSION_MAP.put("oracle", oracleToOthers);
        TYPE_CONVERSION_MAP.put("hana", hanaToOthers);
    }
    
    /**
     * Converts a data type from source database to target database
     * 
     * @param sourceDbType the source database type
     * @param targetDbType the target database type
     * @param dataType the data type to convert
     * @return the converted data type for the target database
     */
    public static String convertDataType(String sourceDbType, String targetDbType, String dataType) {
        if (sourceDbType == null || targetDbType == null || dataType == null) {
            return "VARCHAR(255)"; // Default fallback
        }
        
        sourceDbType = sourceDbType.toLowerCase();
        targetDbType = targetDbType.toLowerCase();
        String normalizedDataType = dataType.toLowerCase();
        
        // If source and target are the same, no conversion needed
        if (sourceDbType.equals(targetDbType)) {
            return dataType;
        }
        
        // Get the conversion map for the source database
        Map<String, String> conversionMap = TYPE_CONVERSION_MAP.get(sourceDbType);
        if (conversionMap == null) {
            log.warn("No conversion map found for source database: {}", sourceDbType);
            return getDefaultDataType(targetDbType, normalizedDataType);
        }
        
        // Look up the conversion for the specific target database
        String key = targetDbType + "." + normalizedDataType;
        String convertedType = conversionMap.get(key);
        
        if (convertedType != null) {
            // Handle special cases where we need to preserve length/precision
            if (normalizedDataType.contains("varchar") || normalizedDataType.contains("char")) {
                // Extract length from source type if available
                int lengthStart = dataType.indexOf('(');
                int lengthEnd = dataType.indexOf(')');
                
                if (lengthStart > 0 && lengthEnd > lengthStart) {
                    String length = dataType.substring(lengthStart, lengthEnd + 1);
                    return convertedType + length;
                } else {
                    return convertedType + "(255)"; // Default length
                }
            } else if (normalizedDataType.contains("decimal") || normalizedDataType.contains("numeric")) {
                // Extract precision and scale from source type if available
                int precisionStart = dataType.indexOf('(');
                int precisionEnd = dataType.indexOf(')');
                
                if (precisionStart > 0 && precisionEnd > precisionStart) {
                    String precision = dataType.substring(precisionStart, precisionEnd + 1);
                    return convertedType + precision;
                } else {
                    return convertedType + "(18,6)"; // Default precision and scale
                }
            }
            
            return convertedType;
        }
        
        log.warn("No specific conversion found for {} from {} to {}", dataType, sourceDbType, targetDbType);
        return getDefaultDataType(targetDbType, normalizedDataType);
    }
    
    /**
     * Gets a default data type for the target database based on the source data type
     */
    private static String getDefaultDataType(String targetDbType, String sourceDataType) {
        if (targetDbType.equals("hana")) {
            if (sourceDataType.contains("varchar") || sourceDataType.contains("char")) {
                return "NVARCHAR(255)";
            } else if (sourceDataType.contains("int")) {
                return "INTEGER";
            } else if (sourceDataType.contains("decimal") || sourceDataType.contains("numeric")) {
                return "DECIMAL(18,6)";
            } else if (sourceDataType.contains("float") || sourceDataType.contains("double")) {
                return "DOUBLE";
            } else if (sourceDataType.contains("date")) {
                return "DATE";
            } else if (sourceDataType.contains("time")) {
                if (sourceDataType.contains("timestamp")) {
                    return "TIMESTAMP";
                } else {
                    return "TIME";
                }
            } else if (sourceDataType.contains("blob") || sourceDataType.contains("binary")) {
                return "BLOB";
            } else if (sourceDataType.contains("text") || sourceDataType.contains("clob")) {
                return "NCLOB";
            } else if (sourceDataType.contains("bool")) {
                return "BOOLEAN";
            }
            return "NVARCHAR(255)";
        } else {
            // Generic fallback for other databases
            if (sourceDataType.contains("varchar") || sourceDataType.contains("char")) {
                return "VARCHAR(255)";
            } else if (sourceDataType.contains("int")) {
                return "INTEGER";
            } else if (sourceDataType.contains("decimal") || sourceDataType.contains("numeric")) {
                return "DECIMAL(18,6)";
            } else if (sourceDataType.contains("float") || sourceDataType.contains("double")) {
                return "DOUBLE";
            } else if (sourceDataType.contains("date")) {
                return "DATE";
            } else if (sourceDataType.contains("time")) {
                if (sourceDataType.contains("timestamp")) {
                    return "TIMESTAMP";
                } else {
                    return "TIME";
                }
            } else if (sourceDataType.contains("blob") || sourceDataType.contains("binary")) {
                return "BLOB";
            } else if (sourceDataType.contains("text") || sourceDataType.contains("clob")) {
                return "TEXT";
            } else if (sourceDataType.contains("bool")) {
                return "BOOLEAN";
            }
            return "VARCHAR(255)";
        }
    }
}
