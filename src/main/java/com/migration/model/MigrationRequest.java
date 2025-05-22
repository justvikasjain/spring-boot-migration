package main.java.com.migration.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class MigrationRequest {
    private DatabaseConnection sourceConnection;
    private DatabaseConnection targetConnection;
    private String sourceTable;
    private String targetTable;
    private List<String> selectedColumns;
    private String whereClause;
    private boolean createTargetTable;
    private boolean truncateTargetTable;
    private int batchSize = 1000;
}
