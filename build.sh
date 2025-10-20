#!/bin/bash

echo "╔════════════════════════════════════════════╗"
echo "║   Event Management System - Builder       ║"
echo "║        Complete Build and Setup            ║"
echo "╚════════════════════════════════════════════╝"
echo ""

# Step 1: Clean previous builds
echo "[1/4] Cleaning previous builds..."
rm -f *.class
rm -rf .lib
echo "✓ Clean complete"
echo ""

# Step 2: Setup dependencies
echo "[2/4] Setting up dependencies..."
JAR_DIR=".lib"
MYSQL_JAR="$JAR_DIR/mysql-connector-j.jar"
MYSQL_MAVEN_URL="https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.0.0/mysql-connector-j-9.0.0.jar"

mkdir -p "$JAR_DIR"

# Check if JAR already exists
if [ -f "$MYSQL_JAR" ]; then
    echo "✓ MySQL Connector already present"
else
    # Try to find JAR in mysql-connector-j-9.4.0 directory
    if [ -d "mysql-connector-j-9.4.0" ]; then
        echo "  Checking mysql-connector-j-9.4.0 directory for JAR..."
        FOUND_JAR=$(find mysql-connector-j-9.4.0 -name "mysql-connector-j-*.jar" -type f 2>/dev/null | head -n 1)
        if [ -n "$FOUND_JAR" ]; then
            echo "  Found JAR: $FOUND_JAR"
            cp "$FOUND_JAR" "$MYSQL_JAR"
            echo "✓ MySQL Connector copied"
        else
            echo "  No JAR found in directory, will download..."
        fi
    fi
    
    # Download if still not present
    if [ ! -f "$MYSQL_JAR" ]; then
        echo "  Downloading MySQL Connector/J from Maven Central..."
        if command -v curl >/dev/null 2>&1; then
            curl -L -o "$MYSQL_JAR" "$MYSQL_MAVEN_URL"
            if [ $? -eq 0 ] && [ -f "$MYSQL_JAR" ]; then
                echo "✓ MySQL Connector downloaded successfully"
            else
                echo "✗ Download failed with curl"
                rm -f "$MYSQL_JAR"
            fi
        elif command -v wget >/dev/null 2>&1; then
            wget -O "$MYSQL_JAR" "$MYSQL_MAVEN_URL"
            if [ $? -eq 0 ] && [ -f "$MYSQL_JAR" ]; then
                echo "✓ MySQL Connector downloaded successfully"
            else
                echo "✗ Download failed with wget"
                rm -f "$MYSQL_JAR"
            fi
        else
            echo "  ERROR: Neither curl nor wget found!"
            echo "  Please download MySQL Connector/J manually to $MYSQL_JAR"
            exit 1
        fi
    fi
    
    # Final check
    if [ ! -f "$MYSQL_JAR" ]; then
        echo "✗ Failed to obtain MySQL Connector JAR"
        echo "  Please download it manually from:"
        echo "  $MYSQL_MAVEN_URL"
        echo "  and place it at: $MYSQL_JAR"
        exit 1
    fi
fi
echo ""

# Step 3: Compile
echo "[3/4] Compiling Java files..."
CP="."
if [ -f "$MYSQL_JAR" ]; then
  CP="$CP:$MYSQL_JAR"
fi

javac --release 21 -cp "$CP" *.java

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful"
else
    echo "✗ Compilation failed!"
    exit 1
fi
echo ""

# Step 4: Verify
echo "[4/4] Verifying build..."
if [ -f "Main.class" ] && [ -f "EventManagerGUI.class" ] && [ -f "DatabaseConnection.class" ]; then
    echo "✓ All required classes present"
else
    echo "✗ Some classes are missing!"
    exit 1
fi
echo ""

echo "╔════════════════════════════════════════════╗"
echo "║           BUILD SUCCESSFUL!                ║"
echo "╚════════════════════════════════════════════╝"
echo ""
echo "To run the application:"
echo "  ./run.sh"
echo ""
echo "Or manually:"
if [ -f "$MYSQL_JAR" ]; then
    echo "  java -cp '.:$MYSQL_JAR' Main"
else
    echo "  java -cp '.:/path/to/mysql-connector-j.jar' Main"
fi
echo ""
echo "Prerequisites before running:"
echo "  1. MySQL Server must be running"
echo "  2. Configure database.properties with your credentials"
echo "  3. Run ./setup_database.sh (optional, creates database)"
echo ""

