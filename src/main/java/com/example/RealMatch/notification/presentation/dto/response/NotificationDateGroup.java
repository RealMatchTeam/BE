package com.example.RealMatch.notification.presentation.dto.response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public record NotificationDateGroup(
        LocalDate date,
        String label,
        int count
) {
    public static NotificationDateGroup of(LocalDate date, int count, DateTimeFormatter formatter) {
        return new NotificationDateGroup(
                date,
                date.format(formatter),
                count
        );
    }
}
