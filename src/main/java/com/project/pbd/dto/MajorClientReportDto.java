package com.project.pbd.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MajorClientReportDto(String name, String surname, String cnp,
                             BigDecimal totalCollectedAmount,
                             LocalDate periodStart, LocalDate periodEnd) {
}
