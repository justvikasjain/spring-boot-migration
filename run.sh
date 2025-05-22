#!/bin/bash
# Script to build and run the Database Migration Tool

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}=== Database Migration Tool - Build and Run Script ===${NC}"

# Check Java version
echo -e "${YELLOW}Checking Java version...${NC}"
if type -p java > /dev/null; then
    _java=java
elif [[ -n "$JAVA_HOME" ]] && [[ -x "$JAVA_HOME/bin/java" ]];  then
    _java="$JAVA_HOME/bin/java"
else
    echo -e "${RED}Java not found. Please install JDK 17 or higher.${NC}"
    exit 1
fi

version=$("$_java" -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "Java version: $version"

# Check if JDK version is >= 17
if [[ "$version" < "17" ]]; then
    echo -e "${RED}Java version must be at least 17.${NC}"
    echo -e "${YELLOW}Current version: $version${NC}"
    exit 1
fi

# Build the application
echo -e "${YELLOW}Building the application...${NC}"
chmod +x ./mvnw
./mvnw clean package -DskipTests

if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed. Please check the errors above.${NC}"
    exit 1
fi

echo -e "${GREEN}Build successful!${NC}"

# Run the application
echo -e "${YELLOW}Starting the application...${NC}"
echo -e "${GREEN}The application will be available at http://localhost:8080${NC}"
java -jar target/data-migration-0.0.1-SNAPSHOT.jar

exit 0
