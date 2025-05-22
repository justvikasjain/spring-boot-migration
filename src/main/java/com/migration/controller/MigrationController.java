package main.java.com.migration.controller;

import com.migration.model.DatabaseConnection;
import com.migration.model.MigrationRequest;
import com.migration.model.MigrationResult;
import com.migration.model.TableInfo;
import com.migration.service.DatabaseService;
import com.migration.service.MigrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MigrationController {

    private final DatabaseService databaseService;
    private final MigrationService migrationService;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("sourceConnection", new DatabaseConnection());
        model.addAttribute("targetConnection", new DatabaseConnection());
        model.addAttribute("dbTypes", List.of("MySQL", "PostgreSQL", "SQLServer", "Oracle", "HANA"));
        return "index";
    }

    @PostMapping("/test-connection")
    @ResponseBody
    public Map<String, Object> testConnection(@RequestBody DatabaseConnection connection) {
        Map<String, Object> response = new HashMap<>();
        try {
            boolean connected = databaseService.testConnection(connection);
            response.put("success", connected);
            response.put("message", "Connection successful!");
        } catch (Exception e) {
            log.error("Connection test failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Connection failed: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/get-tables")
    @ResponseBody
    public Map<String, Object> getTables(@RequestBody DatabaseConnection connection) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<String> tables = databaseService.getAllTables(connection);
            response.put("success", true);
            response.put("tables", tables);
        } catch (Exception e) {
            log.error("Failed to get tables: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to get tables: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/get-columns")
    @ResponseBody
    public Map<String, Object> getColumns(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            DatabaseConnection connection = new DatabaseConnection();
            connection.setDbType((String) request.get("dbType"));
            connection.setHost((String) request.get("host"));
            connection.setPort(Integer.parseInt(request.get("port").toString()));
            connection.setDatabaseName((String) request.get("databaseName"));
            connection.setUsername((String) request.get("username"));
            connection.setPassword((String) request.get("password"));
            
            String tableName = (String) request.get("tableName");
            
            TableInfo tableInfo = databaseService.getTableInfo(connection, tableName);
            response.put("success", true);
            response.put("columns", tableInfo.getColumns());
        } catch (Exception e) {
            log.error("Failed to get columns: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to get columns: " + e.getMessage());
        }
        return response;
    }

    @PostMapping("/migrate")
    @ResponseBody
    public MigrationResult migrateData(@RequestBody MigrationRequest request) {
        try {
            return migrationService.migrateData(request);
        } catch (Exception e) {
            log.error("Migration failed: {}", e.getMessage(), e);
            return new MigrationResult(false, 0, 0, 0, 0, "Migration failed: " + e.getMessage(), e);
        }
    }

    @PostMapping("/count-records")
    @ResponseBody
    public Map<String, Object> countRecords(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();
        try {
            DatabaseConnection connection = new DatabaseConnection();
            connection.setDbType((String) request.get("dbType"));
            connection.setHost((String) request.get("host"));
            connection.setPort(Integer.parseInt(request.get("port").toString()));
            connection.setDatabaseName((String) request.get("databaseName"));
            connection.setUsername((String) request.get("username"));
            connection.setPassword((String) request.get("password"));
            
            String tableName = (String) request.get("tableName");
            String whereClause = (String) request.get("whereClause");
            
            int count = migrationService.countRecords(connection, tableName, whereClause);
            response.put("success", true);
            response.put("count", count);
        } catch (Exception e) {
            log.error("Failed to count records: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "Failed to count records: " + e.getMessage());
        }
        return response;
    }
}
