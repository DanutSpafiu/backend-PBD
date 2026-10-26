package com.project.pbd.repository;

import com.project.pbd.entity.Acquisition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcquisitionRepository extends JpaRepository<Acquisition, Long> {
    @Override
    @EntityGraph(attributePaths = "client")
    List<Acquisition> findAll();
}
