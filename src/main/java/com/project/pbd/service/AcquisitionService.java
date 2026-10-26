package com.project.pbd.service;

import com.project.pbd.entity.Acquisition;
import com.project.pbd.entity.Client;
import com.project.pbd.repository.AcquisitionRepository;
import com.project.pbd.repository.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class AcquisitionService {

    private final AcquisitionRepository acquisitionRepository;
    private final ClientRepository clientRepository;

    public List<Acquisition> getAllAcquisitions() {
        return acquisitionRepository.findAll();
    }

    @Transactional
    public void sellProduct(String cnp, String product, LocalDate acquisitionDate, BigDecimal price, BigDecimal collectedAmount) {
        Client client = clientRepository.findByCNP(cnp)
                .orElseThrow(() -> new IllegalArgumentException("Client not found"));

        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }

        if (collectedAmount == null || collectedAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Collected amount must be non-negative");
        }

        BigDecimal available = client.getAvailable();
        BigDecimal total = available.add(collectedAmount);
        BigDecimal paidAmount = total.min(price);
        client.setAvailable(total.subtract(paidAmount));
        createAndSaveAcquisition(client, product, acquisitionDate, price, paidAmount);
    }

    public void saveAcquisition(Acquisition acquisition) {
        acquisitionRepository.save(acquisition);
    }

    private void createAndSaveAcquisition(
            Client client,
            String product,
            LocalDate acquisitionDate,
            BigDecimal price,
            BigDecimal collectedAmount) {

        Acquisition acquisition = new Acquisition();

        acquisition.setClient(client);
        acquisition.setProduct(product);
        acquisition.setAcquisitionDate(acquisitionDate);
        acquisition.setPrice(price);
        acquisition.setCollectedAmount(collectedAmount);

        saveAcquisition(acquisition);
    }

}
