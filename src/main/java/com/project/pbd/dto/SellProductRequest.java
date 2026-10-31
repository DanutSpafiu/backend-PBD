package com.project.pbd.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SellProductRequest(
        @NotBlank @Pattern(regexp = "^[0-9]{13}$")
        String cnp,

        @NotBlank @Size(max = 255)
        String product,

        @NotNull
        LocalDate acquisitionDate,

        @NotNull @DecimalMin(value = "0.00", inclusive = false)
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @NotNull @DecimalMin("0.00")
        @Digits(integer = 10, fraction = 2)
        BigDecimal collectedAmount
) {
}
