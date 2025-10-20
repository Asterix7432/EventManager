#!/bin/bash

echo "Compiling Event Management System (MySQL)..."

JAR_DIR=".lib"
MYSQL_JAR="$JAR_DIR/mysql-connector-j.jar"
MYSQL_MAVEN_URL="https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.0.0/mysql-connector-j-9.0.0.jar"

# Check if mysql-connector-j-9.4.0 directory exists and use it
if [ -d "mysql-connector-j-9.4.0" ]; then
    echo "Found mysql-connector-j-9.4.0 directory"
    # Find the JAR file in the directory
    FOUND_JAR=$(find mysql-connector-j-9.4.0 -name "mysql-connector-j-*.jar" 2>/dev/null | head -n 1)
    if [ -n "$FOUND_JAR" ]; then
        echo "Using MySQL connector from: $FOUND_JAR"
        mkdir -p "$JAR_DIR"
        cp "$FOUND_JAR" "$MYSQL_JAR"
    fi
fi

mkdir -p "$JAR_DIR"

# Attempt to fetch MySQL Connector/J if not present
if [ ! -f "$MYSQL_JAR" ]; then
  echo "Downloading MySQL Connector/J..."
  # Try curl then wget
  if command -v curl >/dev/null 2>&1; then
    curl -L -o "$MYSQL_JAR" "$MYSQL_MAVEN_URL" || true
  elif command -v wget >/dev/null 2>&1; then
    wget -O "$MYSQL_JAR" "$MYSQL_MAVEN_URL" || true
  else
    echo "Neither curl nor wget found; skipping download."
  fi
fi

CP="."
if [ -f "$MYSQL_JAR" ]; then
  CP="$CP:$MYSQL_JAR"
else
  echo "WARNING: MySQL Connector/J not found!"
  echo "Please download it manually or ensure MySQL driver is available."
fi

echo "Compiling Java files..."
javac -cp "$CP" *.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo ""
    echo "Database connection is MANDATORY for this application."
    echo ""
    echo "Prerequisites:"
    echo "  1. MySQL Server must be running"
    echo "  2. Database configured in database.properties"
    echo "  3. MySQL Connector/J in classpath"
    echo ""
    echo "To run the application:"
    if [ -f "$MYSQL_JAR" ]; then
      echo "  java -cp '.:$MYSQL_JAR' Main"
    else
      echo "  java -cp '.:/path/to/mysql-connector-j.jar' Main"
    fi
    echo ""
    echo "Database Setup:"
    echo "  Run: ./setup_database.sh"
else
    echo "Compilation failed!"
    exit 1
fi
