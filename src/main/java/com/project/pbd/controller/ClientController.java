package com.project.pbd.controller;

import com.project.pbd.entity.Client;
import com.project.pbd.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/clienti")
@RequiredArgsConstructor
public class ClientController {
    private final ClientService clientService;

    @GetMapping("/restDePlata")
    public BigDecimal leftToPay(@RequestParam("cnp") String cnp,
                                @RequestParam("product") String product) {
        return clientService.leftToPay(cnp, product);
    }

    @GetMapping
    public List<Client> getAllClients() {
        return clientService.getAllClients();
    }
}
