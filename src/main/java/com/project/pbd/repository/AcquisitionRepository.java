package com.project.pbd.repository;

import com.project.pbd.entity.Acquisition;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.query.Procedure;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;

public interface AcquisitionRepository extends JpaRepository<Acquisition, Long> {
    @Procedure(procedureName = "SELL_PRODUCT")
    void sellProduct(@Param("P_CNP") String cnp,
                     @Param("P_PRODUCT") String product,
                     @Param("P_DATE") LocalDate acquisitionDate,
                     @Param("P_PRICE") BigDecimal price,
                     @Param("P_COLLECTED") BigDecimal collectedAmount);

    @Override
    @EntityGraph(attributePaths = "client")
    List<Acquisition> findAll();
}
