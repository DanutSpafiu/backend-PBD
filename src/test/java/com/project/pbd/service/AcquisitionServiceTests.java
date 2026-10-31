package com.project.pbd.service;

import com.project.pbd.entity.Client;
import com.project.pbd.repository.AcquisitionRepository;
import com.project.pbd.repository.ClientRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AcquisitionServiceTests {
    @ParameterizedTest
    @CsvSource({
            "30, 50, 100",
            "100, 0, 80",
            "30, 100, 100",
            "30, 70, 100",
            "0, 0, 100"
    })
    void passesRawPaymentToDatabaseWithoutApplyingCreditTwice(String available, String collected,
                                                   String price) {
        ClientRepository clients = mock(ClientRepository.class);
        AcquisitionRepository acquisitions = mock(AcquisitionRepository.class);
        AcquisitionService service = new AcquisitionService(acquisitions, clients);
        Client client = new Client();
        client.setAvailable(new BigDecimal(available));
        when(clients.findByCNP("1234567890123")).thenReturn(Optional.of(client));
        LocalDate date = LocalDate.of(2026, 10, 26);

        service.sellProduct("1234567890123", "Produs", date,
                new BigDecimal(price), new BigDecimal(collected));

        verify(acquisitions).sellProduct("1234567890123", "Produs", date,
                new BigDecimal(price), new BigDecimal(collected));
        assertEquals(0, new BigDecimal(available).compareTo(client.getAvailable()));
    }

    @ParameterizedTest
    @CsvSource(value = {"0, 10", "-1, 10", "NULL, 10", "100, -1", "100, NULL"}, nullValues = "NULL")
    void rejectsInvalidAmountsWithoutChangingCredit(BigDecimal price, BigDecimal collected) {
        ClientRepository clients = mock(ClientRepository.class);
        AcquisitionRepository acquisitions = mock(AcquisitionRepository.class);
        Client client = new Client();
        client.setAvailable(BigDecimal.TEN);
        when(clients.findByCNP("1234567890123")).thenReturn(Optional.of(client));
        AcquisitionService service = new AcquisitionService(acquisitions, clients);

        assertThrows(IllegalArgumentException.class, () -> service.sellProduct(
                "1234567890123", "Produs", LocalDate.of(2026, 10, 26), price, collected));

        assertEquals(BigDecimal.TEN, client.getAvailable());
        verifyNoInteractions(acquisitions);
    }
}
