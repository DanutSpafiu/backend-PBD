package com.project.pbd.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DetailedAcquisitionDto(
        String name,
        String surname,
        String cnp,
        String product,
        LocalDate acquisitionDate,
        BigDecimal price,
        BigDecimal remainingAmount
) {
}
