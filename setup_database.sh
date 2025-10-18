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

# Get database credentials
read -p "Enter MySQL root password: " -s mysql_password
echo ""

# Create database and user
echo "Creating database and user..."
mysql -u root -p$mysql_password << EOF
CREATE DATABASE IF NOT EXISTS eventmanager;
CREATE USER IF NOT EXISTS 'eventuser'@'localhost' IDENTIFIED BY 'eventpass';
GRANT ALL PRIVILEGES ON eventmanager.* TO 'eventuser'@'localhost';
FLUSH PRIVILEGES;
EOF

if [ $? -eq 0 ]; then
    echo "Database setup completed successfully!"
    echo ""
    echo "Database Details:"
    echo "  Database: eventmanager"
    echo "  User: eventuser"
    echo "  Password: eventpass"
    echo ""
    echo "You can now run the database-enabled applications:"
    echo "  Console: java EventManagerDB"
    echo "  GUI: java EventManagerGUIDB"
else
    echo "Database setup failed. Please check your MySQL credentials and try again."
    exit 1
fi
