package com.shopzone.userservice.dto.request;

import com.shopzone.userservice.model.Role;
import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AdminUserUpdateRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Role role;
    private Boolean enabled;
    private Boolean locked;
    private Boolean emailVerified;
}
