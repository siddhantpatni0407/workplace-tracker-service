package com.sid.app.service;

import com.sid.app.model.UserSettingsDTO;

public interface UserSettingsService {
    UserSettingsDTO getSettings(Long userId);

    UserSettingsDTO upsertSettings(UserSettingsDTO dto);

    void deleteSettings(Long userId);
}
