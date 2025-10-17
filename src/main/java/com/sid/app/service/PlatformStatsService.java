package com.sid.app.service;

import com.sid.app.constants.AppConstants;
import com.sid.app.entity.Tenant;
import com.sid.app.model.PlatformStatsDTO;
import com.sid.app.repository.TenantRepository;
import com.sid.app.repository.TenantUserRepository;
import com.sid.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for platform statistics operations
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PlatformStatsService {

    private final TenantRepository tenantRepository;
    private final TenantUserRepository tenantUserRepository;
    private final UserRepository userRepository;

    /**
     * Get comprehensive platform statistics
     */
    public PlatformStatsDTO getPlatformStats() {
        log.info("Fetching platform statistics");

        try {
            // Get overall counts using role codes
            Long totalTenants = tenantUserRepository.countDistinctTenants();
            Long totalSuperAdmins = tenantUserRepository.countSuperAdmins(AppConstants.ROLE_CODE_SUPER_ADMIN);
            Long totalAdmins = tenantUserRepository.countAdmins(AppConstants.ROLE_CODE_ADMIN);

            // Count users with USER and MANAGER roles
            List<String> userRoleCodes = Arrays.asList(AppConstants.ROLE_CODE_USER, AppConstants.ROLE_CODE_MANAGER);
            Long totalUsers = tenantUserRepository.countUsers(userRoleCodes);
            Long totalTenantUsers = tenantUserRepository.count();

            log.info("Overall stats - Tenants: {}, Super Admins: {}, Admins: {}, Users: {}, Total Tenant Users: {}",
                    totalTenants, totalSuperAdmins, totalAdmins, totalUsers, totalTenantUsers);

            // Get tenant-wise statistics
            List<PlatformStatsDTO.TenantStatsDTO> tenantStats = getTenantWiseStats();

            return PlatformStatsDTO.builder()
                    .totalTenants(totalTenants)
                    .totalSuperAdmins(totalSuperAdmins)
                    .totalAdmins(totalAdmins)
                    .totalUsers(totalUsers)
                    .totalTenantUsers(totalTenantUsers)
                    .tenantStats(tenantStats)
                    .build();

        } catch (Exception e) {
            log.error("Error fetching platform statistics: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch platform statistics", e);
        }
    }

    /**
     * Get tenant-wise statistics breakdown
     */
    private List<PlatformStatsDTO.TenantStatsDTO> getTenantWiseStats() {
        log.info("Fetching tenant-wise statistics");

        // Get all tenants
        Map<Long, String> tenantMap = tenantRepository.findAll().stream()
                .collect(Collectors.toMap(
                        Tenant::getTenantId,
                        Tenant::getTenantName
                ));

        // Get counts by tenant using role codes
        Map<Long, Long> superAdminCounts = convertToMap(
                tenantUserRepository.countSuperAdminsByTenant(AppConstants.ROLE_CODE_SUPER_ADMIN));
        Map<Long, Long> adminCounts = convertToMap(
                tenantUserRepository.countAdminsByTenant(AppConstants.ROLE_CODE_ADMIN));

        List<String> userRoleCodes = Arrays.asList(AppConstants.ROLE_CODE_USER, AppConstants.ROLE_CODE_MANAGER);
        Map<Long, Long> userCounts = convertToMap(
                tenantUserRepository.countUsersByTenant(userRoleCodes));
        Map<Long, Long> totalCounts = convertToMap(tenantUserRepository.countTotalUsersByTenant());

        // Build tenant stats
        return tenantMap.entrySet().stream()
                .map(entry -> {
                    Long tenantId = entry.getKey();
                    String tenantName = entry.getValue();

                    return PlatformStatsDTO.TenantStatsDTO.builder()
                            .tenantId(tenantId)
                            .tenantName(tenantName)
                            .superAdminCount(superAdminCounts.getOrDefault(tenantId, 0L))
                            .adminCount(adminCounts.getOrDefault(tenantId, 0L))
                            .userCount(userCounts.getOrDefault(tenantId, 0L))
                            .totalTenantUsers(totalCounts.getOrDefault(tenantId, 0L))
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Convert query result to map
     */
    private Map<Long, Long> convertToMap(List<Object[]> results) {
        Map<Long, Long> map = new HashMap<>();
        for (Object[] result : results) {
            Long tenantId = (Long) result[0];
            Long count = (Long) result[1];
            map.put(tenantId, count);
        }
        return map;
    }
}
