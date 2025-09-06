package com.sid.app.auth;

import com.sid.app.entity.User;
import com.sid.app.entity.UserRole;
import com.sid.app.repository.UserRepository;
import com.sid.app.repository.UserRoleRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    public CustomUserDetailsService(UserRepository userRepository,
                                    UserRoleRepository userRoleRepository) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // resolve role name from roleId
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
