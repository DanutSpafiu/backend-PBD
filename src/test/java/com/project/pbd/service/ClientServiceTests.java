package com.project.pbd.service;

import com.project.pbd.entity.Acquisition;
import com.project.pbd.entity.Client;
import com.project.pbd.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientServiceTests {
    private final ClientRepository clients = mock(ClientRepository.class);
    private final ClientService service = new ClientService(clients);

    @Test
    void sumsDebtsForAllMatchingPurchasesAndExcludesOtherProducts() {
        Client client = new Client();
        client.setAcquisitions(List.of(purchase("Laptop", "100", "30"),
                purchase("Laptop", "200", "150"), purchase("Laptop", "50", "50"),
                purchase("Telefon", "500", "0")));
        when(clients.findByCNP("1234567890123")).thenReturn(Optional.of(client));

        assertEquals(0, new BigDecimal("120").compareTo(service.leftToPay("1234567890123", "Laptop")));
    }

    @Test
    void fullyPaidProductReturnsZero() {
        Client client = new Client();
        client.setAcquisitions(List.of(purchase("Laptop", "100.00", "100")));
        when(clients.findByCNP("1234567890123")).thenReturn(Optional.of(client));
        assertEquals(0, service.leftToPay("1234567890123", "Laptop").signum());
    }

    @Test
    void unknownClientOrUnpurchasedProductReturnsNotFound() {
        when(clients.findByCNP("1234567890123")).thenReturn(Optional.empty());
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service.leftToPay("1234567890123", "Laptop")).getStatusCode());
        when(clients.findByCNP("1234567890123")).thenReturn(Optional.of(new Client()));
        assertEquals(HttpStatus.NOT_FOUND, assertThrows(ResponseStatusException.class,
                () -> service.leftToPay("1234567890123", "Laptop")).getStatusCode());
    }

    @Test
    void rejectsMissingParametersBeforeReadingDatabase() {
        assertThrows(IllegalArgumentException.class, () -> service.leftToPay(null, "Laptop"));
        assertThrows(IllegalArgumentException.class, () -> service.leftToPay(" ", "Laptop"));
        assertThrows(IllegalArgumentException.class, () -> service.leftToPay("1234567890123", null));
        assertThrows(IllegalArgumentException.class, () -> service.leftToPay("1234567890123", " "));
        verifyNoInteractions(clients);
    }

    private Acquisition purchase(String product, String price, String paid) {
        Acquisition acquisition = new Acquisition();
        acquisition.setProduct(product);
        acquisition.setPrice(new BigDecimal(price));
        acquisition.setCollectedAmount(new BigDecimal(paid));
        return acquisition;
    }
}
