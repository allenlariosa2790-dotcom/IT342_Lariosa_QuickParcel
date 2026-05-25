package edu.cit.lariosa.quickparcel.features.email;

import edu.cit.lariosa.quickparcel.features.shared.entity.Delivery;
import edu.cit.lariosa.quickparcel.features.shared.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    public void sendWelcomeEmail(User user) {
        String subject = "Welcome to QuickParcel!";
        String body = buildWelcomeEmailBody(user);
        sendEmail(user.getEmail(), subject, body);
    }

    public void sendDeliveryStatusUpdate(Delivery delivery, User recipient, String oldStatus, String newStatus) {
        String subject = "Delivery " + delivery.getTrackingNumber() + " - Status Update: " + newStatus;
        String body = buildStatusUpdateBody(delivery, recipient, oldStatus, newStatus);
        sendEmail(recipient.getEmail(), subject, body);
    }

    public void sendPaymentConfirmation(Delivery delivery, User recipient) {
        String subject = "Payment Confirmed - " + delivery.getTrackingNumber();
        String body = buildPaymentConfirmationBody(delivery, recipient);
        sendEmail(recipient.getEmail(), subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, false); // false = plain text
            mailSender.send(message);
            System.out.println("Email sent to: " + to);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    private String buildWelcomeEmailBody(User user) {
        return """
            Welcome to QuickParcel, %s %s!
            
            Thank you for joining QuickParcel – your trusted on-demand delivery partner.
            
            Your account has been successfully created as a %s.
            
            You can now:
            • Create delivery requests
            • Track your packages in real-time
            • Connect with nearby riders
            
            Login to your account: %s/login
            
            If you have any questions, feel free to contact our support team.
            
            ---
            QuickParcel Team
            """.formatted(
                user.getFirstName(),
                user.getLastName(),
                user.getUserType(),
                frontendUrl
        );
    }

    private String buildStatusUpdateBody(Delivery delivery, User recipient, String oldStatus, String newStatus) {
        return """
            Delivery Status Update
            
            Tracking Number: %s
            Status: %s → %s
            
            Pickup Address: %s
            Dropoff Address: %s
            
            Track your delivery: %s/tracking/%d
            
            ---
            QuickParcel Team
            """.formatted(
                delivery.getTrackingNumber(),
                oldStatus,
                newStatus,
                delivery.getPickupAddress(),
                delivery.getDropoffAddress(),
                frontendUrl,
                delivery.getId()
        );
    }

    private String buildPaymentConfirmationBody(Delivery delivery, User recipient) {
        return """
            Payment Confirmed!
            
            Tracking Number: %s
            Amount Paid: ₱%.2f
            Payment Method: %s
            Payment Status: %s
            
            Your delivery will be assigned to a rider shortly.
            
            Track your delivery: %s/tracking/%d
            
            Thank you for choosing QuickParcel!
            
            ---
            QuickParcel Team
            """.formatted(
                delivery.getTrackingNumber(),
                delivery.getEstimatedCost(),
                delivery.getPaymentMethod(),
                delivery.getPaymentStatus(),
                frontendUrl,
                delivery.getId()
        );
    }
}