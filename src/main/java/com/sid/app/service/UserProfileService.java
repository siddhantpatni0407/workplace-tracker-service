package com.sid.app.service;

import com.sid.app.model.UserProfileDTO;

public interface UserProfileService {
    UserProfileDTO getProfile(Long userId);

    UserProfileDTO upsertProfile(UserProfileDTO dto);
}
