package main.java.com.migration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MigrationResult {
    private boolean success;
    private int totalRecords;
    private int migratedRecords;
    private int failedRecords;
    private long executionTime;
    private String message;
    private Exception exception;
}
