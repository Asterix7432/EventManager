#!/bin/bash

echo "Compiling Event Management System (MySQL Only)..."

JAR_DIR=".lib"
MYSQL_JAR="$JAR_DIR/mysql-connector-j.jar"
MYSQL_MAVEN_URL="https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.0.0/mysql-connector-j-9.0.0.jar"

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
fi

javac --release 21 -cp "$CP" *.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo ""
    echo "Available applications:"
    echo "1. Console version: java EventManager"
    echo "2. GUI version: java EventManagerGUI"
    echo ""
    echo "Prerequisites:"
    echo "  - MySQL Server running"
    echo "  - MySQL Connector/J in classpath"
    echo ""
    echo "Database Setup:"
    echo "  Run: ./setup_database.sh"
    echo ""
    echo "To run with MySQL Connector:"
    if [ -f "$MYSQL_JAR" ]; then
      echo "  java -cp '.:$MYSQL_JAR' EventManager"
      echo "  java -cp '.:$MYSQL_JAR' EventManagerGUI"
    else
      echo "  Place mysql-connector-j.jar on classpath. Example:"
      echo "  java -cp '.:path/to/mysql-connector-j-x.y.z.jar' EventManager"
      echo "  java -cp '.:path/to/mysql-connector-j-x.y.z.jar' EventManagerGUI"
    fi
else
    echo "Compilation failed!"
    exit 1
fi
