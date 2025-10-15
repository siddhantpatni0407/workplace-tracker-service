package com.sid.app.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author Siddhant Patni
 */

@Data
public class RegisterRequest {

    @JsonProperty("name")
    private String name;

    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @JsonProperty("email")
    private String email;

    @JsonProperty("password")
    private String password;

    @JsonProperty("role")
    private String role;

    @JsonProperty("tenantCode")
    private String tenantCode;

    @JsonProperty("platformUserCode")
    private String platformUserCode;

    @JsonProperty("tenantUserCode")
    private String tenantUserCode;

    @JsonProperty("adminCode")
    private String adminCode; // Required for USER/MANAGER registration to map them to a specific Admin
}