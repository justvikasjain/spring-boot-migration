package main.java.com.migration.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TableInfo {
    private String tableName;
    private List<ColumnInfo> columns;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ColumnInfo {
        private String name;
        private String dataType;
        private boolean isPrimaryKey;
        private boolean isNullable;
    }
}
