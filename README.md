# SamounGram

A feature-rich desktop messaging application inspired by Telegram, built with Java, JavaFX, and PostgreSQL. This project was developed for the Advanced Programming course in Shahid Beheshti University (Summer 2025).

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-ED8B00?style=for-the-badge&logo=javafx&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)

## Login Page
![Login Page](https://github.com/user-attachments/assets/1ecd513f-59a2-4382-9faf-557e12010e48)

## Home Page
![Home Page](https://github.com/user-attachments/assets/dc637321-2ab5-430c-b771-e9a01a9b98d8)

## Table of Contents

1.  [Description](#description)
2.  [Features](#features)
3.  [Technology Stack](#technology-stack)
4.  [Installation & Setup](#installation--setup)
5.  [Usage](#usage)
6.  [Project Structure](#project-structure)
7.  [Team & Credits](#team--credits)
8.  [Changelog](#changelog)
9.  [Contact](#contact)

## Description

This project is a desktop implementation of a modern messaging platform. Its core purpose is to demonstrate proficiency in advanced Java programming concepts, including Object-Oriented Programming (OOP) principles, GUI development with JavaFX, database integration with PostgreSQL, and software development best practices like version control with Git.

The application allows users to register, log in, and communicate through private chats, group chats, and channels, providing a seamless and intuitive user experience.

## Features

### 🟢 Mandatory Features
*   **User Account Management:** Secure registration and login with BCrypt password hashing.
*   **Private Chats:** One-on-one text messaging with full chat history.
*   **Group Chats:** Create groups, add members, and chat with multiple users.
*   **Channels:** Create channels, broadcast messages to subscribers, and view channel history.
*   **Search Functionality:** Search for users by their username to start chats or add to groups.
*   **Profile Viewing:** View your own profile and the profiles of other users.
*   **Data Persistence:** All data is securely stored and retrieved from a PostgreSQL database.

### 🟡 Bonus Features (Implemented)
*   **User Status Indicators:** Visual indicators showing online/offline status.
*   **Real-time Notifications:** In-app notifications for new messages
*   **Message Read Status:** Messages are marked as "read" when viewed by the recipient (double checkmarks ✓✓)
*   **Dark Mode Theme:** Complete dark mode implementation for comfortable nighttime usage
*   **User Status Indicators:** Visual indicators showing online/offline status

## Technology Stack

*   **Language:** Java 17+
*   **GUI Toolkit:** JavaFX
*   **Build Tool:** Gradle
*   **Database:** PostgreSQL
*   **Password Hashing:** BCrypt

## Installation & Setup

### Prerequisites
Before you begin, ensure you have the following installed on your machine:
*   **Java JDK 17** or higher
*   **PostgreSQL** (v12 or higher)
*   **Git**

### Step 1: Clone the Repository
```bash
git clone <https://github.com/ShirinHosseinNiya/Samoungram.git>
cd SamounGram
```

### Step 2: Set Up the Database
1.  Open your PostgreSQL shell.
2.  Create a new database for the project:
    ```sql
    CREATE DATABASE telegram_db;
    ```
3.  The application will automatically generate the necessary tables upon first run. Ensure your database user has the required privileges to create tables.

### Step 3: Configure Database Connection
Locate the database configuration file at `src/main/resources/config.properties`. Update the connection URL, username, and password to match your local PostgreSQL setup.

Example `config.properties`:
```properties
db.url=jdbc:postgresql://localhost:5432/telegram_db
db.user=postgres
db.password=your_password_here
```

### Step 4: Build and Run the Application
Using Gradle, you can easily build and run the project from the command line:

```bash
# Run the application
./gradlew run

# Build the project
./gradlew build
```
The application will launch automatically after running ./gradlew run.

## Usage

1. **Registration:** Launch the app and click "Register" to create a new account. You will need a unique username and a password.
2. **Login:** Enter your credentials to log into the application.
3. **Start Chatting:**
*   Use the search bar to find a user by their username and start a private chat
*   Click "New Group" to create a group chat and add members
*   Click "New Channel" to create a channel and start broadcasting messages
4. **Search:** Use the search functionality to find users, groups, or channels to interact with

## Project Structure

```text
src/
├── main/
│   └── java/
│       ├── org/
│       │   └── project/            
│       │       ├── client/
│       │       │   └── views/              # JavaFX Controller classes
│       │       ├── models/                 # Data models (User, Message, Chat, etc.)
│       │       ├── server/                 # Server classes
│       │       │   └── db/                 # Database connection and DAO classes
│       │       ├── util/                   # Utility classes (BCrypt, Helpers)
│       │       └── Main.java               # Application entry point
│       │          
│       └── resources/
│           └── org/
│               └── project/
│                   └── client/
│                       └── views/          # FXML files for UI layouts
└── test/                                   # Unit tests
```

## Team & Credits

This project was developed by:
*   **[Shirin HosseinNiya](https://github.com/ShirinHosseinNiya)** - `t.me/shurbea`
*   **[SoyMori](https://github.com/SoyMori)** - `t.me/yo_soy_mori`

**Credits & Acknowledgments:**
*   Course Instructor: Dr. Saeed Reza Kheradpisheh
*   Built as part of the Advanced Programming Winter, Spring, and Summer 2025 course.
*   Icons and design inspiration from Telegram.

## Changelog

### [v1.0.0] - Initial Release (2025-09-08)
*   Initial project submission
*   Implemented all mandatory features: User management, private chats, group chats, channels, and search
*   Enhanced UI with custom JavaFX styling
*   Integrated PostgreSQL database with BCrypt for secure authentication

## Contact
For questions, support, or contributions, please feel free to contact us via our GitHub profiles or Telegram handles linked above.

Note: This repository is private for course evaluation purposes. Access is restricted to the development team and course staff.