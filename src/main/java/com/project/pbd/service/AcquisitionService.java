package com.project.pbd.service;

import com.project.pbd.entity.Acquisition;
import com.project.pbd.repository.AcquisitionRepository;
import com.project.pbd.repository.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AcquisitionService {

    private final AcquisitionRepository acquisitionRepository;
    private final ClientRepository clientRepository;

    public List<Acquisition> getAllAcquisitions() {
        return acquisitionRepository.findAll();
    }

    @Transactional
    public void sellProduct(String cnp, String product, LocalDate acquisitionDate, BigDecimal price, BigDecimal collectedAmount) {
        clientRepository.findByCNP(cnp)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        if (collectedAmount == null || collectedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Collected amount must be non-negative");
        }

        acquisitionRepository.sellProduct(cnp, product, acquisitionDate, price, collectedAmount);
    }

}
