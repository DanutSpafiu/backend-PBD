package com.project.pbd.dto;

import java.math.BigDecimal;

public record UnpaidPurchasesReportDto(String name, String surname, String cnp,
                                   long unpaidProductCount, BigDecimal paymentRate) {
}
