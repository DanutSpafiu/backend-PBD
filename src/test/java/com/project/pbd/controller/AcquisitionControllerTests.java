package com.project.pbd.controller;

import com.project.pbd.repository.AcquisitionRepository;
import com.project.pbd.repository.ClientRepository;
import com.project.pbd.service.AcquisitionService;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AcquisitionControllerTests {
    @Test
    void unknownClientReturnsNotFoundWithoutSavingPurchase() throws Exception {
        ClientRepository clients = mock(ClientRepository.class);
        AcquisitionRepository acquisitions = mock(AcquisitionRepository.class);
        when(clients.findByCNP("9999999999999")).thenReturn(Optional.empty());
        AcquisitionService service = new AcquisitionService(acquisitions, clients);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new AcquisitionController(service)).build();

        mvc.perform(post("/api/achizitii/achizitieNoua")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "cnp": "9999999999999",
                                  "product": "Test",
                                  "acquisitionDate": "2026-10-26",
                                  "price": 100,
                                  "collectedAmount": 10
                                }
                                """))
                .andExpect(status().isNotFound());

        verify(clients).findByCNP("9999999999999");
        verifyNoInteractions(acquisitions);
    }
}
