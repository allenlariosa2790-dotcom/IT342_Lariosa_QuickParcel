package edu.cit.lariosa.quickparcel.features.auth;

import edu.cit.lariosa.quickparcel.features.auth.dto.LoginRequest;
import edu.cit.lariosa.quickparcel.features.auth.dto.SignupRequest;
import edu.cit.lariosa.quickparcel.features.auth.dto.JwtResponse;
import edu.cit.lariosa.quickparcel.features.auth.dto.MessageResponse;
import edu.cit.lariosa.quickparcel.features.auth.repository.UserRepository;
import edu.cit.lariosa.quickparcel.features.auth.JwtUtils;
import edu.cit.lariosa.quickparcel.features.auth.UserDetailsImpl;
import edu.cit.lariosa.quickparcel.features.shared.entity.User;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    JwtUtils jwtUtils;

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @RequestBody Map<String, String> request) {
        try {
            User user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (request.containsKey("firstName")) {
                user.setFirstName(request.get("firstName"));
            }
            if (request.containsKey("lastName")) {
                user.setLastName(request.get("lastName"));
            }
            if (request.containsKey("phone")) {
                user.setPhone(request.get("phone"));
            }

            userRepository.save(user);

            return ResponseEntity.ok(Map.of("success", true, "message", "Profile updated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

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

        if (authService.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error: Email is already in use!"));
        }

        try {
            User user = authService.registerUser(signUpRequest);
            logger.info("User registered successfully with ID: {}", user.getId());
            return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
        } catch (Exception e) {
            logger.error("Registration failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new MessageResponse("Registration failed: " + e.getMessage()));
        }
    }
}