package com.project.pbd.service;

import com.project.pbd.entity.Acquisition;
import com.project.pbd.repository.AcquisitionRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AcquisitionService {

    private final AcquisitionRepository acquisitionRepository;

    public List<Acquisition> getAllAcquisitions() {
        return acquisitionRepository.findAll();
    }
}
