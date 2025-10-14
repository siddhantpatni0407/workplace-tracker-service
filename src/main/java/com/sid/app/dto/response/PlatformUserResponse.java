package com.sid.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformUserResponse {

    private Long platformUserId;
    private String name;
    private String email;
    private String mobileNumber;
    private String role;
    private Boolean isActive;
    private Integer loginAttempts;
    private Boolean accountLocked;
    private LocalDateTime createdDate;
    private LocalDateTime modifiedDate;
}
