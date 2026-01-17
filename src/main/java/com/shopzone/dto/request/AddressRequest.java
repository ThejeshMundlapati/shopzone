// src/main/java/com/shopzone/dto/request/AddressRequest.java

package com.shopzone.dto.request;

import com.shopzone.model.Address.AddressType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

  @NotBlank(message = "Full name is required")
  @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
  private String fullName;

  @NotBlank(message = "Phone number is required")
  @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid phone number format")
  private String phoneNumber;

  @NotBlank(message = "Address line 1 is required")
  @Size(max = 200, message = "Address line 1 must not exceed 200 characters")
  private String addressLine1;

  @Size(max = 200, message = "Address line 2 must not exceed 200 characters")
  private String addressLine2;

  @NotBlank(message = "City is required")
  @Size(max = 100, message = "City must not exceed 100 characters")
  private String city;

  @NotBlank(message = "State is required")
  @Size(max = 100, message = "State must not exceed 100 characters")
  private String state;

  @NotBlank(message = "Postal code is required")
  @Pattern(regexp = "^[0-9]{5,10}$", message = "Invalid postal code format")
  private String postalCode;

  @NotBlank(message = "Country is required")
  @Size(max = 100, message = "Country must not exceed 100 characters")
  private String country;

  @Size(max = 200, message = "Landmark must not exceed 200 characters")
  private String landmark;

  @Builder.Default
  private AddressType addressType = AddressType.HOME;

  @Builder.Default
  private boolean isDefault = false;
}