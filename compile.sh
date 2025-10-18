#!/bin/bash

echo "Compiling Event Management System (MySQL/H2)"

JAR_DIR=".lib"
DEFAULT_DL_JAR="$JAR_DIR/mysql-connector-j.jar"
MYSQL_MAVEN_URL="https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/9.0.0/mysql-connector-j-9.0.0.jar"

# H2 fallback driver (for local testing without MySQL)
H2_DEFAULT_JAR="$JAR_DIR/h2.jar"
H2_MAVEN_URL="https://repo1.maven.org/maven2/com/h2database/h2/2.2.224/h2-2.2.224.jar"

mkdir -p "$JAR_DIR"

# Detect an existing MySQL JDBC driver JAR in the repo (preferred)
detect_repo_mysql_jar() {
  for pat in "mysql-connector-j*.jar" "mysql-connector-java*.jar" "mysql-connector-*.jar" "mysql*connector*.jar"; do
    for f in $pat; do
      if [ -f "$f" ]; then
        echo "$f"
        return 0
      fi
    done
  done
  # Common subdirs
  for dir in lib libs jars drivers driver "$JAR_DIR"; do
    if [ -d "$dir" ]; then
      for f in "$dir"/mysql-connector-*.jar "$dir"/mysql-connector-j*.jar "$dir"/mysql-connector-java*.jar; do
        if [ -f "$f" ]; then
          echo "$f"
          return 0
        fi
      done
    fi
  done
  return 1
}

MYSQL_JAR_PATH=""
if MYSQL_JAR_PATH="$(detect_repo_mysql_jar)"; then
  echo "Using MySQL JDBC driver from repository: $MYSQL_JAR_PATH"
else
  # Attempt to fetch MySQL Connector/J if not present locally
  if [ ! -f "$DEFAULT_DL_JAR" ]; then
    echo "Downloading MySQL Connector/J..."
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL -o "$DEFAULT_DL_JAR" "$MYSQL_MAVEN_URL" || true
    elif command -v wget >/dev/null 2>&1; then
      wget -q -O "$DEFAULT_DL_JAR" "$MYSQL_MAVEN_URL" || true
    else
      echo "Neither curl nor wget found; skipping download."
    fi
  fi
  if [ -f "$DEFAULT_DL_JAR" ]; then
    MYSQL_JAR_PATH="$DEFAULT_DL_JAR"
  else
    echo "Warning: MySQL JDBC driver not available."
  fi
fi

H2_JAR_PATH=""
# Detect an existing H2 JDBC driver JAR (optional but recommended for local E2E tests)
detect_repo_h2_jar() {
  for pat in "h2*.jar"; do
    for f in $pat; do
      if [ -f "$f" ]; then
        echo "$f"; return 0
      fi
    done
  done
  for dir in lib libs jars drivers driver "$JAR_DIR"; do
    if [ -d "$dir" ]; then
      for f in "$dir"/h2*.jar; do
        if [ -f "$f" ]; then
          echo "$f"; return 0
        fi
      done
    fi
  done
  return 1
}

if H2_JAR_PATH="$(detect_repo_h2_jar)"; then
  echo "Using H2 JDBC driver from repository: $H2_JAR_PATH"
else
  # Attempt to fetch H2 for fallback
  if [ ! -f "$H2_DEFAULT_JAR" ]; then
    echo "Downloading H2 database driver (for fallback testing)..."
    if command -v curl >/dev/null 2>&1; then
      curl -fsSL -o "$H2_DEFAULT_JAR" "$H2_MAVEN_URL" || true
    elif command -v wget >/dev/null 2>&1; then
      wget -q -O "$H2_DEFAULT_JAR" "$H2_MAVEN_URL" || true
    else
      echo "Neither curl nor wget found; skipping H2 download."
    fi
  fi
  if [ -f "$H2_DEFAULT_JAR" ]; then
    H2_JAR_PATH="$H2_DEFAULT_JAR"
  fi
fi

CP="."
if [ -n "$MYSQL_JAR_PATH" ] && [ -f "$MYSQL_JAR_PATH" ]; then
  CP="$CP:$MYSQL_JAR_PATH"
fi
if [ -n "$H2_JAR_PATH" ] && [ -f "$H2_JAR_PATH" ]; then
  CP="$CP:$H2_JAR_PATH"
fi

javac --release 21 -cp "$CP" *.java

if [ $? -eq 0 ]; then
    echo "Compilation successful!"
    echo ""
    echo "Available applications:"
    echo "1. Console version: EventManager"
    echo "2. GUI version: EventManagerGUI"
    echo ""
    echo "Database Setup:"
    echo "  Run: ./setup_database.sh (or configure database.properties)"
    echo ""
    # Generate run scripts with resolved classpath
    RUN_CONSOLE="run-console.sh"
    RUN_GUI="run-gui.sh"
    RUN_DBTEST="run-db-test.sh"
    echo "#!/bin/bash" > "$RUN_CONSOLE"
    echo "exec java -cp '$CP' EventManager \"$@\"" >> "$RUN_CONSOLE"
    echo "#!/bin/bash" > "$RUN_GUI"
    echo "exec java -cp '$CP' EventManagerGUI \"$@\"" >> "$RUN_GUI"
    echo "#!/bin/bash" > "$RUN_DBTEST"
    echo "exec java -cp '$CP' DatabaseConnection \"$@\"" >> "$RUN_DBTEST"
    chmod +x "$RUN_CONSOLE" "$RUN_GUI" "$RUN_DBTEST"
    echo "Run scripts generated: ./run-console.sh, ./run-gui.sh, ./run-db-test.sh"
else
    echo "Compilation failed!"
    exit 1
fi
