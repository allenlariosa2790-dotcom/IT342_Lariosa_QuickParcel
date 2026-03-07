# 📦 QuickParcel - On-Demand Logistics Platform

A localized courier platform connecting users with nearby riders for efficient package delivery within communities. Built from scratch with Spring Boot, React, and MySQL.

## 🚀 Project Overview

QuickParcel is a full-stack logistics platform that allows users (Senders) to request package deliveries and nearby couriers (Riders) to accept and fulfill these requests. This project is built entirely from scratch as part of IT342 - System Integration and Architecture.

## 📁 Project Structure
IT342-Lariosa-QuickParcel/
├── 📂 backend/ # Spring Boot REST API (Java)
├── 📂 frontend/ # React web application
├── 📂 mobile/ # Kotlin Android app (coming soon)
├── 📂 docs/ # Documentation files (SDD, diagrams)
├── 📄 README.md # Project overview
├── 📄 TASK_CHECKLIST.md # Development tasks tracker
└── 📄 CHANGELOG.md # Version history


## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Java 17, Spring Boot 3.5.11, Spring Security, JWT, Spring Data JPA, MySQL |
| **Frontend** | React 18, TypeScript, Tailwind CSS, Axios, React Router DOM |
| **Database** | MySQL 8.0 with XAMPP/phpMyAdmin |
| **Tools** | Maven, npm, Git, Postman, IntelliJ IDEA, VS Code |

## ✨ Features Implemented (v0.1)

### Backend Features
- ✅ User registration with BCrypt password encryption
- ✅ User login with JWT token generation
- ✅ JWT authentication filter
- ✅ Role-based user types (SENDER, RIDER, ADMIN)
- ✅ 11 database tables with proper relationships
- ✅ RESTful API endpoints for authentication
- ✅ CORS configuration for frontend access
- ✅ Global exception handling

### Frontend Features
- ✅ Responsive homepage with hero section
- ✅ "How It Works" 3-step process
- ✅ Features grid (2x2 layout)
- ✅ Testimonials section
- ✅ Login page with role tabs (Sender/Rider)
- ✅ Registration page with role-based forms
- ✅ Sender Dashboard with stats cards
- ✅ Profile page with user information
- ✅ Protected routes with authentication
- ✅ Responsive Navbar and Footer
- ✅ API integration with backend

## 🚦 Getting Started

### Prerequisites

- **Java 17** or higher
- **Node.js 18+** and npm
- **MySQL** (XAMPP recommended)
- **Git**

### Database Setup

1. Start MySQL in XAMPP Control Panel
2. Open phpMyAdmin: `http://localhost/phpmyadmin`
3. Create database: `quickparcel_db`

### Backend Setup

```bash
# Clone the repository
git clone https://github.com/allenlariosa2790-dotcom/IT342-Lariosa-QuickParcel.git

# Navigate to backend
cd IT342-Lariosa-QuickParcel/backend

# Configure database
# Edit src/main/resources/application.properties:
# spring.datasource.username=root
# spring.datasource.password=     # blank for XAMPP

# Run the application
./mvnw spring-boot:run

Backend runs at: http://localhost:8080

### Frontend Setup
# Navigate to frontend
cd IT342-Lariosa-QuickParcel/frontend

# Install dependencies
npm install

# Start development server
npm start

Frontend runs at: http://localhost:3000

### 🔌 API Endpoints

Method	Endpoint	Description
POST	/api/auth/register	Register new user
POST	/api/auth/login	Login and receive JWT token
GET	/api/auth/me	Get current authenticated user
GET	/api/test	Test endpoint

### Database Schema
The database consists of 11 tables:

users - Base user accounts

senders - Sender-specific data

riders - Rider-specific data

admins - Admin-specific data

vehicles - Rider vehicles

parcels - Package details

deliveries - Core delivery transactions

tracking_history - Status change logs

payments - Payment transactions

files - Uploaded parcel images

refresh_tokens - JWT refresh tokens

See /docs/ERD.png for the complete entity relationship diagram.

### 🧪 Testing
bash
# Test backend
cd backend
./mvnw test

# Test frontend
cd frontend
npm test

### 📝 License
This project is created for academic purposes (IT342 - System Integration and Architecture).

### 👤 Author
Allen N. Lariosa

Email: allenlariosa2790@gmail.com

GitHub: allenlariosa2790-dotcom

### 📅 Course Information
Course: IT342 - System Integration and Architecture

Section: G5

Academic Year: 2025-2026

### 🔄 Version History
See CHANGELOG.md for detailed version history.

Current version: 0.1 - Initial release with complete authentication system