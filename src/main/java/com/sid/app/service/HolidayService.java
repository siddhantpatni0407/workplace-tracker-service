package com.sid.app.service;

import com.sid.app.entity.Holiday;
import com.sid.app.enums.HolidayType;
import com.sid.app.model.HolidayDTO;
import com.sid.app.repository.HolidayRepository;
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
public class HolidayService {

    private final HolidayRepository holidayRepo;

    public List<HolidayDTO> getAllHolidays() {
        log.debug("getAllHolidays()");
        return holidayRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<HolidayDTO> getHolidaysBetween(LocalDate from, LocalDate to) {
        log.debug("getHolidaysBetween() from={} to={}", from, to);
        return holidayRepo.findByHolidayDateBetween(from, to).stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    public HolidayDTO createHoliday(HolidayDTO dto) {
        log.info("createHoliday() name={} date={}", dto.getName(), dto.getHolidayDate());
        Holiday h = Holiday.builder()
                .holidayDate(dto.getHolidayDate())
                .name(dto.getName())
                .holidayType(HolidayType.valueOf(dto.getHolidayType()))
                .description(dto.getDescription())
                .build();
        holidayRepo.save(h);
        return toDto(h);
    }

    @Transactional
    public void deleteHoliday(Long holidayId) {
        log.info("deleteHoliday() holidayId={}", holidayId);
        if (!holidayRepo.existsById(holidayId)) {
            throw new EntityNotFoundException("Holiday not found id: " + holidayId);
        }
        holidayRepo.deleteById(holidayId);
    }

    private HolidayDTO toDto(Holiday h) {
        return HolidayDTO.builder()
                .holidayId(h.getHolidayId())
                .holidayDate(h.getHolidayDate())
                .name(h.getName())
                .holidayType(String.valueOf(h.getHolidayType()))
                .description(h.getDescription())
                .build();
    }
}
