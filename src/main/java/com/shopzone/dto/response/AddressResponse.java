// src/main/java/com/shopzone/dto/response/AddressResponse.java

package com.shopzone.dto.response;

import com.shopzone.model.Address;
import com.shopzone.model.Address.AddressType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
  private AddressType addressType;
  private boolean isDefault;
  private String formattedAddress;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static AddressResponse fromAddress(Address address) {
    return AddressResponse.builder()
        .id(address.getId())
        .userId(address.getUserId())
        .fullName(address.getFullName())
        .phoneNumber(address.getPhoneNumber())
        .addressLine1(address.getAddressLine1())
        .addressLine2(address.getAddressLine2())
        .city(address.getCity())
        .state(address.getState())
        .postalCode(address.getPostalCode())
        .country(address.getCountry())
        .landmark(address.getLandmark())
        .addressType(address.getAddressType())
        .isDefault(address.isDefault())
        .formattedAddress(address.getFormattedAddress())
        .createdAt(address.getCreatedAt())
        .updatedAt(address.getUpdatedAt())
        .build();
  }
}