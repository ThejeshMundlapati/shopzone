package com.shopzone.orderservice.dto.response;
import com.shopzone.orderservice.model.AddressSnapshot;
import lombok.*;
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AddressSnapshotResponse {
    private String fullName, phoneNumber, addressLine1, addressLine2, city, state, postalCode, country, landmark, formattedAddress;
    public static AddressSnapshotResponse from(AddressSnapshot a) {
        return AddressSnapshotResponse.builder().fullName(a.getFullName()).phoneNumber(a.getPhoneNumber())
            .addressLine1(a.getAddressLine1()).addressLine2(a.getAddressLine2()).city(a.getCity())
            .state(a.getState()).postalCode(a.getPostalCode()).country(a.getCountry())
            .landmark(a.getLandmark()).formattedAddress(a.getFormattedAddress()).build();
    }
}
