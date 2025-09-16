package com.sid.app.repository;

import com.sid.app.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserId(Long userId);

    Optional<UserAddress> findByUserIdAndIsPrimaryTrue(Long userId);
}
