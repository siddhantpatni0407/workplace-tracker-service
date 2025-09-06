package com.sid.app.service.impl;

import com.sid.app.entity.UserSettings;
import com.sid.app.exception.ServiceException;
import com.sid.app.model.UserSettingsDTO;
import com.sid.app.repository.UserRepository;
import com.sid.app.repository.UserSettingsRepository;
import com.sid.app.service.UserSettingsService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class UserSettingsServiceImpl implements UserSettingsService {

    private final UserSettingsRepository settingsRepository;
    private final UserRepository userRepository; // validate user existence

    @Override
    @Transactional(readOnly = true)
    public UserSettingsDTO getSettings(Long userId) {
        UserSettings settings = settingsRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User settings not found for userId: " + userId));
        return toDto(settings);
    }

    @Override
    @Transactional
    public UserSettingsDTO upsertSettings(UserSettingsDTO dto) {
        Long userId = dto.getUserId();
        // validate user exists
        userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found with userId: " + userId));

        UserSettings settings = settingsRepository.findByUserId(userId).orElseGet(() -> {
            UserSettings s = new UserSettings();
            s.setUserId(userId);
            return s;
        });

        // timezone
        if (dto.getTimezone() != null && !dto.getTimezone().isBlank()) {
            settings.setTimezone(dto.getTimezone());
        }

        // workWeekStart
        if (dto.getWorkWeekStart() != null) {
            int v = dto.getWorkWeekStart();
            if (v < 1 || v > 7) throw new EntityNotFoundException("workWeekStart must be between 1 and 7");
            settings.setWorkWeekStart(v);
        }

        // language
        if (dto.getLanguage() != null && !dto.getLanguage().isBlank()) {
            settings.setLanguage(dto.getLanguage());
        }

        // date format
        if (dto.getDateFormat() != null && !dto.getDateFormat().isBlank()) {
            settings.setDateFormat(dto.getDateFormat());
        }

        UserSettings saved = settingsRepository.save(settings);
        return toDto(saved);
    }

    @Override
    @Transactional
    public void deleteSettings(Long userId) {
        UserSettings settings = settingsRepository.findByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException("User settings not found for userId: " + userId));
        settingsRepository.delete(settings);
    }

    private UserSettingsDTO toDto(UserSettings s) {
        return UserSettingsDTO.builder()
                .userSettingId(s.getUserSettingId())
                .userId(s.getUserId())
                .timezone(s.getTimezone())
                .workWeekStart(s.getWorkWeekStart())
                .language(s.getLanguage())
                .dateFormat(s.getDateFormat())
                .build();
    }
}
