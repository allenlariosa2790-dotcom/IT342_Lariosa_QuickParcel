package edu.cit.lariosa.quickparcel.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class CreateDeliveryRequest {

    @Valid
    @NotNull(message = "Parcel details are required")
    private ParcelInfo parcel;

    @NotBlank(message = "Pickup address is required")
    private String pickupAddress;

    @NotBlank(message = "Dropoff address is required")
    private String dropoffAddress;

    private String notes;
    private LocalDateTime scheduledTime;

    private String paymentMethod;
    private String paymentStatus;

    // Getters and setters
    public ParcelInfo getParcel() { return parcel; }
    public void setParcel(ParcelInfo parcel) { this.parcel = parcel; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public String getDropoffAddress() { return dropoffAddress; }
    public void setDropoffAddress(String dropoffAddress) { this.dropoffAddress = dropoffAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public String getPaymentMethod(){return paymentMethod;}
    public void setPaymentMethod(String paymentMethod){this.paymentMethod = paymentMethod;}

    public String getPaymentStatus(){return paymentStatus;}
    public void setPaymentStatus(String paymentStatus){this.paymentStatus = paymentStatus;}
    // Inner class for parcel details
    public static class ParcelInfo {
        @NotBlank(message = "Parcel name is required")
        private String name;

        private String description;

        @NotNull(message = "Weight is required")
        private Double weight;

        @NotBlank(message = "Size is required")
        private String size;

        @NotBlank(message = "Category is required")
        private String category;

        private Boolean isFragile = false;

        // Getters and setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public Double getWeight() { return weight; }
        public void setWeight(Double weight) { this.weight = weight; }

        public String getSize() { return size; }
        public void setSize(String size) { this.size = size; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public Boolean getIsFragile() { return isFragile; }
        public void setIsFragile(Boolean isFragile) { this.isFragile = isFragile; }
    }
}