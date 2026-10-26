package com.project.pbd.controller;

import com.project.pbd.dto.FulllyPaidAcquisitionDto;
import com.project.pbd.dto.DetailedAcquisitionDto;
import com.project.pbd.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rapoarte")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @GetMapping("/achizitiiDetalite")
    public List<DetailedAcquisitionDto> getDetailedAcquisitions() {
        return reportService.getDetailedAcquisitions();
    }

    @GetMapping("/achizitiAchitate")
    public List<FulllyPaidAcquisitionDto> getFullyPaidAcquisitions() {
        return reportService.getFullyPaidAcquisitions();
    }

}
