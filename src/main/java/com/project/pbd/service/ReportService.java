package com.project.pbd.service;

import com.project.pbd.dto.FulllyPaidAcquisitionDto;
import com.project.pbd.dto.DetailedAcquisitionDto;
import com.project.pbd.dto.MajorClientReportDto;
import com.project.pbd.dto.UnpaidPurchasesReportDto;
import com.project.pbd.entity.Acquisition;
import com.project.pbd.entity.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final AcquisitionService acquisitionService;

    public List<MajorClientReportDto> getMajorClients() {
        List<MajorClientReportDto> result = new ArrayList<>();
        for (List<Acquisition> purchases : purchasesByClient().values()) {
            boolean hasPartialPayment = purchases.stream().anyMatch(purchase ->
                    purchase.getCollectedAmount().signum() > 0
                            && purchase.getCollectedAmount().compareTo(purchase.getPrice()) < 0);
            if (hasPartialPayment) {
                continue;
            }
            List<Acquisition> sorted = purchases.stream()
                    .sorted(Comparator.comparing(Acquisition::getAcquisitionDate)).toList();
            MajorClientReportDto bestPeriod = null;
            List<LocalDate> starts = sorted.stream()
                    .map(purchase -> purchase.getAcquisitionDate().withDayOfYear(1)).distinct().toList();
            for (LocalDate start : starts) {
                LocalDate limit = start.plusYears(2).minusDays(1);
                List<Acquisition> window = sorted.stream()
                        .filter(purchase -> !purchase.getAcquisitionDate().isBefore(start)
                                && !purchase.getAcquisitionDate().isAfter(limit)).toList();
                long fullyPaid = window.stream().filter(purchase ->
                        purchase.getCollectedAmount().compareTo(purchase.getPrice()) == 0).count();
                BigDecimal value = window.stream().map(Acquisition::getPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (fullyPaid < 4 && value.compareTo(new BigDecimal("1000")) <= 0) {
                    continue;
                }
                BigDecimal collected = window.stream().map(Acquisition::getCollectedAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                if (bestPeriod == null || collected.compareTo(bestPeriod.totalCollectedAmount()) > 0) {
                    Client client = sorted.getFirst().getClient();
                    bestPeriod = new MajorClientReportDto(client.getName(), client.getSurname(), client.getCNP(),
                            collected, start, limit);
                }
            }
            if (bestPeriod != null) {
                result.add(bestPeriod);
            }
        }
        return result.stream().sorted(Comparator.comparing(MajorClientReportDto::name)
                .thenComparing(MajorClientReportDto::surname).thenComparing(MajorClientReportDto::cnp)).toList();
    }

    public List<UnpaidPurchasesReportDto> getClientsWithMostUnpaidProducts() {
        List<UnpaidPurchasesReportDto> candidates = new ArrayList<>();
        long maximum = 0;
        for (List<Acquisition> purchases : purchasesByClient().values()) {
            long count = purchases.stream().filter(purchase ->
                    purchase.getCollectedAmount().compareTo(purchase.getPrice()) < 0).count();
            if (count == 0 || count < maximum) {
                continue;
            }
            if (count > maximum) {
                candidates.clear();
                maximum = count;
            }
            BigDecimal totalPrice = purchases.stream().map(Acquisition::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal totalPaid = purchases.stream().map(Acquisition::getCollectedAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal rate = totalPaid.multiply(new BigDecimal("100"))
                    .divide(totalPrice, 2, RoundingMode.HALF_UP);
            Client client = purchases.getFirst().getClient();
            candidates.add(new UnpaidPurchasesReportDto(client.getName(), client.getSurname(),
                    client.getCNP(), count, rate));
        }
        return candidates.stream().sorted(Comparator.comparing(UnpaidPurchasesReportDto::name)
                .thenComparing(UnpaidPurchasesReportDto::surname).thenComparing(UnpaidPurchasesReportDto::cnp)).toList();
    }

    private Map<String, List<Acquisition>> purchasesByClient() {
        return acquisitionService.getAllAcquisitions().stream()
                .collect(Collectors.groupingBy(purchase -> purchase.getClient().getCNP()));
    }

    public List<DetailedAcquisitionDto> getDetailedAcquisitions() {
        return acquisitionService.getAllAcquisitions().stream()
                .map(acquisition -> {
                    Client client = acquisition.getClient();
                    return new DetailedAcquisitionDto(client.getName(), client.getSurname(),
                            client.getCNP(), acquisition.getProduct(), acquisition.getAcquisitionDate(),
                            acquisition.getPrice(),
                            acquisition.getPrice().subtract(acquisition.getCollectedAmount()));
                })
                .sorted(Comparator.comparing(DetailedAcquisitionDto::name)
                        .thenComparing(DetailedAcquisitionDto::surname)
                        .thenComparing(DetailedAcquisitionDto::acquisitionDate)
                        .thenComparing(DetailedAcquisitionDto::remainingAmount, Comparator.reverseOrder()))
                .toList();
    }

    public List<FulllyPaidAcquisitionDto> getFullyPaidAcquisitions() {
        return acquisitionService.getAllAcquisitions()
                .stream()
                .filter(acquisition -> acquisition.getCollectedAmount()
                        .compareTo(acquisition.getPrice()) == 0)
                .map(acquisition -> {
                    Client client = acquisition.getClient();
                    return new FulllyPaidAcquisitionDto(client.getName(), client.getSurname(),
                            client.getCNP(), acquisition.getProduct());
                })
                .toList();
    }

}
