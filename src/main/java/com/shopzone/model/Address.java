package com.shopzone.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false)
  private String userId;

  @Column(nullable = false)
  private String fullName;

  @Column(nullable = false)
  private String phoneNumber;

  @Column(nullable = false)
  private String addressLine1;

  private String addressLine2;

  @Column(nullable = false)
  private String city;

  @Column(nullable = false)
  private String state;

  @Column(nullable = false)
  private String postalCode;

  @Column(nullable = false)
  private String country;

  private String landmark;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private AddressType addressType = AddressType.HOME;

  @Column(nullable = false)
  @Builder.Default
  private boolean isDefault = false;

  @Column(nullable = false)
  @Builder.Default
  private boolean active = true;

  @CreationTimestamp
  private LocalDateTime createdAt;

  @UpdateTimestamp
  private LocalDateTime updatedAt;

  public enum AddressType {
    HOME,
    WORK,
    OTHER
  }

  /**
   * Get formatted full address
   */
  public String getFormattedAddress() {
    StringBuilder sb = new StringBuilder();
    sb.append(addressLine1);
    if (addressLine2 != null && !addressLine2.isEmpty()) {
      sb.append(", ").append(addressLine2);
    }
    if (landmark != null && !landmark.isEmpty()) {
      sb.append(" (").append(landmark).append(")");
    }
    sb.append(", ").append(city);
    sb.append(", ").append(state);
    sb.append(" - ").append(postalCode);
    sb.append(", ").append(country);
    return sb.toString();
  }
}