package com.sid.app.service;

import com.sid.app.dto.request.PlatformUserLoginRequest;
import com.sid.app.dto.request.PlatformUserSignupRequest;
import com.sid.app.dto.response.PlatformUserAuthResponse;
import com.sid.app.dto.response.PlatformUserResponse;

public interface PlatformUserService {

    PlatformUserAuthResponse signup(PlatformUserSignupRequest request);

    PlatformUserAuthResponse login(PlatformUserLoginRequest request);

    PlatformUserResponse getPlatformUserProfile(Long platformUserId);

    PlatformUserAuthResponse refreshToken(String refreshToken);
}
