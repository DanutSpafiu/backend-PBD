package com.project.pbd.controller;

import com.project.pbd.entity.Acquisition;
import com.project.pbd.dto.SellProductRequest;
import com.project.pbd.service.AcquisitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/achizitii")
@RequiredArgsConstructor
public class AcquisitionController {
    private final AcquisitionService acquisitionService;

    @GetMapping
    public List<Acquisition> getAllAcquisitions() {
        return acquisitionService.getAllAcquisitions();
    }

    @PostMapping("/achizitieNoua")
    public void sellProduct(@Valid @RequestBody SellProductRequest request) {
        acquisitionService.sellProduct(request.cnp(), request.product(), request.acquisitionDate(),
                request.price(), request.collectedAmount());
    }
}
