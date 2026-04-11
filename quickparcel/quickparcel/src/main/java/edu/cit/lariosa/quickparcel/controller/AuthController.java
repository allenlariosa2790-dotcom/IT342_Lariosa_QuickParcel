package edu.cit.lariosa.quickparcel.controller;

import edu.cit.lariosa.quickparcel.dto.*;
import edu.cit.lariosa.quickparcel.entity.*;
import edu.cit.lariosa.quickparcel.repository.*;
import edu.cit.lariosa.quickparcel.security.JwtUtils;
import edu.cit.lariosa.quickparcel.security.UserDetailsImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    SenderRepository senderRepository;

    @Autowired
    RiderRepository riderRepository;

    @Autowired
    AdminRepository adminRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for: {}", loginRequest.getEmail());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.ok(new JwtResponse(jwt, userDetails.getId(), userDetails.getEmail(),
                userDetails.getFirstName(), userDetails.getLastName(), userDetails.getUserType()));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        logger.info("Registration attempt for: {}", signUpRequest.getEmail());

        // Check if email already exists
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            logger.warn("Email already in use: {}", signUpRequest.getEmail());
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user account
        User user = new User();
        user.setEmail(signUpRequest.getEmail());
        user.setPasswordHash(encoder.encode(signUpRequest.getPassword()));
        user.setFirstName(signUpRequest.getFirstName());
        user.setLastName(signUpRequest.getLastName());
        user.setPhone(signUpRequest.getPhone());
        user.setUserType(signUpRequest.getUserType());

        userRepository.save(user);
        logger.info("User created with ID: {}", user.getId());

        // Create role-specific record based on user type
        if ("SENDER".equals(signUpRequest.getUserType())) {
            Sender sender = new Sender();
            //sender.setUserId(user.getId());
            sender.setUser(user);
            sender.setCreatedAt(LocalDateTime.now());
            senderRepository.save(sender);
            logger.info("Sender record created for user ID: {}", user.getId());
        } else if ("RIDER".equals(signUpRequest.getUserType())) {
            Rider rider = new Rider();
            //rider.setUserId(user.getId());
            rider.setUser(user);
            rider.setCreatedAt(LocalDateTime.now());
            riderRepository.save(rider);
            logger.info("Rider record created for user ID: {}", user.getId());
        } else if ("ADMIN".equals(signUpRequest.getUserType())) {
            Admin admin = new Admin();
            //admin.setUserId(user.getId());
            admin.setUser(user);
            admin.setCreatedAt(LocalDateTime.now());
            adminRepository.save(admin);
            logger.info("Admin record created for user ID: {}", user.getId());
        }

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        Map<String, Object> response = new HashMap<>();
        response.put("id", userDetails.getId());
        response.put("email", userDetails.getEmail());
        response.put("firstName", userDetails.getFirstName());
        response.put("lastName", userDetails.getLastName());
        response.put("userType", userDetails.getUserType());

        return ResponseEntity.ok(response);
    }
}