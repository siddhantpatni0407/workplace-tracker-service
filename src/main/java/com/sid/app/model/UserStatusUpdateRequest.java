package com.sid.app.model;

import lombok.Data;

/**
 * Request DTO to update user active/locked status.
 */
@Data
public class UserStatusUpdateRequest {
    private Long userId;
    /**
     * Optional. If provided will set the user's active state.
     * true = active, false = inactive
     */
    private Boolean isActive;

    /**
     * Optional. If provided will set user's locked state.
     * true = locked, false = unlocked
     */
    private Boolean isAccountLocked;
}
