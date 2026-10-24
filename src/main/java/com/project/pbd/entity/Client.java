package com.project.pbd.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clienti")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 15)
    @Column(name = "nume", nullable = false, length = 15)
    private String name;

    @NotBlank
    @Size(max = 20)
    @Column(name = "prenume", nullable = false, length = 20)
    private String surname;

    @NotBlank
    @Pattern(regexp = "^[0-9]{13}$")
    @Column(name = "cnp", nullable = false, unique = true, length = 13)
    private String CNP;

    @Column(name = "adresa", length = 255)
    private String address;

    @NotBlank
    @Pattern(regexp = "^[0-9]{9}$")
    @Column(name = "telefon", nullable = false, unique = true, length = 9)
    private String phone;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(name = "disponibil", nullable = false,
            precision = 12, scale = 2)
    private BigDecimal available = BigDecimal.ZERO;

    @OneToMany(mappedBy = "client", fetch = FetchType.LAZY)
    private List<Acquisition> acquisitions = new ArrayList<>();
}
