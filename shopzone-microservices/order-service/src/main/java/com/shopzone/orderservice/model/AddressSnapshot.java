package com.shopzone.orderservice.model;

import jakarta.persistence.*;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor @Embeddable
public class AddressSnapshot {
    @Column(name = "shipping_full_name") private String fullName;
    @Column(name = "shipping_phone_number") private String phoneNumber;
    @Column(name = "shipping_address_line1") private String addressLine1;
    @Column(name = "shipping_address_line2") private String addressLine2;
    @Column(name = "shipping_city") private String city;
    @Column(name = "shipping_state") private String state;
    @Column(name = "shipping_postal_code") private String postalCode;
    @Column(name = "shipping_country") private String country;
    @Column(name = "shipping_landmark") private String landmark;

    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder(addressLine1);
        if (addressLine2 != null && !addressLine2.isBlank()) sb.append(", ").append(addressLine2);
        if (landmark != null && !landmark.isBlank()) sb.append(" (").append(landmark).append(")");
        sb.append(", ").append(city).append(", ").append(state).append(" - ").append(postalCode).append(", ").append(country);
        return sb.toString();
    }
}
