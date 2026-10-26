package com.project.pbd.service;

import com.project.pbd.dto.FulllyPaidAcquisitionDto;
import com.project.pbd.dto.DetailedAcquisitionDto;
import com.project.pbd.entity.Client;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final AcquisitionService acquisitionService;

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
