package com.shopzone.userservice.dto.response;

import com.shopzone.userservice.model.Address;
import lombok.*;

import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AddressResponse {
    private String id;
    private String userId;
    private String fullName;
    private String phoneNumber;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;
    private String landmark;
    private String addressType;
    private boolean isDefault;
    private String formattedAddress;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AddressResponse fromAddress(Address a) {
        return AddressResponse.builder()
            .id(a.getId()).userId(a.getUserId()).fullName(a.getFullName())
            .phoneNumber(a.getPhoneNumber()).addressLine1(a.getAddressLine1())
            .addressLine2(a.getAddressLine2()).city(a.getCity()).state(a.getState())
            .postalCode(a.getPostalCode()).country(a.getCountry()).landmark(a.getLandmark())
            .addressType(a.getAddressType().name()).isDefault(a.isDefault())
            .formattedAddress(a.getFormattedAddress())
            .createdAt(a.getCreatedAt()).updatedAt(a.getUpdatedAt())
            .build();
    }
}
