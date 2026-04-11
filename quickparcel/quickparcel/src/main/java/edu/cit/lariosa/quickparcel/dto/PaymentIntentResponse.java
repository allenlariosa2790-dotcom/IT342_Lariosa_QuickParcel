package edu.cit.lariosa.quickparcel.dto.paymongo;

import lombok.Data;

@Data
public class PaymentIntentResponse {
    private Data data;

    @lombok.Data
    public static class Data {
        private String id;
        private String type;
        private Attributes attributes;
    }

    @lombok.Data
    public static class Attributes {
        private int amount;
        private String currency;
        private String status;
        private String description;
        private NextAction nextAction;
    }

    @lombok.Data
    public static class NextAction {
        private String redirect;
        private String type;
    }
}