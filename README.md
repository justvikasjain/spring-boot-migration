# Database Migration Tool

A Spring Boot application that facilitates database migration between different database systems, including SAP HANA.

## Features

- Connect to different database systems (MySQL, PostgreSQL, SQL Server, Oracle, SAP HANA)
- Select source tables and columns to migrate
- Filter source data using WHERE clauses
- Create target tables automatically
- Truncate target tables before migration (optional)
- Batch processing for efficient migration
- Real-time progress tracking
- Detailed migration results

## Technologies Used

- Java 17
- Spring Boot 3.1.5
- Thymeleaf (UI)
- Bootstrap 5
- jQuery
- JDBC for database connectivity
- SAP HANA JDBC driver

## Prerequisites

- Java Development Kit (JDK) 17 or newer
- Maven for building the application
- Access to source and target databases

## Installation and Setup

1. Clone the repository or download the source code
2. Build the application using Maven:
   ```
   mvn clean install
   ```
3. Run the application:
   ```
   mvn spring-boot:run
   ```
   or
   ```
   java -jar target/data-migration-0.0.1-SNAPSHOT.jar
   ```
4. Access the application at http://localhost:8080

## Usage Guide

1. **Configure Source Database**
   - Select database type
   - Enter connection details
   - Test the connection
   - Load available tables

2. **Select Source Table and Columns**
   - Choose a table from the dropdown
   - Select columns to migrate
   - Add optional WHERE clause to filter data
   - Count records to estimate migration size

3. **Configure Target Database**
   - Select database type
   - Enter connection details
   - Test the connection

4. **Configure Target Table Options**
   - Enter target table name
   - Choose whether to create the table if it doesn't exist
   - Choose whether to truncate the table before migration
   - Set batch size for processing

5. **Start Migration**
   - Click "Start Migration" button
   - Monitor progress
   - View results upon completion

## Special SAP HANA Configuration

When connecting to SAP HANA databases:

1. Select "HANA" as the database type
2. Enter host and port (default port is 30015)
3. Provide database name, username, and password
4. Optionally specify instance number

## Troubleshooting

- **Connection Issues**: Verify network connectivity, credentials, and that the database is running
- **Permission Errors**: Ensure the database user has appropriate permissions on both source and target databases
- **Data Type Conversion Errors**: Check for data type compatibility between source and target databases

## Extending the Application

The application can be extended to support:

- Additional database systems
- More advanced data transformation options
- Schema migration in addition to data migration
- Scheduled/automated migrations
- Multi-table migrations

## License

This project is licensed under the MIT License - see the LICENSE file for details.
# spring-boot-migration
