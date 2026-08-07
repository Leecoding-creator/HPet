package com.hpet.posture.dto;

import java.time.LocalDate;

public class PostureSummaryResponse {
    private final LocalDate date;
    private final long count;

    public PostureSummaryResponse(LocalDate date, long count) {
        this.date = date;
        this.count = count;
    }

    public LocalDate getDate() { return date; }
    public long getCount() { return count; }
}
