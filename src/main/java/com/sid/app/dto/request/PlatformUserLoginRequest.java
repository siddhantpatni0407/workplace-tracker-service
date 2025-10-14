package com.sid.app.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformUserLoginRequest {

    @NotBlank(message = "Email or mobile number is required")
    private String emailOrMobile;

    @NotBlank(message = "Password is required")
    private String password;
}
