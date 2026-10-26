package com.project.pbd.service;

import com.project.pbd.dto.FulllyPaidAcquisitionDto;
import com.project.pbd.entity.Client;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportService {
    private final AcquisitionService acquisitionService;

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
