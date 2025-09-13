package com.sid.app.service;

import com.sid.app.entity.OfficeVisit;
import com.sid.app.enums.VisitType;
import com.sid.app.model.OfficeVisitDTO;
import com.sid.app.repository.OfficeVisitRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OfficeVisitService {

    private final OfficeVisitRepository officeVisitRepo;

    public List<OfficeVisitDTO> getVisitsForUserBetween(Long userId, LocalDate from, LocalDate to) {
        log.debug("getVisitsForUserBetween() userId={} from={} to={}", userId, from, to);
        return officeVisitRepo.findByUserIdAndVisitDateBetweenOrderByVisitDate(userId, from, to)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public OfficeVisitDTO createOrUpdateVisit(OfficeVisitDTO dto) {
        log.info("createOrUpdateVisit() userId={} visitDate={}", dto.getUserId(), dto.getVisitDate());
        List<OfficeVisit> existing = officeVisitRepo.findByUserIdAndVisitDate(dto.getUserId(), dto.getVisitDate());
        OfficeVisit entity;
        if (!existing.isEmpty()) {
            entity = existing.getFirst();
            entity.setVisitType(VisitType.valueOf(dto.getVisitType()));
            entity.setDayOfWeek(dto.getDayOfWeek());
            entity.setNotes(dto.getNotes());
        } else {
            entity = OfficeVisit.builder()
                    .userId(dto.getUserId())
                    .visitDate(dto.getVisitDate())
                    .dayOfWeek(dto.getDayOfWeek())
                    .visitType(VisitType.valueOf(dto.getVisitType()))
                    .notes(dto.getNotes())
                    .build();
        }
        officeVisitRepo.save(entity);
        return toDto(entity);
    }

    @Transactional
    public void deleteVisit(Long id) {
        log.info("deleteVisit() id={}", id);
        if (!officeVisitRepo.existsById(id)) {
            throw new EntityNotFoundException("OfficeVisit not found id: " + id);
        }
        officeVisitRepo.deleteById(id);
    }

    private OfficeVisitDTO toDto(OfficeVisit v) {
        return OfficeVisitDTO.builder()
                .officeVisitId(v.getOfficeVisitId())
                .userId(v.getUserId())
                .visitDate(v.getVisitDate())
                .dayOfWeek(v.getDayOfWeek())
                .visitType(String.valueOf(v.getVisitType()))
                .notes(v.getNotes())
                .build();
    }
}
