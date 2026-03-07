Changelog
All notable changes to this project will be documented in this file.

0.1 - 2026-03-07
Added
Initial project setup from scratch
Project structure created:
/backend - Spring Boot application
/frontend - React application
/mobile - Android app (placeholder)
/docs - Documentation files
Backend:
Spring Boot project generated from start.spring.io
Configured MySQL database connection
Added project dependencies (Spring Web, Security, JPA, MySQL, Lombok, Validation)
Created User entity and repository
Implemented JWT authentication from scratch
Created AuthController with login/register endpoints
Added password encryption with BCrypt
Configured CORS for frontend access
Created all 11 database entities based on ERD design
Established JPA relationships (OneToOne, OneToMany, ManyToOne)
Frontend:
React app created with create-react-app
Installed dependencies (axios, react-router-dom, tailwindcss)
Configured Tailwind CSS for styling
Created folder structure (components, pages, services)
Built responsive Navbar component
Built Footer component
Created Home page with hero section
Created Login page with role-based tabs (Sender/Rider)
Created Register page with conditional vehicle field for riders
Created Sender Dashboard with stats cards
Created Profile page
Implemented API service layer with axios
Connected frontend to backend API
Added protected routes with authentication
Documentation:
Created README.md with project overview
Created TASK_CHECKLIST.md with 167 tasks
Created CHANGELOG.md for version tracking
Added .gitignore for all environments
Added Software Design Document (SDD) to /docs
Added ERD diagram to /docs
Added API specifications to /docs
Added UI/UX wireframes to /docs
Changed
N/A (initial release)
Fixed
N/A (initial release)
