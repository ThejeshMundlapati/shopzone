package com.shopzone.userservice.dto.request;

import com.shopzone.userservice.model.Address;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AddressRequest {
    @NotBlank private String fullName;
    @NotBlank private String phoneNumber;
    @NotBlank private String addressLine1;
    private String addressLine2;
    @NotBlank private String city;
    @NotBlank private String state;
    @NotBlank private String postalCode;
    @NotBlank private String country;
    private String landmark;
    @Builder.Default
    private Address.AddressType addressType = Address.AddressType.HOME;
    private boolean isDefault;
}
