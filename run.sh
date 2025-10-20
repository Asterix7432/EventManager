#!/bin/bash

echo "╔════════════════════════════════════════════╗"
echo "║   Event Management System - Version 2.0   ║"
echo "║        Database-Driven Application        ║"
echo "╚════════════════════════════════════════════╝"
echo ""

JAR_DIR=".lib"
MYSQL_JAR="$JAR_DIR/mysql-connector-j.jar"

# Check if MySQL JAR exists
if [ ! -f "$MYSQL_JAR" ]; then
    echo "ERROR: MySQL Connector/J not found!"
    echo "Please run ./compile.sh first to download dependencies."
    exit 1
fi

# Check if Main.class exists
if [ ! -f "Main.class" ]; then
    echo "ERROR: Application not compiled!"
    echo "Please run ./compile.sh first."
    exit 1
fi

echo "Starting Event Management System..."
echo "Entry Point: Main.java"
echo "Database: MySQL (Required)"
echo ""

# Run the application
java -cp ".:$MYSQL_JAR" Main

if [ $? -ne 0 ]; then
    echo ""
    echo "Application exited with errors."
    echo "Please check the error messages above."
    exit 1
fi

