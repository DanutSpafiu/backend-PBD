package com.project.pbd.controller;

import com.project.pbd.entity.Acquisition;
import com.project.pbd.service.AcquisitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
}
