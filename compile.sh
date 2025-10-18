#!/bin/bash

echo "Compiling Event Management System (MySQL Only)..."
javac --release 21 *.java

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
    echo "  java -cp '.:mysql-connector-java.jar' EventManager"
    echo "  java -cp '.:mysql-connector-java.jar' EventManagerGUI"
else
    echo "Compilation failed!"
    exit 1
fi
