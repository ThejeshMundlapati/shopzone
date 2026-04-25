package com.shopzone.userservice.dto.response;

import com.shopzone.userservice.model.User;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserManagementResponse {
    private String id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String role;
    private Boolean emailVerified;
    private Boolean enabled;
    private Boolean locked;
    private LocalDateTime createdAt;
    private long orderCount;
    private BigDecimal totalSpent;
    private long reviewCount;
    private LocalDateTime lastOrderAt;

    public static UserManagementResponse fromEntity(User u) {
        return UserManagementResponse.builder()
            .id(u.getId().toString()).firstName(u.getFirstName()).lastName(u.getLastName())
            .email(u.getEmail()).phone(u.getPhone()).role(u.getRole().name())
            .emailVerified(u.getEmailVerified()).enabled(u.getEnabled()).locked(u.getLocked())
            .createdAt(u.getCreatedAt())
            .build();
    }

    public static UserManagementResponse fromEntityWithStats(User u, long orders, BigDecimal spent,
                                                              long reviews, LocalDateTime lastOrder) {
        UserManagementResponse r = fromEntity(u);
        r.setOrderCount(orders);
        r.setTotalSpent(spent);
        r.setReviewCount(reviews);
        r.setLastOrderAt(lastOrder);
        return r;
    }
}
