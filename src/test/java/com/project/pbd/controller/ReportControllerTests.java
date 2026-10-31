package com.project.pbd.controller;

import com.project.pbd.dto.MajorClientReportDto;
import com.project.pbd.dto.UnpaidPurchasesReportDto;
import com.project.pbd.service.ReportService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReportControllerTests {
    @Test
    void exposesBothNewReportsWithCamelCaseEndpoints() throws Exception {
        ReportService service = mock(ReportService.class);
        when(service.getMajorClients()).thenReturn(List.of(new MajorClientReportDto("Ion", "Adi",
                "1234567890123", new BigDecimal("1200"), LocalDate.of(2024, 1, 1), LocalDate.of(2025, 1, 1))));
        when(service.getClientsWithMostUnpaidProducts()).thenReturn(List.of(new UnpaidPurchasesReportDto(
                "Ion", "Adi", "1234567890123", 2, new BigDecimal("50.00"))));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ReportController(service)).build();
        mvc.perform(get("/api/rapoarte/clientiMajori"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].totalCollectedAmount").value(1200));
        mvc.perform(get("/api/rapoarte/clientiCuCeleMaiMulteRestante"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].unpaidProductCount").value(2))
                .andExpect(jsonPath("$[0].paymentRate").value(50));
    }
}
