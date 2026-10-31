package com.project.pbd.service;

import com.project.pbd.dto.DetailedAcquisitionDto;
import com.project.pbd.dto.MajorClientReportDto;
import com.project.pbd.dto.UnpaidPurchasesReportDto;
import com.project.pbd.entity.Acquisition;
import com.project.pbd.entity.Client;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportServiceTests {
    @Test
    void majorClientsQualifyByFourFullyPaidPurchasesOrStrictlyMoreThanThousand() {
        AcquisitionService acquisitions = mock(AcquisitionService.class);
        when(acquisitions.getAllAcquisitions()).thenReturn(List.of(
                clientPurchase("A", "2024-01-01", "100", "100"),
                clientPurchase("A", "2024-07-01", "100", "100"),
                clientPurchase("A", "2025-06-01", "100", "100"),
                clientPurchase("A", "2025-12-31", "100", "100"),
                clientPurchase("B", "2025-01-01", "1000.01", "1000.01"),
                clientPurchase("C", "2025-01-01", "1000", "1000")));
        List<MajorClientReportDto> result = new ReportService(acquisitions).getMajorClients();
        assertEquals(List.of("A", "B"), result.stream().map(MajorClientReportDto::cnp).toList());
        assertEquals(0, new BigDecimal("400").compareTo(result.getFirst().totalCollectedAmount()));
        assertEquals(LocalDate.of(2024, 1, 1), result.getFirst().periodStart());
        assertEquals(LocalDate.of(2025, 12, 31), result.getFirst().periodEnd());
    }

    @Test
    void majorClientsExcludePartialPaymentsAnywhereAndDoNotCombineDistantYears() {
        AcquisitionService acquisitions = mock(AcquisitionService.class);
        when(acquisitions.getAllAcquisitions()).thenReturn(List.of(
                clientPurchase("A", "2020-01-01", "1500", "1500"),
                clientPurchase("A", "2026-01-01", "100", "1"),
                clientPurchase("B", "2020-01-01", "600", "600"),
                clientPurchase("B", "2022-01-02", "600", "600")));
        assertTrue(new ReportService(acquisitions).getMajorClients().isEmpty());
    }

    @Test
    void majorClientsChooseHighestCollectedQualifyingWindowWithoutCountingAdvanceTwice() {
        AcquisitionService acquisitions = mock(AcquisitionService.class);
        when(acquisitions.getAllAcquisitions()).thenReturn(List.of(
                clientPurchase("A", "2020-01-01", "1100", "1100"),
                clientPurchase("A", "2025-01-01", "1200", "1200"),
                clientPurchase("A", "2025-02-01", "100", "0")));
        MajorClientReportDto result = new ReportService(acquisitions).getMajorClients().getFirst();
        assertEquals(0, new BigDecimal("1200").compareTo(result.totalCollectedAmount()));
        assertEquals(LocalDate.of(2025, 1, 1), result.periodStart());
    }

    @Test
    void mostUnpaidIncludesTiesAndUsesWeightedPaymentRateAcrossAllPurchases() {
        AcquisitionService acquisitions = mock(AcquisitionService.class);
        when(acquisitions.getAllAcquisitions()).thenReturn(List.of(
                clientPurchase("A", "2025-01-01", "100", "0"),
                clientPurchase("A", "2025-01-01", "200", "100"),
                clientPurchase("A", "2025-01-01", "100", "100"),
                clientPurchase("B", "2025-01-01", "100", "0"),
                clientPurchase("B", "2025-01-01", "200", "100"),
                clientPurchase("C", "2025-01-01", "1000", "1")));
        List<UnpaidPurchasesReportDto> result = new ReportService(acquisitions).getClientsWithMostUnpaidProducts();
        assertEquals(List.of("A", "B"), result.stream().map(UnpaidPurchasesReportDto::cnp).toList());
        assertEquals(2, result.getFirst().unpaidProductCount());
        assertEquals(new BigDecimal("50.00"), result.getFirst().paymentRate());
        assertEquals(new BigDecimal("33.33"), result.get(1).paymentRate());
    }

    @Test
    void noOutstandingClientsWhenEverythingIsPaidAndEmptyReportsAreSupported() {
        AcquisitionService acquisitions = mock(AcquisitionService.class);
        ReportService service = new ReportService(acquisitions);
        when(acquisitions.getAllAcquisitions()).thenReturn(List.of(
                clientPurchase("A", "2025-01-01", "100", "100")));
        assertTrue(service.getClientsWithMostUnpaidProducts().isEmpty());
        when(acquisitions.getAllAcquisitions()).thenReturn(List.of());
        assertTrue(service.getClientsWithMostUnpaidProducts().isEmpty());
        assertTrue(service.getMajorClients().isEmpty());
    }

    private Acquisition clientPurchase(String cnp, String date, String price, String paid) {
        Acquisition acquisition = purchase("Same", "Name", date, "Product", paid);
        acquisition.getClient().setCNP(cnp);
        acquisition.setPrice(new BigDecimal(price));
        return acquisition;
    }

    @Test
    void includesAllPurchasesAndSortsByNameSurnameDateThenDescendingDebt() {
        AcquisitionService acquisitions = mock(AcquisitionService.class);
        when(acquisitions.getAllAcquisitions()).thenReturn(List.of(
                purchase("Z", "A", "2026-01-01", "last", "0"),
                purchase("A", "B", "2026-01-01", "surname", "10"),
                purchase("A", "A", "2026-02-01", "later", "0"),
                purchase("A", "A", "2026-01-01", "paid", "100"),
                purchase("A", "A", "2026-01-01", "partial", "30"),
                purchase("A", "A", "2026-01-01", "unpaid", "0")));

        List<DetailedAcquisitionDto> report = new ReportService(acquisitions).getDetailedAcquisitions();

        assertEquals(List.of("unpaid", "partial", "paid", "later", "surname", "last"),
                report.stream().map(DetailedAcquisitionDto::product).toList());
        assertEquals(new BigDecimal("70"), report.get(1).remainingAmount());
        assertEquals(BigDecimal.ZERO, report.get(2).remainingAmount());
        assertEquals("1234567890123", report.get(0).cnp());
        assertEquals(new BigDecimal("100"), report.get(0).price());
        assertEquals(LocalDate.of(2026, 1, 1), report.get(0).acquisitionDate());
    }

    @Test
    void returnsEmptyReportWhenThereAreNoPurchases() {
        AcquisitionService acquisitions = mock(AcquisitionService.class);
        when(acquisitions.getAllAcquisitions()).thenReturn(List.of());
        assertTrue(new ReportService(acquisitions).getDetailedAcquisitions().isEmpty());
    }

    private Acquisition purchase(String name, String surname, String date, String product, String paid) {
        Client client = new Client();
        client.setName(name);
        client.setSurname(surname);
        client.setCNP("1234567890123");
        Acquisition acquisition = new Acquisition();
        acquisition.setClient(client);
        acquisition.setProduct(product);
        acquisition.setAcquisitionDate(LocalDate.parse(date));
        acquisition.setPrice(new BigDecimal("100"));
        acquisition.setCollectedAmount(new BigDecimal(paid));
        return acquisition;
    }
}
