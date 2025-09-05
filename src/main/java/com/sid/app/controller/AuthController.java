package com.sid.app.controller;

import com.sid.app.constants.AppConstants;
import com.sid.app.model.AuthResponse;
import com.sid.app.model.LoginRequest;
import com.sid.app.model.RegisterRequest;
import com.sid.app.model.ForgotPasswordResetRequest;
import com.sid.app.model.ResponseDTO;
import com.sid.app.service.AuthService;
import com.sid.app.utils.ApplicationUtils;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@CrossOrigin
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping(AppConstants.USER_REGISTER_ENDPOINT)
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        log.info("Register request -> {}", ApplicationUtils.getJSONString(request));
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(AppConstants.USER_LOGIN_ENDPOINT)
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        log.info("Login request -> {}", ApplicationUtils.getJSONString(request));
        AuthResponse response = authService.login(request);

        if (AppConstants.STATUS_SUCCESS.equals(response.getStatus())) {
            return ResponseEntity.ok(response);
        } else {
            HttpStatus status = (Boolean.TRUE.equals(response.getAccountLocked()))
                    ? HttpStatus.FORBIDDEN
                    : HttpStatus.UNAUTHORIZED;
            return ResponseEntity.status(status).body(response);
        }
    }

    @PostMapping(AppConstants.FORGOT_PASSWORD_RESET_ENDPOINT)
    public ResponseEntity<ResponseDTO<Void>> resetPassword(@Valid @RequestBody ForgotPasswordResetRequest request) {
        log.info("Reset password for email: {}", request.getEmail());
        return authService.resetPassword(request);
    }

}
