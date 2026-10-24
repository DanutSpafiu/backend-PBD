package com.project.pbd.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "Achizitii")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class Acquisition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    @JsonIgnore
    private Client client;

    @NotBlank
    @Column(name = "produs", nullable = false)
    private String product;

    @NotNull
    @Column(name = "data_achizitie", nullable = false)
    private LocalDate acquisitionDate;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    @Column(name = "pret", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(name = "suma_incasata", nullable = false, precision = 12, scale = 2)
    private BigDecimal collectedAmount = BigDecimal.ZERO;
}
