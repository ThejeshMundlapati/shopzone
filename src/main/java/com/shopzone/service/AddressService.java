package com.shopzone.service;

import com.shopzone.dto.request.AddressRequest;
import com.shopzone.dto.response.AddressResponse;
import com.shopzone.exception.BadRequestException;
import com.shopzone.exception.ResourceNotFoundException;
import com.shopzone.model.Address;
import com.shopzone.repository.jpa.AddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressService {

  private final AddressRepository addressRepository;

  private static final int MAX_ADDRESSES_PER_USER = 10;

  /**
   * Get all addresses for user
   */
  public List<AddressResponse> getUserAddresses(String userId) {
    return addressRepository
        .findByUserIdAndActiveTrueOrderByIsDefaultDescCreatedAtDesc(userId)
        .stream()
        .map(AddressResponse::fromAddress)
        .toList();
  }

  /**
   * Get address by ID
   */
  public AddressResponse getAddressById(String userId, String addressId) {
    Address address = addressRepository.findByIdAndUserIdAndActiveTrue(addressId, userId)
        .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
    return AddressResponse.fromAddress(address);
  }

  /**
   * Get default address
   */
  public AddressResponse getDefaultAddress(String userId) {
    Address address = addressRepository.findByUserIdAndIsDefaultTrueAndActiveTrue(userId)
        .orElseThrow(() -> new ResourceNotFoundException("No default address found"));
    return AddressResponse.fromAddress(address);
  }

  /**
   * Create new address
   */
  @Transactional
  public AddressResponse createAddress(String userId, AddressRequest request) {
    long count = addressRepository.countByUserIdAndActiveTrue(userId);
    if (count >= MAX_ADDRESSES_PER_USER) {
      throw new BadRequestException("Maximum " + MAX_ADDRESSES_PER_USER + " addresses allowed per user");
    }

    boolean isFirstAddress = count == 0;
    if (request.isDefault() || isFirstAddress) {
      addressRepository.clearDefaultForUser(userId);
    }

    Address address = Address.builder()
        .userId(userId)
        .fullName(request.getFullName())
        .phoneNumber(request.getPhoneNumber())
        .addressLine1(request.getAddressLine1())
        .addressLine2(request.getAddressLine2())
        .city(request.getCity())
        .state(request.getState())
        .postalCode(request.getPostalCode())
        .country(request.getCountry())
        .landmark(request.getLandmark())
        .addressType(request.getAddressType())
        .isDefault(request.isDefault() || isFirstAddress)
        .active(true)
        .build();

    address = addressRepository.save(address);
    log.info("Created address {} for user {}", address.getId(), userId);

    return AddressResponse.fromAddress(address);
  }

  /**
   * Update address
   */
  @Transactional
  public AddressResponse updateAddress(String userId, String addressId, AddressRequest request) {
    Address address = addressRepository.findByIdAndUserIdAndActiveTrue(addressId, userId)
        .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

    if (request.isDefault() && !address.isDefault()) {
      addressRepository.clearDefaultForUser(userId);
    }

    address.setFullName(request.getFullName());
    address.setPhoneNumber(request.getPhoneNumber());
    address.setAddressLine1(request.getAddressLine1());
    address.setAddressLine2(request.getAddressLine2());
    address.setCity(request.getCity());
    address.setState(request.getState());
    address.setPostalCode(request.getPostalCode());
    address.setCountry(request.getCountry());
    address.setLandmark(request.getLandmark());
    address.setAddressType(request.getAddressType());
    address.setDefault(request.isDefault());

    address = addressRepository.save(address);
    log.info("Updated address {} for user {}", addressId, userId);

    return AddressResponse.fromAddress(address);
  }

  /**
   * Delete address (soft delete)
   */
  @Transactional
  public void deleteAddress(String userId, String addressId) {
    Address address = addressRepository.findByIdAndUserIdAndActiveTrue(addressId, userId)
        .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

    boolean wasDefault = address.isDefault();

    address.setActive(false);
    address.setDefault(false);
    addressRepository.save(address);

    if (wasDefault) {
      addressRepository.findByUserIdAndActiveTrueOrderByIsDefaultDescCreatedAtDesc(userId)
          .stream()
          .findFirst()
          .ifPresent(newDefault -> {
            newDefault.setDefault(true);
            addressRepository.save(newDefault);
          });
    }

    log.info("Deleted address {} for user {}", addressId, userId);
  }

  /**
   * Set address as default
   */
  @Transactional
  public AddressResponse setAsDefault(String userId, String addressId) {
    Address address = addressRepository.findByIdAndUserIdAndActiveTrue(addressId, userId)
        .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

    if (address.isDefault()) {
      return AddressResponse.fromAddress(address);
    }

    addressRepository.clearDefaultForUser(userId);
    address.setDefault(true);
    address = addressRepository.save(address);

    log.info("Set address {} as default for user {}", addressId, userId);
    return AddressResponse.fromAddress(address);
  }
}