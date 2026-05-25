package edu.cit.lariosa.quickparcel.features.email;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import edu.cit.lariosa.quickparcel.features.shared.entity.Delivery;
import edu.cit.lariosa.quickparcel.features.shared.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class SendGridEmailService {

    @Autowired(required = false)
    private SendGrid sendGrid;

    @Value("${sendgrid.sender.email}")
    private String fromEmail;

    @Value("${sendgrid.sender.name:QuickParcel}")
    private String fromName;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private boolean isEmailEnabled() {
        return sendGrid != null;
    }

    public void sendWelcomeEmail(User user) {
        if (!isEmailEnabled()) {
            System.out.println("Email service not configured. Skipping welcome email.");
            return;
        }

        String subject = "Welcome to QuickParcel!";
        String htmlContent = buildWelcomeHtml(user);
        sendEmail(user.getEmail(), subject, htmlContent);
    }

    public void sendDeliveryStatusUpdate(Delivery delivery, User recipient, String oldStatus, String newStatus) {
        if (!isEmailEnabled()) {
            System.out.println("Email service not configured. Skipping status update email.");
            return;
        }

        String subject = "Delivery " + delivery.getTrackingNumber() + " - Status: " + newStatus;
        String htmlContent = buildStatusUpdateHtml(delivery, recipient, oldStatus, newStatus);
        sendEmail(recipient.getEmail(), subject, htmlContent);
    }

    public void sendPaymentConfirmation(Delivery delivery, User recipient) {
        if (!isEmailEnabled()) {
            System.out.println("Email service not configured. Skipping payment confirmation email.");
            return;
        }

        String subject = "Payment Confirmed - " + delivery.getTrackingNumber();
        String htmlContent = buildPaymentConfirmationHtml(delivery, recipient);
        sendEmail(recipient.getEmail(), subject, htmlContent);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email toEmail = new Email(to);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, subject, toEmail, content);

            Request request = new Request();
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("✅ Email sent to: " + to + " (Status: " + response.getStatusCode() + ")");
            } else {
                System.err.println("❌ Failed to send email to: " + to + " (Status: " + response.getStatusCode() + ")");
                System.err.println("Response: " + response.getBody());
            }
        } catch (IOException e) {
            System.err.println("❌ Error sending email to " + to + ": " + e.getMessage());
        }
    }

    private String buildWelcomeHtml(User user) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: #2563EB; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0;">
                        <h1>Welcome to QuickParcel!</h1>
                    </div>
                    <div style="background: #f9fafb; padding: 20px; border-radius: 0 0 10px 10px;">
                        <h2>Hello %s %s!</h2>
                        <p>Thank you for joining QuickParcel – your trusted on-demand delivery partner.</p>
                        <p>Your account has been successfully created as a <strong>%s</strong>.</p>
                        <p>You can now:</p>
                        <ul>
                            <li>Create delivery requests</li>
                            <li>Track your packages in real-time</li>
                            <li>Connect with nearby riders</li>
                        </ul>
                        <a href="%s/login" style="display: inline-block; background: #2563EB; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin-top: 15px;">Login to Your Account</a>
                    </div>
                    <div style="text-align: center; padding: 15px; font-size: 12px; color: #6b7280;">
                        <p>&copy; 2026 QuickParcel. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(
                user.getFirstName(),
                user.getLastName(),
                user.getUserType(),
                frontendUrl
        );
    }

    private String buildStatusUpdateHtml(Delivery delivery, User recipient, String oldStatus, String newStatus) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: #2563EB; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0;">
                        <h1>Delivery Status Update</h1>
                    </div>
                    <div style="background: #f9fafb; padding: 20px; border-radius: 0 0 10px 10px;">
                        <h2>Tracking #: %s</h2>
                        <p>Your delivery status has been updated:</p>
                        <div style="text-align: center; margin: 20px 0;">
                            <span style="background: #e5e7eb; padding: 5px 10px; border-radius: 20px;">%s</span>
                            <span style="font-size: 20px;"> → </span>
                            <span style="background: #10b981; color: white; padding: 5px 10px; border-radius: 20px;">%s</span>
                        </div>
                        <div style="background: white; padding: 10px; border-radius: 5px; margin: 10px 0; border-left: 3px solid #2563EB;">
                            <strong>📍 Pickup:</strong> %s<br>
                            <strong>🏁 Dropoff:</strong> %s
                        </div>
                        <a href="%s/tracking/%d" style="display: inline-block; background: #2563EB; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Track Your Delivery</a>
                    </div>
                    <div style="text-align: center; padding: 15px; font-size: 12px; color: #6b7280;">
                        <p>&copy; 2026 QuickParcel. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
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

    private String buildPaymentConfirmationHtml(Delivery delivery, User recipient) {
        return """
            <!DOCTYPE html>
            <html>
            <head><meta charset="UTF-8"></head>
            <body style="font-family: Arial, sans-serif; line-height: 1.6;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="background: #10b981; color: white; padding: 20px; text-align: center; border-radius: 10px 10px 0 0;">
                        <h1>Payment Confirmed!</h1>
                    </div>
                    <div style="background: #f9fafb; padding: 20px; border-radius: 0 0 10px 10px;">
                        <h2>Tracking #: %s</h2>
                        <div style="text-align: center; font-size: 28px; font-weight: bold; color: #10b981; margin: 20px 0;">₱%.2f</div>
                        <div style="background: white; padding: 15px; border-radius: 8px; margin: 15px 0;">
                            <p><strong>Payment Method:</strong> %s</p>
                            <p><strong>Payment Status:</strong> %s</p>
                            <p><strong>Your delivery will be assigned to a rider shortly.</strong></p>
                        </div>
                        <a href="%s/tracking/%d" style="display: inline-block; background: #2563EB; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;">Track Your Delivery</a>
                    </div>
                    <div style="text-align: center; padding: 15px; font-size: 12px; color: #6b7280;">
                        <p>&copy; 2026 QuickParcel. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
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