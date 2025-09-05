package com.sid.app.model;

import lombok.Data;

/**
 * @author Siddhant Patni
 */
@Data
public class LoginRequest {

    private String email;
    private String password;

}