package com.shopzone.orderservice.client;

import com.shopzone.common.dto.response.ApiResponse;
import com.shopzone.common.dto.response.UserResponse;
import com.shopzone.common.exception.ServiceCommunicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component @Slf4j
public class UserClient {
    private final RestTemplate restTemplate;
    private final String userUrl;
    public UserClient(RestTemplate restTemplate, @Value("${services.user-url}") String url) {
        this.restTemplate = restTemplate; this.userUrl = url;
    }

    public UserResponse getUserById(String userId) {
        try {
            ResponseEntity<ApiResponse<UserResponse>> resp = restTemplate.exchange(
                userUrl + "/api/internal/users/" + userId, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            if (resp.getBody() != null && resp.getBody().isSuccess()) return resp.getBody().getData();
            throw new ServiceCommunicationException("user-service", "User not found: " + userId);
        } catch (ServiceCommunicationException e) { throw e; }
        catch (Exception e) { throw new ServiceCommunicationException("user-service", e.getMessage(), e); }
    }

    public UserResponse getUserByEmail(String email) {
        try {
            ResponseEntity<ApiResponse<UserResponse>> resp = restTemplate.exchange(
                userUrl + "/api/internal/users/email/" + email, HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            if (resp.getBody() != null && resp.getBody().isSuccess()) return resp.getBody().getData();
            throw new ServiceCommunicationException("user-service", "User not found: " + email);
        } catch (ServiceCommunicationException e) { throw e; }
        catch (Exception e) { throw new ServiceCommunicationException("user-service", e.getMessage(), e); }
    }

    /** Get address from User Service */
    public Object getAddress(String userId, String addressId) {
        try {
            ResponseEntity<ApiResponse<Object>> resp = restTemplate.exchange(
                userUrl + "/api/internal/users/" + userId + "/addresses/" + addressId,
                HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
            if (resp.getBody() != null && resp.getBody().isSuccess()) return resp.getBody().getData();
            return null;
        } catch (Exception e) { throw new ServiceCommunicationException("user-service", "Address not found", e); }
    }
}
