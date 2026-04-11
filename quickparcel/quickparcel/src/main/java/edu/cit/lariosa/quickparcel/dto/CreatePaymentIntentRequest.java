package edu.cit.lariosa.quickparcel.dto.paymongo;

import lombok.Data;
import java.util.List;

@Data
public class CreatePaymentIntentRequest {
    private Data data;

    @lombok.Data
    public static class Data {
        private Attributes attributes;
    }

    @lombok.Data
    public static class Attributes {
        private int amount;
        private String currency = "PHP";
        private List<String> paymentMethodAllowed = List.of("gcash", "card");
        private String description;
        private String statementDescriptor = "QuickParcel";
    }
}