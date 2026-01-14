#!/bin/bash

# Spring Boot Startup Script - Parameters Only (No .env)
# Location: services/data-server-springboot/spring_start.sh
# Usage: ./spring_start.sh <DB_HOST> <DB_PORT> <DB_NAME> <DB_USERNAME> <DB_PASSWORD> <SERVER_PORT>
# Example: ./spring_start.sh localhost 5432 anime_db anime_user anime_pass 8080

# Check if all parameters are provided
if [ $# -ne 6 ]; then
    echo "❌ Error: Missing parameters!"
    echo ""
    echo "Usage: ./spring_start.sh <DB_HOST> <DB_PORT> <DB_NAME> <DB_USERNAME> <DB_PASSWORD> <SERVER_PORT>"
    echo ""
    echo "Example:"
    echo "  ./spring_start.sh localhost 5432 anime_db anime_user anime_pass 8080"
    echo ""
    echo "Parameters:"
    echo "  1. DB_HOST       - Database host (e.g., localhost)"
    echo "  2. DB_PORT       - Database port (e.g., 5432)"
    echo "  3. DB_NAME       - Database name (e.g., anime_db)"
    echo "  4. DB_USERNAME   - Database username (e.g., anime_user)"
    echo "  5. DB_PASSWORD   - Database password"
    echo "  6. SERVER_PORT   - Spring Boot server port (e.g., 8080)"
    exit 1
fi

# Get parameters
DB_HOST=$1
DB_PORT=$2
DB_NAME=$3
DB_USERNAME=$4
DB_PASSWORD=$5
SERVER_PORT=$6

echo "🔧 Spring Boot Configuration:"
echo "   DB_HOST: ${DB_HOST}"
echo "   DB_PORT: ${DB_PORT}"
echo "   DB_NAME: ${DB_NAME}"
echo "   DB_USERNAME: ${DB_USERNAME}"
echo "   DB_PASSWORD: ********"
echo "   SERVER_PORT: ${SERVER_PORT}"
echo ""

# Check if Gradle wrapper exists
if [ ! -f "./gradlew" ]; then
    echo "❌ Error: Gradle wrapper not found!"
    echo "   This script must be run from the Spring Boot project directory."
    echo "   Expected location: services/data-server-springboot/"
    exit 1
fi

# Build the project
echo "📦 Building Spring Boot project..."
gradle_build_output=$(./gradlew clean build -x test 2>&1)
gradle_build_status=$?

if [ $gradle_build_status -ne 0 ]; then
    echo "$gradle_build_output"
    echo ""
    echo "🛠️ Build issues detected, attempting to resolve..."
    ./gradlew clean build --refresh-dependencies -x test || {
        echo "❌ Build failed! Cannot continue."
        exit 1
    }
fi

echo "✅ Build successful!"
echo ""

# Construct database URL
DB_URL="jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}"

echo "🌐 Starting SPRING BOOT server..."
echo "   Database URL: ${DB_URL}"
echo "   Server Port: ${SERVER_PORT}"
echo ""

# Start Spring Boot with environment variables
# NO .env file is used - all values come from parameters!
SPRING_DATASOURCE_URL="${DB_URL}" \
SPRING_DATASOURCE_USERNAME="${DB_USERNAME}" \
SPRING_DATASOURCE_PASSWORD="${DB_PASSWORD}" \
SERVER_PORT="${SERVER_PORT}" \
./gradlew bootRun --quiet