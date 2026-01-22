package com.shopzone.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Embeddable snapshot of an address at order time.
 * Preserves the exact address used when the order was placed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class AddressSnapshot {

  @Column(name = "shipping_full_name")
  private String fullName;

  @Column(name = "shipping_phone_number")
  private String phoneNumber;

  @Column(name = "shipping_address_line1")
  private String addressLine1;

  @Column(name = "shipping_address_line2")
  private String addressLine2;

  @Column(name = "shipping_city")
  private String city;

  @Column(name = "shipping_state")
  private String state;

  @Column(name = "shipping_postal_code")
  private String postalCode;

  @Column(name = "shipping_country")
  private String country;

  @Column(name = "shipping_landmark")
  private String landmark;

  /**
   * Create a snapshot from an Address entity.
   */
  public static AddressSnapshot fromAddress(Address address) {
    if (address == null) {
      return null;
    }

    return AddressSnapshot.builder()
        .fullName(address.getFullName())
        .phoneNumber(address.getPhoneNumber())
        .addressLine1(address.getAddressLine1())
        .addressLine2(address.getAddressLine2())
        .city(address.getCity())
        .state(address.getState())
        .postalCode(address.getPostalCode())
        .country(address.getCountry())
        .landmark(address.getLandmark())
        .build();
  }

  /**
   * Get formatted single-line address.
   */
  public String getFormattedAddress() {
    StringBuilder sb = new StringBuilder();
    sb.append(addressLine1);

    if (addressLine2 != null && !addressLine2.isBlank()) {
      sb.append(", ").append(addressLine2);
    }

    if (landmark != null && !landmark.isBlank()) {
      sb.append(" (").append(landmark).append(")");
    }

    sb.append(", ").append(city);
    sb.append(", ").append(state);
    sb.append(" - ").append(postalCode);
    sb.append(", ").append(country);

    return sb.toString();
  }
}