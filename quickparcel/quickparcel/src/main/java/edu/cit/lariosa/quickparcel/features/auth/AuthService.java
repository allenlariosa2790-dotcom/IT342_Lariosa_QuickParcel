package edu.cit.lariosa.quickparcel.features.auth;

import edu.cit.lariosa.quickparcel.features.auth.dto.SignupRequest;
import edu.cit.lariosa.quickparcel.features.auth.repository.UserRepository;
import edu.cit.lariosa.quickparcel.features.auth.repository.RefreshTokenRepository;
import edu.cit.lariosa.quickparcel.features.email.SendGridEmailService;
import edu.cit.lariosa.quickparcel.features.shared.entity.User;
import edu.cit.lariosa.quickparcel.features.shared.entity.Sender;
import edu.cit.lariosa.quickparcel.features.shared.entity.Rider;
import edu.cit.lariosa.quickparcel.features.shared.entity.Admin;
import edu.cit.lariosa.quickparcel.features.shared.repository.SenderRepository;
import edu.cit.lariosa.quickparcel.features.shared.repository.RiderRepository;
import edu.cit.lariosa.quickparcel.features.shared.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SenderRepository senderRepository;

    @Autowired
    private RiderRepository riderRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SendGridEmailService emailService;

    @Transactional
    public User registerUser(SignupRequest request) {
        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already in use!");
        }

        // Create new user
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setUserType(request.getUserType());
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);
        user = userRepository.save(user);

        // Create role-specific record
        if ("SENDER".equals(request.getUserType())) {
            Sender sender = new Sender();
            sender.setUser(user);
            sender.setCreatedAt(LocalDateTime.now());
            if (sender.getTotalDeliveriesRequested() == null) {
                sender.setTotalDeliveriesRequested(0);
            }
            senderRepository.save(sender);
        } else if ("RIDER".equals(request.getUserType())) {
            Rider rider = new Rider();
            rider.setUser(user);
            rider.setCreatedAt(LocalDateTime.now());
            rider.setIsVerified(false);
            rider.setIsActive(true);
            if (rider.getTotalDeliveriesCompleted() == null) {
                rider.setTotalDeliveriesCompleted(0);
            }
            riderRepository.save(rider);
        } else if ("ADMIN".equals(request.getUserType())) {
            Admin admin = new Admin();
            admin.setUser(user);
            admin.setCreatedAt(LocalDateTime.now());
            adminRepository.save(admin);
        }

        // Send welcome email
        try {
            emailService.sendWelcomeEmail(user);
        } catch (Exception e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }

        return user;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}