package com.sid.app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for platform statistics response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformStatsDTO {

    private Long totalTenants;
    private Long totalSuperAdmins;
    private Long totalAdmins;
    private Long totalUsers;
    private Long totalTenantUsers;
    private List<TenantStatsDTO> tenantStats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TenantStatsDTO {
        private Long tenantId;
        private String tenantName;
        private Long superAdminCount;
        private Long adminCount;
        private Long userCount;
        private Long totalTenantUsers;
    }
}
