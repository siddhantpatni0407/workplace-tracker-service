package com.sid.app.service;

import com.sid.app.entity.LeavePolicy;
import com.sid.app.model.LeavePolicyDTO;
import com.sid.app.repository.LeavePolicyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeavePolicyService {

    private final LeavePolicyRepository policyRepo;

    public List<LeavePolicyDTO> getAllPolicies() {
        log.debug("getAllPolicies()");
        return policyRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public LeavePolicyDTO getPolicy(Long policyId) {
        log.debug("getPolicy() policyId={}", policyId);
        LeavePolicy p = policyRepo.findById(policyId)
                .orElseThrow(() -> new EntityNotFoundException("LeavePolicy not found with id: " + policyId));
        return toDto(p);
    }

    @Transactional
    public LeavePolicyDTO createPolicy(LeavePolicyDTO dto) {
        log.info("createPolicy() code={}", dto.getPolicyCode());
        LeavePolicy p = LeavePolicy.builder()
                .policyCode(dto.getPolicyCode().trim().toUpperCase())
                .policyName(dto.getPolicyName())
                .defaultAnnualDays(dto.getDefaultAnnualDays() == null ? 0 : dto.getDefaultAnnualDays())
                .description(dto.getDescription())
                .build();
        policyRepo.save(p);
        return toDto(p);
    }

    @Transactional
    public LeavePolicyDTO updatePolicy(Long policyId, LeavePolicyDTO dto) {
        log.info("updatePolicy() policyId={}", policyId);
        LeavePolicy p = policyRepo.findById(policyId)
                .orElseThrow(() -> new EntityNotFoundException("LeavePolicy not found with id: " + policyId));
        if (dto.getPolicyName() != null) p.setPolicyName(dto.getPolicyName());
        if (dto.getDefaultAnnualDays() != null) p.setDefaultAnnualDays(dto.getDefaultAnnualDays());
        if (dto.getDescription() != null) p.setDescription(dto.getDescription());
        policyRepo.save(p);
        return toDto(p);
    }

    private LeavePolicyDTO toDto(LeavePolicy p) {
        return LeavePolicyDTO.builder()
                .policyId(p.getPolicyId())
                .policyCode(p.getPolicyCode())
                .policyName(p.getPolicyName())
                .defaultAnnualDays(p.getDefaultAnnualDays())
                .description(p.getDescription())
                .build();
    }
}
