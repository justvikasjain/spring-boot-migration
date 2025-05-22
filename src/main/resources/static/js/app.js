/**
 * Database Migration Tool - Application JavaScript
 */

$(document).ready(function() {
    // Common variables
    let sourceConnectionValid = false;
    let targetConnectionValid = false;
    let sourceColumns = [];
    let sourceTableInfo = null;
    
    // Event handlers for database type selection
    $('#source-db-type').on('change', function() {
        const dbType = $(this).val();
        updateDbPortAndFields('source', dbType);
    });
    
    $('#target-db-type').on('change', function() {
        const dbType = $(this).val();
        updateDbPortAndFields('target', dbType);
    });
    
    // Test connection buttons
    $('#source-test-connection').on('click', function() {
        testConnection('source');
    });
    
    $('#target-test-connection').on('click', function() {
        testConnection('target');
    });
    
    // Get tables buttons
    $('#source-get-tables').on('click', function() {
        getTables('source');
    });
    
    $('#target-get-tables').on('click', function() {
        getTables('target');
    });
    
    // Source table selection
    $('#source-tables').on('change', function() {
        const selectedTable = $(this).val();
        if (selectedTable) {
            getTableColumns(selectedTable);
            $('#target-table-name').val(selectedTable);
        } else {
            $('#source-columns-container').hide();
        }
    });
    
    // Column selection buttons
    $('#select-all-columns').on('click', function() {
        $('.column-checkbox').prop('checked', true);
    });
    
    $('#deselect-all-columns').on('click', function() {
        $('.column-checkbox').prop('checked', false);
    });
    
    // Count records button
    $('#count-records').on('click', function() {
        countRecords();
    });
    
    // Start migration button
    $('#start-migration').on('click', function() {
        startMigration();
    });
    
    /**
     * Updates port and shows/hides specific fields based on selected database type
     */
    function updateDbPortAndFields(prefix, dbType) {
        let port = 3306; // Default MySQL port
        
        switch (dbType) {
            case 'mysql':
                port = 3306;
                break;
            case 'postgresql':
                port = 5432;
                break;
            case 'sqlserver':
                port = 1433;
                break;
            case 'oracle':
                port = 1521;
                break;
            case 'hana':
                port = 30015;
                $(`#${prefix}-hana-fields`).show();
                break;
            default:
                port = 3306;
        }
        
        // Hide HANA fields for all other database types
        if (dbType !== 'hana') {
            $(`#${prefix}-hana-fields`).hide();
        }
        
        $(`#${prefix}-port`).val(port);
    }
    
    /**
     * Tests the database connection
     */
    function testConnection(prefix) {
        const connectionData = getConnectionData(prefix);
        const $button = $(`#${prefix}-test-connection`);
        const $message = $(`#${prefix}-connection-message`);
        
        // Basic validation
        if (!connectionData.dbType || !connectionData.host || !connectionData.databaseName || !connectionData.username) {
            showMessage($message, 'Please fill in all required fields', 'danger');
            return;
        }
        
        $button.prop('disabled', true).html('<span class="loading-spinner me-2"></span> Testing...');
        
        $.ajax({
            url: '/test-connection',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(connectionData),
            success: function(response) {
                if (response.success) {
                    showMessage($message, response.message, 'success');
                    $(`#${prefix}-get-tables`).prop('disabled', false);
                    
                    if (prefix === 'source') {
                        sourceConnectionValid = true;
                    } else {
                        targetConnectionValid = true;
                    }
                } else {
                    showMessage($message, response.message, 'danger');
                }
            },
            error: function(xhr) {
                showMessage($message, 'Error connecting to database: ' + xhr.responseText, 'danger');
            },
            complete: function() {
                $button.prop('disabled', false).html('<i class="fas fa-plug"></i> Test Connection');
            }
        });
    }
    
    /**
     * Gets all tables from the database
     */
    function getTables(prefix) {
        const connectionData = getConnectionData(prefix);
        const $button = $(`#${prefix}-get-tables`);
        const $select = $(`#${prefix}-tables`);
        
        $button.prop('disabled', true).html('<span class="loading-spinner me-2"></span> Loading...');
        
        $.ajax({
            url: '/get-tables',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(connectionData),
            success: function(response) {
                if (response.success) {
                    $select.empty().append('<option value="">Select a table</option>');
                    
                    if (response.tables && response.tables.length > 0) {
                        response.tables.forEach(function(table) {
                            $select.append(`<option value="${table}">${table}</option>`);
                        });
                        $select.prop('disabled', false);
                    } else {
                        $select.append('<option value="" disabled>No tables found</option>');
                    }
                } else {
                    showMessage($(`#${prefix}-connection-message`), response.message, 'danger');
                }
            },
            error: function(xhr) {
                showMessage($(`#${prefix}-connection-message`), 'Error getting tables: ' + xhr.responseText, 'danger');
            },
            complete: function() {
                $button.prop('disabled', false).html('<i class="fas fa-table"></i> Get Tables');
            }
        });
    }
    
    /**
     * Gets column information for a specific table
     */
    function getTableColumns(tableName) {
        const connectionData = getConnectionData('source');
        
        // Add table name to request
        connectionData.tableName = tableName;
        
        $.ajax({
            url: '/get-columns',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(connectionData),
            success: function(response) {
                if (response.success) {
                    sourceColumns = response.columns;
                    renderColumns(response.columns);
                    $('#source-columns-container').show();
                } else {
                    showMessage($('#source-connection-message'), response.message, 'danger');
                }
            },
            error: function(xhr) {
                showMessage($('#source-connection-message'), 'Error getting columns: ' + xhr.responseText, 'danger');
            }
        });
    }
    
    /**
     * Renders column checkboxes in the UI
     */
    function renderColumns(columns) {
        const $container = $('#source-columns');
        $container.empty();
        
        columns.forEach(function(column, index) {
            const isPrimaryKey = column.primaryKey ? ' <span class="badge bg-warning">PK</span>' : '';
            const isNullable = column.nullable ? '' : ' <span class="badge bg-secondary">NOT NULL</span>';
            
            $container.append(`
                <div class="column-item">
                    <div class="form-check">
                        <input class="form-check-input column-checkbox" type="checkbox" id="column-${index}" value="${column.name}" checked>
                        <label class="form-check-label" for="column-${index}">
                            ${column.name} <small class="text-muted">(${column.dataType})</small>${isPrimaryKey}${isNullable}
                        </label>
                    </div>
                </div>
            `);
        });
    }
    
    /**
     * Counts records in the selected table
     */
    function countRecords() {
        const connectionData = getConnectionData('source');
        const tableName = $('#source-tables').val();
        const whereClause = $('#where-clause').val();
        
        if (!tableName) {
            return;
        }
        
        const $button = $('#count-records');
        const $display = $('#record-count-display');
        
        $button.prop('disabled', true).html('<span class="loading-spinner me-2"></span> Counting...');
        $display.html('');
        
        $.ajax({
            url: '/count-records',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify({
                ...connectionData,
                tableName: tableName,
                whereClause: whereClause
            }),
            success: function(response) {
                if (response.success) {
                    $display.html(`<strong>${response.count.toLocaleString()}</strong> records found`);
                } else {
                    showMessage($('#source-connection-message'), response.message, 'danger');
                }
            },
            error: function(xhr) {
                showMessage($('#source-connection-message'), 'Error counting records: ' + xhr.responseText, 'danger');
            },
            complete: function() {
                $button.prop('disabled', false).html('<i class="fas fa-calculator"></i> Count Records');
            }
        });
    }
    
    /**
     * Starts the migration process
     */
    function startMigration() {
        if (!validateMigrationForm()) {
            return;
        }
        
        const $button = $('#start-migration');
        $button.prop('disabled', true).html('<span class="loading-spinner me-2"></span> Migrating...');
        
        // Hide previous results if any
        $('#migration-result').hide();
        
        // Show progress section
        $('#migration-progress').show();
        $('#migration-status').html('Preparing migration...');
        $('.progress-bar').css('width', '10%').attr('aria-valuenow', 10);
        
        const migrationRequest = {
            sourceConnection: getConnectionData('source'),
            targetConnection: getConnectionData('target'),
            sourceTable: $('#source-tables').val(),
            targetTable: $('#target-table-name').val(),
            selectedColumns: getSelectedColumns(),
            whereClause: $('#where-clause').val(),
            createTargetTable: $('#create-target-table').is(':checked'),
            truncateTargetTable: $('#truncate-target-table').is(':checked'),
            batchSize: parseInt($('#batch-size').val())
        };
        
        $.ajax({
            url: '/migrate',
            type: 'POST',
            contentType: 'application/json',
            data: JSON.stringify(migrationRequest),
            success: function(response) {
                // Update progress
                $('.progress-bar').css('width', '100%').attr('aria-valuenow', 100);
                $('#migration-status').html('Migration complete!');
                
                // Show results
                showMigrationResults(response);
            },
            error: function(xhr) {
                $('.progress-bar').css('width', '100%').addClass('bg-danger');
                $('#migration-status').html('Migration failed!');
                
                try {
                    const response = JSON.parse(xhr.responseText);
                    showMigrationResults({
                        success: false,
                        totalRecords: 0,
                        migratedRecords: 0,
                        failedRecords: 0,
                        executionTime: 0,
                        message: response.message || 'Migration failed!'
                    });
                } catch (e) {
                    showMigrationResults({
                        success: false,
                        totalRecords: 0,
                        migratedRecords: 0,
                        failedRecords: 0,
                        executionTime: 0,
                        message: 'Migration failed: ' + xhr.responseText
                    });
                }
            },
            complete: function() {
                $button.prop('disabled', false).html('<i class="fas fa-exchange-alt"></i> Start Migration');
            }
        });
    }
    
    /**
     * Displays migration results in the UI
     */
    function showMigrationResults(result) {
        const $resultSection = $('#migration-result');
        const $resultMessage = $('#migration-result-message');
        
        $('#total-records').text(result.totalRecords.toLocaleString());
        $('#migrated-records').text(result.migratedRecords.toLocaleString());
        $('#failed-records').text(result.failedRecords.toLocaleString());
        $('#execution-time').text((result.executionTime / 1000).toFixed(2));
        
        if (result.success) {
            $resultMessage.removeClass('alert-danger').addClass('alert-success')
                .html('<i class="fas fa-check-circle"></i> ' + result.message);
        } else {
            $resultMessage.removeClass('alert-success').addClass('alert-danger')
                .html('<i class="fas fa-exclamation-triangle"></i> ' + result.message);
        }
        
        $resultSection.show();
    }
    
    /**
     * Validates the migration form before submission
     */
    function validateMigrationForm() {
        if (!sourceConnectionValid) {
            showMessage($('#source-connection-message'), 'Please test source connection first', 'danger');
            return false;
        }
        
        if (!targetConnectionValid) {
            showMessage($('#target-connection-message'), 'Please test target connection first', 'danger');
            return false;
        }
        
        if (!$('#source-tables').val()) {
            showMessage($('#source-connection-message'), 'Please select a source table', 'danger');
            return false;
        }
        
        if (!$('#target-table-name').val()) {
            showMessage($('#target-connection-message'), 'Please enter a target table name', 'danger');
            return false;
        }
        
        if (getSelectedColumns().length === 0) {
            showMessage($('#source-connection-message'), 'Please select at least one column to migrate', 'danger');
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets the selected columns from the UI
     */
    function getSelectedColumns() {
        const selectedColumns = [];
        $('.column-checkbox:checked').each(function() {
            selectedColumns.push($(this).val());
        });
        return selectedColumns;
    }
    
    /**
     * Gets the connection data from the form
     */
    function getConnectionData(prefix) {
        const connection = {
            dbType: $(`#${prefix}-db-type`).val(),
            host: $(`#${prefix}-host`).val(),
            port: parseInt($(`#${prefix}-port`).val()),
            databaseName: $(`#${prefix}-db-name`).val(),
            username: $(`#${prefix}-username`).val(),
            password: $(`#${prefix}-password`).val()
        };
        
        // Add SAP HANA specific fields if applicable
        if (connection.dbType === 'hana') {
            connection.instanceNumber = $(`#${prefix}-instance-number`).val();
            connection.isSapHana = true;
        }
        
        return connection;
    }
    
    /**
     * Shows a message with specified type (success, danger, etc)
     */
    function showMessage($element, message, type) {
        $element.removeClass('alert-success alert-danger alert-warning')
            .addClass(`alert-${type}`)
            .html(message)
            .show();
        
        // Scroll to message
        $('html, body').animate({
            scrollTop: $element.offset().top - 100
        }, 200);
    }
});
