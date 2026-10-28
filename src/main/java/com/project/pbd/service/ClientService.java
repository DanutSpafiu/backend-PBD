package com.project.pbd.service;

import com.project.pbd.entity.Client;
import com.project.pbd.entity.Acquisition;
import com.project.pbd.repository.ClientRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    @Transactional
    public BigDecimal leftToPay(String cnp, String product) {
        if (cnp == null || cnp.isBlank()) {
            throw new IllegalArgumentException("CNP cannot be blank");
        }
        if (product == null || product.isBlank()) {
            throw new IllegalArgumentException("Product cannot be blank");
        }
        Client client = clientRepository.findByCNP(cnp)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Client not found"));

        List<Acquisition> purchases = client.getAcquisitions().stream()
                .filter(acquisition -> product.equals(acquisition.getProduct()))
                .toList();
        if (purchases.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not purchased by client");
        }

        return purchases.stream()
                .map(acquisition -> acquisition.getPrice().subtract(acquisition.getCollectedAmount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
