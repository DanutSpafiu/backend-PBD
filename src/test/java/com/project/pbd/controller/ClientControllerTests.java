package com.project.pbd.controller;

import com.project.pbd.service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ClientControllerTests {
    @Test
    void exposesRemainingDebtAndRejectsInvalidParameters() throws Exception {
        ClientService service = mock(ClientService.class);
        when(service.leftToPay("1234567890123", "Sony")).thenReturn(new BigDecimal("6.00"));
        when(service.leftToPay("", "Sony")).thenThrow(new IllegalArgumentException("CNP cannot be blank"));
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new ClientController(service))
                .setControllerAdvice(new ApiExceptionHandler()).build();
        mvc.perform(get("/api/clienti/restDePlata").param("cnp", "1234567890123").param("product", "Sony"))
                .andExpect(status().isOk()).andExpect(content().string("6.00"));
        mvc.perform(get("/api/clienti/restDePlata").param("cnp", "").param("product", "Sony"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.detail").value("CNP cannot be blank"));
        mvc.perform(get("/api/clienti/restDePlata").param("cnp", "1234567890123"))
                .andExpect(status().isBadRequest());
    }
}
