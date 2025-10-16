package com.sid.app.auth;

import com.sid.app.entity.PlatformUser;
import com.sid.app.entity.User;
import com.sid.app.entity.UserRole;
import com.sid.app.repository.PlatformUserRepository;
import com.sid.app.repository.UserRepository;
import com.sid.app.repository.UserRoleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final PlatformUserRepository platformUserRepository;

    public CustomUserDetailsService(UserRepository userRepository,
                                    UserRoleRepository userRoleRepository,
                                    PlatformUserRepository platformUserRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.platformUserRepository = platformUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Loading user by email: {}", email);

        // First, try to find platform user
        Optional<PlatformUser> platformUser = platformUserRepository.findByEmail(email);
        if (platformUser.isPresent()) {
            log.debug("Found platform user: {}", email);
            return createUserDetailsFromPlatformUser(platformUser.get());
        }

        // If not found, try to find regular user
        Optional<User> regularUser = userRepository.findByEmail(email);
        if (regularUser.isPresent()) {
            log.debug("Found regular user: {}", email);
            return createUserDetailsFromRegularUser(regularUser.get());
        }

        log.warn("User not found with email: {}", email);
        throw new UsernameNotFoundException("User not found with email: " + email);
    }

    private UserDetails createUserDetailsFromPlatformUser(PlatformUser platformUser) {
        // Platform users always have PLATFORM_USER role
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_PLATFORM_USER");

        return org.springframework.security.core.userdetails.User.builder()
                .username(platformUser.getEmail())
                .password(platformUser.getPassword()) // must be encoded
                .authorities(Collections.singleton(authority))
                .disabled(!Boolean.TRUE.equals(platformUser.getIsActive()))
                .accountLocked(false) // Platform users don't have account locking
                .build();
    }

    private UserDetails createUserDetailsFromRegularUser(User user) {
        // Resolve role name from roleId for regular users
        String roleName = userRoleRepository.findById(user.getRoleId())
                .map(UserRole::getRole)
                .orElse("USER");

        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + roleName.toUpperCase());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword()) // must be encoded
                .authorities(Collections.singleton(authority))
                .disabled(!Boolean.TRUE.equals(user.getIsActive()))
                .accountLocked(Boolean.TRUE.equals(user.getAccountLocked()))
                .build();
    }
}
