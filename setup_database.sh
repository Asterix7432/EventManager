#!/bin/bash

echo "Event Management System - Database Setup"
echo "======================================="
echo ""

# Check if MySQL is installed
if ! command -v mysql &> /dev/null; then
    echo "MySQL is not installed. Please install MySQL first."
    echo "On Ubuntu/Debian: sudo apt-get install mysql-server"
    echo "On CentOS/RHEL: sudo yum install mysql-server"
    echo "On Fedora: sudo dnf install mysql-server"
    exit 1
fi

echo "MySQL is installed. Proceeding with database setup..."
echo ""

# Get database credentials (defaults to 'password' for root as per app config)
mysql_password=${MYSQL_ROOT_PASSWORD:-password}
echo "Using MySQL root password from env or default. Current value assumed: 'password'"

# Create database (user is root/password for the app)
echo "Creating database if not exists..."
mysql -u root -p$mysql_password << EOF
CREATE DATABASE IF NOT EXISTS eventmanager;
EOF

if [ $? -eq 0 ]; then
    echo "Database setup completed successfully!"
    echo ""
    echo "Database Details:"
    echo "  Database: eventmanager"
    echo "  User: root"
    echo "  Password: password"
    echo ""
    echo "You can now run the database-enabled applications:"
    echo "  Console: java -cp '.:.lib/mysql-connector-j.jar' EventManager"
    echo "  GUI: java -cp '.:.lib/mysql-connector-j.jar' EventManagerGUI"
else
    echo "Database setup failed. Please check your MySQL credentials and try again."
    exit 1
fi
