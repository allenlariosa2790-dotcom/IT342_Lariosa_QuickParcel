package edu.cit.lariosa.quickparcel.features.email;

import com.sendgrid.SendGrid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SendGridConfig {

    @Value("${sendgrid.api.key}")
    private String apiKey;

    @Bean
    public SendGrid sendGrid() {
        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("⚠️ Warning: SendGrid API key is not configured!");
            return null;
        }
        return new SendGrid(apiKey);
    }
}