package com.sid.app.repository;

import com.sid.app.entity.OfficeVisit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface OfficeVisitRepository extends JpaRepository<OfficeVisit, Long> {

    List<OfficeVisit> findByUserIdAndVisitDateBetweenOrderByVisitDate(Long userId, LocalDate from, LocalDate to);

    List<OfficeVisit> findByUserIdAndVisitDate(Long userId, LocalDate visitDate);

    List<OfficeVisit> findByUserIdAndVisitDateBetween(Long userId, LocalDate from, LocalDate to);

    List<OfficeVisit> findByVisitDateBetween(LocalDate from, LocalDate to); // for org-wide

}
