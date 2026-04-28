package com.shopzone.userservice.service;

import com.shopzone.common.exception.BadRequestException;
import com.shopzone.common.exception.ResourceNotFoundException;
import com.shopzone.userservice.dto.request.AddressRequest;
import com.shopzone.userservice.dto.response.AddressResponse;
import com.shopzone.userservice.model.Address;
import com.shopzone.userservice.repository.AddressRepository;
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
    private static final int MAX_ADDRESSES = 10;

    public List<AddressResponse> getUserAddresses(String userId) {
        return addressRepository.findByUserIdAndActiveTrueOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream().map(AddressResponse::fromAddress).toList();
    }

    public AddressResponse getAddressById(String userId, String addressId) {
        Address a = addressRepository.findByIdAndUserIdAndActiveTrue(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        return AddressResponse.fromAddress(a);
    }

    public AddressResponse getDefaultAddress(String userId) {
        Address a = addressRepository.findByUserIdAndIsDefaultTrueAndActiveTrue(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No default address found"));
        return AddressResponse.fromAddress(a);
    }

    @Transactional
    public AddressResponse createAddress(String userId, AddressRequest req) {
        long count = addressRepository.countByUserIdAndActiveTrue(userId);
        if (count >= MAX_ADDRESSES) throw new BadRequestException("Maximum " + MAX_ADDRESSES + " addresses");

        boolean isFirst = count == 0;
        if (req.isDefault() || isFirst) addressRepository.clearDefaultForUser(userId);

        Address address = Address.builder()
                .userId(userId).fullName(req.getFullName()).phoneNumber(req.getPhoneNumber())
                .addressLine1(req.getAddressLine1()).addressLine2(req.getAddressLine2())
                .city(req.getCity()).state(req.getState()).postalCode(req.getPostalCode())
                .country(req.getCountry()).landmark(req.getLandmark())
                .addressType(req.getAddressType()).isDefault(req.isDefault() || isFirst).active(true)
                .build();

        address = addressRepository.save(address);
        return AddressResponse.fromAddress(address);
    }

    @Transactional
    public AddressResponse updateAddress(String userId, String addressId, AddressRequest req) {
        Address address = addressRepository.findByIdAndUserIdAndActiveTrue(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));

        if (req.isDefault() && !address.isDefault()) addressRepository.clearDefaultForUser(userId);

        address.setFullName(req.getFullName());
        address.setPhoneNumber(req.getPhoneNumber());
        address.setAddressLine1(req.getAddressLine1());
        address.setAddressLine2(req.getAddressLine2());
        address.setCity(req.getCity());
        address.setState(req.getState());
        address.setPostalCode(req.getPostalCode());
        address.setCountry(req.getCountry());
        address.setLandmark(req.getLandmark());
        address.setAddressType(req.getAddressType());
        address.setDefault(req.isDefault());

        return AddressResponse.fromAddress(addressRepository.save(address));
    }

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
                    .stream().findFirst().ifPresent(newDefault -> {
                        newDefault.setDefault(true);
                        addressRepository.save(newDefault);
                    });
        }
    }

    @Transactional
    public AddressResponse setAsDefault(String userId, String addressId) {
        Address address = addressRepository.findByIdAndUserIdAndActiveTrue(addressId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        if (address.isDefault()) return AddressResponse.fromAddress(address);

        addressRepository.clearDefaultForUser(userId);
        address.setDefault(true);
        return AddressResponse.fromAddress(addressRepository.save(address));
    }
}
