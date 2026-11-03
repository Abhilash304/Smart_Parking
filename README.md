<<<<<<< HEAD
# 🅿️ Smart Parking Management System

A beginner-friendly, full-stack parking management system built with **Spring Boot**, **Thymeleaf**, and **MySQL**. It allows users to book parking slots, auto-calculates billing, and provides an admin dashboard for slot management and revenue tracking.

---

## 🛠️ Technology Stack

| Technology | Usage |
|---|---|
| Java 17 | Programming Language |
| Spring Boot 3.2.3 | Backend Framework |
| Spring Data JPA (Hibernate) | Database ORM |
| MySQL 8.0+ | Relational Database |
| Thymeleaf | Server-side HTML Template Engine |
| Maven | Build & Dependency Management |
| Embedded Tomcat | Web Server |
| HTML / CSS | Frontend UI |

---

## ✨ Features

### 👤 User Module
- ✅ User Registration & Login
- ✅ View available parking slots in real-time
- ✅ Book a parking slot (one active booking at a time)
- ✅ Exit parking with auto-calculated bill (₹20/hour)
- ✅ View complete booking history

### 🛡️ Admin Module
- ✅ Pre-created admin account
- ✅ Dashboard with live statistics (Total, Available, Occupied slots)
- ✅ Add new parking slots
- ✅ Delete parking slots (only if unoccupied)
- ✅ View total revenue

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┐
│                    Browser (Client)                  │
│              http://localhost:8080                    │
└──────────────────────┬──────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────┐
│              Spring Boot Application                 │
│                                                      │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │ Controllers  │→│   Services   │→│ Repositories│  │
│  │ (Web Layer)  │  │(Business     │  │ (Data      │  │
│  │              │  │  Logic)      │  │  Access)   │  │
│  └─────────────┘  └──────────────┘  └─────┬──────┘  │
│                                           │          │
│  ┌─────────────┐                          │          │
│  │  Thymeleaf   │                          │          │
│  │  Templates   │                          │          │
│  └─────────────┘                          │          │
└───────────────────────────────────────────┼──────────┘
                                            │
                              ┌─────────────▼─────────┐
                              │     MySQL Database     │
                              │   smart_parking_db     │
                              └───────────────────────┘
```

---

## 📁 Project Structure

```
smart_parking/
├── pom.xml                                    # Maven build file
├── README.md                                  # Project documentation
├── database/
│   └── smart_parking_db.sql                   # Database setup script
└── src/
    └── main/
        ├── java/com/example/parking/
        │   ├── SmartParkingApplication.java   # Main entry point
        │   ├── model/
        │   │   ├── User.java                  # User entity
        │   │   ├── ParkingSlot.java           # Parking slot entity
        │   │   └── Booking.java               # Booking entity
        │   ├── repository/
        │   │   ├── UserRepository.java        # User data access
        │   │   ├── ParkingSlotRepository.java # Slot data access
        │   │   └── BookingRepository.java     # Booking data access
        │   ├── service/
        │   │   ├── UserService.java           # User business logic
        │   │   ├── ParkingSlotService.java    # Slot business logic
        │   │   └── BookingService.java        # Booking & billing logic
        │   └── controller/
        │       ├── HomeController.java        # Home page
        │       ├── UserController.java        # Login, Register, Logout
        │       ├── BookingController.java     # Book, Exit, History
        │       └── AdminController.java       # Admin dashboard & slots
        └── resources/
            ├── application.properties         # App configuration
            └── templates/
                ├── index.html                 # Home/Landing page
                ├── login.html                 # Login page
                ├── register.html              # Registration page
                ├── user-dashboard.html        # User dashboard
                ├── admin-dashboard.html       # Admin dashboard
                └── booking-history.html       # Booking history page
```

---

## 🚀 Step-by-Step Instructions to Run

### Prerequisites

| Software | Version | Download |
|---|---|---|
| Java JDK | 17 or above | [Oracle JDK 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) |
| MySQL Server | 8.0+ | [MySQL Installer](https://dev.mysql.com/downloads/installer/) |
| Maven | 3.8+ | [Apache Maven](https://maven.apache.org/download.cgi) |
| IDE | VS Code or IntelliJ | [VS Code](https://code.visualstudio.com/) / [IntelliJ](https://www.jetbrains.com/idea/) |

---

### Step 1: Set Up the Database

Open **MySQL Command Line** or **MySQL Workbench** and run the SQL script:

```sql
source C:/path/to/smart_parking/database/smart_parking_db.sql;
```

Or manually run:

```sql
CREATE DATABASE IF NOT EXISTS smart_parking_db;
USE smart_parking_db;

-- The script will create tables and insert:
-- Admin user: admin@parking.com / admin123
-- 6 sample parking slots: A-01, A-02, A-03, B-01, B-02, B-03
```

---

### Step 2: Configure Database Credentials

Open `src/main/resources/application.properties` and update your MySQL credentials:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smart_parking_db
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
```

---

### Step 3: Run the Application

#### Option A: Using VS Code

1. Install these VS Code extensions:
   - **Extension Pack for Java** (by Microsoft)
   - **Spring Boot Extension Pack** (by VMware)
2. **File → Open Folder** → select the `smart_parking` folder
3. Wait for Java project to load (check bottom-right progress bar)
4. Open the terminal (**Ctrl + `**) and run:
   ```
   mvn clean spring-boot:run
   ```
5. Or open `SmartParkingApplication.java` → click the **▶ Run** button above `main()`

#### Option B: Using IntelliJ IDEA

1. **File → Open** → select the `smart_parking` folder
2. Wait for Maven to download dependencies
3. Open `SmartParkingApplication.java`
4. Click the **green ▶ Run** button
5. Wait for: `Smart Parking System is Running!`

#### Option C: Using Command Line

```bash
cd smart_parking
mvn clean spring-boot:run
```

---

### Step 4: Open in Browser

Once the application starts successfully, open:

🌐 **http://localhost:8080**

---

## 🔐 Login Credentials

| Role | Email | Password |
|---|---|---|
| **Admin** | admin@parking.com | admin123 |
| **User** | Register a new account | Your chosen password |

---

## 🌐 Application Pages & Endpoints

| Method | URL | Description | Access |
|---|---|---|---|
| GET | `/` | Home / Landing page | Public |
| GET | `/login` | Login page | Public |
| POST | `/login` | Handle login | Public |
| GET | `/register` | Registration page | Public |
| POST | `/register` | Handle registration | Public |
| GET | `/logout` | Logout & clear session | All Users |
| GET | `/user/dashboard` | User dashboard | User |
| POST | `/user/book` | Book a parking slot | User |
| POST | `/user/exit` | Exit parking & get bill | User |
| GET | `/user/history` | View booking history | User |
| GET | `/admin/dashboard` | Admin dashboard | Admin |
| POST | `/admin/add-slot` | Add a new parking slot | Admin |
| POST | `/admin/delete-slot` | Delete a parking slot | Admin |

---

## 💰 Pricing Model

| Item | Rate |
|---|---|
| Parking Fee | **₹20 per hour** |
| Minimum Charge | **₹20** (1 hour minimum) |
| Billing | Auto-calculated on exit (rounded up to nearest hour) |

**Example:**
- Parked for 30 minutes → ₹20
- Parked for 1 hour 15 minutes → ₹40
- Parked for 3 hours → ₹60

---

## 📊 Database Schema

### `users` Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Auto-increment primary key |
| name | VARCHAR(100) | User's full name |
| email | VARCHAR(100) | Unique email address |
| password | VARCHAR(255) | Plain text password |
| role | VARCHAR(20) | USER or ADMIN |

### `parking_slots` Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Auto-increment primary key |
| slot_number | VARCHAR(20) | Unique slot identifier (e.g., A-01) |
| status | VARCHAR(20) | AVAILABLE or OCCUPIED |

### `bookings` Table
| Column | Type | Description |
|---|---|---|
| id | BIGINT (PK) | Auto-increment primary key |
| user_id | BIGINT (FK) | References users.id |
| parking_slot_id | BIGINT (FK) | References parking_slots.id |
| entry_time | DATETIME | When the car was parked |
| exit_time | DATETIME | When the car exited (NULL if active) |
| total_amount | DOUBLE | Calculated parking fee |

### ER Diagram

```
┌──────────┐       ┌──────────────┐       ┌───────────────┐
│  users   │       │   bookings   │       │ parking_slots │
├──────────┤       ├──────────────┤       ├───────────────┤
│ id (PK)  │───┐   │ id (PK)      │   ┌───│ id (PK)       │
│ name     │   └──→│ user_id (FK) │   │   │ slot_number   │
│ email    │       │ slot_id (FK) │←──┘   │ status        │
│ password │       │ entry_time   │       └───────────────┘
│ role     │       │ exit_time    │
└──────────┘       │ total_amount │
                   └──────────────┘
```

---

## 🔄 Application Workflow

```
                    ┌──────────┐
                    │  Home /  │
                    └────┬─────┘
                         │
              ┌──────────┴──────────┐
              ▼                     ▼
        ┌──────────┐          ┌──────────┐
        │  Login   │          │ Register │
        └────┬─────┘          └──────────┘
             │
     ┌───────┴───────┐
     ▼               ▼
┌─────────┐    ┌──────────┐
│  USER   │    │  ADMIN   │
│Dashboard│    │Dashboard │
├─────────┤    ├──────────┤
│Book Slot│    │Add Slot  │
│Exit Park│    │Delete    │
│History  │    │Revenue   │
└─────────┘    └──────────┘
```

---

## ❗ Troubleshooting

| Problem | Solution |
|---|---|
| `Port 8080 already in use` | Run: `netstat -ano \| findstr :8080` then `taskkill /F /PID <PID>` |
| `mvn not recognized` | Add Maven `bin` folder to system PATH environment variable |
| `Access denied for user 'root'` | Update username/password in `application.properties` |
| `Unknown database 'smart_parking_db'` | Run the SQL script: `database/smart_parking_db.sql` |
| `Parameter name not available` | Make sure you're using the latest code with explicit `@RequestParam` names |
| Java version error | Ensure JDK 17 is installed and `JAVA_HOME` is set |

---

## 📌 Important Notes

- 🔓 **No Spring Security** – uses simple session-based authentication
- 🚫 **No password hashing** – passwords stored in plain text (for simplicity)
- 🔄 **Auto DDL** – `spring.jpa.hibernate.ddl-auto=update` auto-creates/updates tables
- 📐 **MVC Architecture** – clean separation of Model, View, and Controller layers
- 🎨 **Server-side rendering** – Thymeleaf templates, no separate frontend framework

---

## 🧑‍💻 Author

Built as a learning project to demonstrate Spring Boot MVC with Thymeleaf and MySQL.

---

## 📝 License

This project is for educational purposes. Free to use and modify.
=======
# Smart_Parking
A full-stack Smart Parking Management System built using Java, Spring Boot, Spring Data JPA, Thymeleaf, and MySQL. Features include user registration and login, real-time parking slot availability, online slot booking, automated parking fee calculation, booking history, and an admin dashboard for managing parking slots and tracking revenue.
>>>>>>> bc1a52518a1cfc6a0cad5c1c459199b8eab1a6f7
