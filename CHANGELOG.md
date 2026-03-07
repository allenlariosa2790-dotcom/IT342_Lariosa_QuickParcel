# Changelog

All notable changes to this project will be documented in this file.

## [0.2] - 2026-03-07
### Added
- **Phase 1 Completion - User Registration and Login**
- Complete authentication system implemented from scratch

### Backend
- Spring Boot project initialized with Spring Security, JPA, MySQL
- JWT authentication with token generation and validation
- User registration endpoint (`POST /api/auth/register`)
- User login endpoint (`POST /api/auth/login`)
- Current user endpoint (`GET /api/auth/me`)
- Password encryption with BCrypt (12 salt rounds)
- Duplicate email validation preventing multiple registrations
- User entity with fields: id, email, password_hash, first_name, last_name, phone, user_type
- MySQL database integration with proper schema
- CORS configuration for frontend access
- Global exception handling for auth errors

### Frontend
- React application with Tailwind CSS
- Login page with role tabs (Sender/Rider)
- Registration page with conditional vehicle fields for riders
- Success message popup on successful login
- Form validation for all inputs
- Axios integration with backend API
- JWT token storage in localStorage
- Protected routes with role-based access
- Sender Dashboard with stats cards
- Rider Dashboard with available deliveries
- Available Deliveries page for riders
- Earnings page with transaction history
- Profile page displaying user info
- Responsive design matching SDD wireframes

### Documentation
- Software Design Document (SDD) completed
- README.md with setup instructions
- TASK_CHECKLIST.md with 211 tasks
- CHANGELOG.md for version tracking
- .gitignore configured for all environments

### GitHub Repository
- Repository: IT342-Lariosa-QuickParcel
- Initial code push with complete backend and frontend
- Branch structure cleaned up with main as default

## [0.1] - 2026-03-07
### Added
- Initial project structure
- Backend and frontend setup
- Basic dependencies installed
- Project documentation templates

---

## Commit History for Phase 1
- `IT342 Phase 1 – User Registration and Login Completed`
- Full commit hash: [Insert your commit hash here]

---

**Project Status:** ✅ Phase 1 Complete - Authentication System Working
- ✅ User registration with validation
- ✅ User login with JWT
- ✅ Database storage with password encryption
- ✅ React frontend with matching SDD design
- ✅ Full integration between frontend and backend

---

[0.2]: https://github.com/allenlariosa2790-dotcom/IT342-Lariosa-QuickParcel/releases/tag/v0.2
[0.1]: https://github.com/allenlariosa2790-dotcom/IT342-Lariosa-QuickParcel/releases/tag/v0.1