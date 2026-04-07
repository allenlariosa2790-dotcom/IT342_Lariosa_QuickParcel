# Changelog

All notable changes to this project will be documented in this file.
## [0.5] - 2026-04-07

### Added
- **Unified Cost Calculation** – Backend `/api/deliveries/calculate-distance` now uses the same formula as `DeliveryService` (base fare + per‑km + weight surcharge), ensuring frontend preview matches stored cost.
- **Enhanced Distance Service** – Added fallback to OSRM public routing and Nominatim geocoding when ORS API key is missing or fails; reduces “using estimate” errors.

### Fixed
- **Tracking page** – Last updated timestamp now displays correctly (was showing N/A).
- **Sender Dashboard** – Create Delivery button now navigates properly.
- **Estimated cost mismatch** – Preview cost now matches the cost saved in database and shown on tracking page.
- **LocationPicker height** – Map increased from `h-64` to `h-96` for better usability.
- **RiderDashboard** – Active deliveries now show all (ACCEPTED, PICKED_UP, IN_TRANSIT), not just one.
- **Distance calculation fallback** – No longer shows error banner when fallback works; uses OSRM/Nominatim seamlessly.

### Changed
- Frontend `calculateDistance` service now accepts `weight` parameter.
- `CreateDelivery` passes parcel weight to `/calculate-distance` endpoint.

---
## [0.4]
-NA
## [0.3] - 2026-03-28
### Added
- **Complete Backend Delivery Management System**
    - Created all 11 entities based on ERD (User, Sender, Rider, Admin, Parcel, Delivery, Vehicle, TrackingHistory, Payment, File, RefreshToken)
    - Created corresponding repositories for all entities
    - Established proper JPA relationships (OneToOne, OneToMany, ManyToOne)

- **Delivery Service & Controller**
    - Created DeliveryService with business logic:
        - Create delivery with auto-generated tracking number
        - Accept delivery by rider
        - Update delivery status with location tracking
        - Cancel delivery
        - Get tracking history
    - Implemented DeliveryController with REST endpoints
    - Added automatic tracking history recording on every status change

### API Endpoints Added
- `POST /api/deliveries` - Create new delivery request
- `GET /api/deliveries/available` - Get available deliveries for riders
- `GET /api/deliveries/my` - Get current user's deliveries
- `PUT /api/deliveries/{id}/accept` - Accept delivery (rider only)
- `PUT /api/deliveries/{id}/status` - Update delivery status with location
- `GET /api/deliveries/{id}/track` - Get full tracking history
- `PUT /api/deliveries/{id}/cancel` - Cancel delivery

### Technical Improvements
- Tracking number format: `QP-timestamp` (e.g., QP-1743161234567)
- Status workflow: PENDING → ACCEPTED → PICKED_UP → IN_TRANSIT → DELIVERED → CANCELLED
- Automatic timestamp recording for pickup and delivery events
- Location tracking for each status update

---
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
- Full commit hash: 8277bcb

---

**Project Status:** ✅ Phase 1 Complete - Authentication System Working
- ✅ User registration with validation
- ✅ User login with JWT
- ✅ Database storage with password encryption
- ✅ React frontend with matching SDD design
- ✅ Full integration between frontend and backend

---

[0.3]: https://github.com/allenlariosa2790-dotcom/IT342-Lariosa-QuickParcel/releases/tag/v0.3
[0.2]: https://github.com/allenlariosa2790-dotcom/IT342-Lariosa-QuickParcel/releases/tag/v0.2
[0.1]: https://github.com/allenlariosa2790-dotcom/IT342-Lariosa-QuickParcel/releases/tag/v0.1