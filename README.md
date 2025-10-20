# Event Management System - Version 2.0

A comprehensive Java application with Swing GUI for managing events with **mandatory** MySQL database connectivity.

---

## 🎯 Key Features (Version 2.0)

### ✅ What's New
- **Java Entry Point:** Application starts from `Main.java` (not batch/shell scripts)
- **Mandatory Database:** MySQL connection is required - no fallback
- **Swing GUI:** Full graphical user interface
- **Auto-Setup:** Database and tables created automatically
- **Smart Build:** Dependencies downloaded automatically

---

## 🚀 Quick Start

### 1. Configure Database
Edit `database.properties`:
```properties
db.url=jdbc:mysql://localhost:3306/eventmanager
db.user=root
db.password=your_mysql_password
```

### 2. Build
```bash
./build.sh
```

### 3. Run
```bash
./run.sh
```

**That's it!** The GUI will launch automatically.

---

## 📋 Prerequisites

### Required
- **JDK 11+** (JDK 21 recommended)
- **MySQL Server** (running on localhost:3306)
- **MySQL credentials** configured in `database.properties`

### Verified On
- Fedora 42
- Ubuntu 22.04+
- CentOS/RHEL 8+

---

## 🎨 Features

### Event Management
- ✅ Create events with full details
- ✅ Edit existing events
- ✅ Delete events (with confirmation)
- ✅ Search and filter events
- ✅ Status tracking (PLANNED, ONGOING, COMPLETED, CANCELLED)

### Attendee Management
- ✅ Add attendees (respects capacity)
- ✅ Remove attendees
- ✅ Real-time capacity tracking
- ✅ Availability monitoring

### Reporting
- ✅ Comprehensive statistics
- ✅ Status breakdown
- ✅ Occupancy rates
- ✅ Export to file

### User Interface
- ✅ Full Swing GUI
- ✅ Table-based event display
- ✅ Search and filter controls
- ✅ Menu bar with all features
- ✅ Dialog-based forms

---

## 📁 Project Structure

### Core Files
- `Main.java` - **Application entry point**
- `EventManagerGUI.java` - Main GUI window
- `DatabaseConnection.java` - Database singleton (mandatory connection)
- `EventDAO.java` - Database operations
- `Event.java` - Event model

### Dialog Classes
- `AddEventDialog.java` - Add new event
- `EditEventDialog.java` - Edit existing event
- `AttendeeManagementDialog.java` - Manage attendees
- `ReportDialog.java` - Generate reports

### Build Scripts
- `build.sh` - **Complete build process (recommended)**
- `run.sh` - Run the application
- `compile.sh` - Simple compilation
- `setup_database.sh` - Database setup helper

### Configuration
- `database.properties` - Database credentials
- `.lib/mysql-connector-j.jar` - MySQL driver (auto-downloaded)

---

## 🔨 Building and Running

### Recommended Method
```bash
# Build (downloads dependencies, compiles)
./build.sh

# Run
./run.sh
```

### Manual Method
```bash
# Compile
javac -cp '.:.lib/mysql-connector-j.jar' *.java

# Run
java -cp '.:.lib/mysql-connector-j.jar' Main
```

---

## 💾 Database Schema

The application automatically creates:

```sql
CREATE DATABASE IF NOT EXISTS eventmanager;

CREATE TABLE IF NOT EXISTS events (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    event_date DATE NOT NULL,
    location VARCHAR(255) NOT NULL,
    description TEXT,
    capacity INT NOT NULL,
    current_attendees INT DEFAULT 0,
    status ENUM('PLANNED', 'ONGOING', 'COMPLETED', 'CANCELLED') DEFAULT 'PLANNED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

---

## 🎯 Usage

### Starting the Application
1. Ensure MySQL is running
2. Configure `database.properties`
3. Run `./build.sh` (first time only)
4. Run `./run.sh`

### Main Window
- **Event Table:** All events displayed
- **Search Bar:** Search by name or location
- **Status Filter:** Filter by event status
- **Buttons:** Quick access to all features
- **Menu Bar:** Organized feature access

### Adding an Event
1. Click "Add Event" button or File → Add New Event
2. Fill in the form:
   - Event Name
   - Date (yyyy-MM-dd)
   - Location
   - Description
   - Capacity
   - Status
3. Click "Add Event"

### Editing an Event
1. Select event in table
2. Click "Edit Event" or double-click row
3. Modify fields
4. Click "Save Changes"

### Managing Attendees
1. Select event in table
2. Click "Manage Attendees"
3. Add or remove attendees
4. Changes saved automatically

### Generating Reports
1. Click "Generate Report"
2. View statistics and event details
3. Export to file if needed

---

## 🐛 Troubleshooting

### "MySQL JDBC Driver not found"
**Solution:**
```bash
./build.sh  # Downloads driver automatically
```

### "Database connection failed"
**Solution:**
```bash
# Check MySQL is running
sudo systemctl status mysql
sudo systemctl start mysql

# Verify credentials in database.properties
cat database.properties
```

### "Compilation failed"
**Solution:**
```bash
# Clean and rebuild
rm -f *.class
rm -rf .lib
./build.sh
```

### GUI doesn't appear
**Solution:**
- Check console for error messages
- Ensure DISPLAY is set (for remote sessions)
- Verify database connection is successful

---

## ⚡ Architecture

### Entry Point Flow
```
Main.java
  ↓
Initialize DatabaseConnection (mandatory)
  ↓
Launch EventManagerGUI (Swing)
  ↓
User interacts with GUI
  ↓
GUI calls EventDAO
  ↓
EventDAO executes SQL via DatabaseConnection
  ↓
Results displayed in GUI
```

### Key Design Decisions

1. **Mandatory Database**
   - Application throws exception if DB unavailable
   - No file-based fallback
   - Ensures data consistency

2. **Singleton Connection**
   - Single database connection instance
   - Connection reuse for efficiency
   - Automatic reconnection handling

3. **Swing GUI**
   - Native Java GUI framework
   - Cross-platform compatibility
   - No external UI dependencies

4. **DAO Pattern**
   - Separation of concerns
   - Database logic isolated
   - Easy to test and maintain

---

## 📚 Documentation

- **QUICKSTART.md** - Fast setup guide
- **USAGE_GUIDE.md** - Comprehensive documentation
- **PROJECT_SUMMARY.md** - Project overview
- **TEST_REPORT.md** - Testing documentation

---

## 🔄 Version History

### Version 2.0 (Current)
- ✨ Mandatory database connection (no fallback)
- ✨ Main.java as entry point (Java-only entry)
- ✨ Full Swing GUI
- ✨ Auto-download MySQL connector
- ✨ Improved build scripts
- ✨ Enhanced error handling
- ✨ Better user experience

### Version 1.0
- Basic event management
- Optional database support
- Console and GUI modes
- File-based fallback

---

## 🤝 Requirements

### Minimum
- JDK 11
- MySQL 5.7+
- 100 MB disk space

### Recommended
- JDK 21
- MySQL 8.0+
- 500 MB disk space

---

## 📝 License

This project is provided as-is for educational and commercial use.

---

## 📞 Support

For issues:
1. Check console output for detailed errors
2. Verify MySQL is running
3. Review `USAGE_GUIDE.md`
4. Check `QUICKSTART.md` for common solutions

---

## 🎓 Learning Resources

This project demonstrates:
- Java Swing GUI development
- JDBC database connectivity
- DAO design pattern
- Singleton pattern
- MVC architecture
- Error handling
- Input validation
- Event-driven programming

---

**Ready to start?** Run `./build.sh` then `./run.sh`!
