package com.sid.app.model;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DateRange {
    private final LocalDate from;
    private final LocalDate to;

    public DateRange(LocalDate from, LocalDate to) {
        this.from = from;
        this.to = to;
    }
}