package com.project.pbd.repository;

import com.project.pbd.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;
import java.util.List;

public interface ClientRepository extends JpaRepository<Client, Long> {
    @Override
    @EntityGraph(attributePaths = "acquisitions")
    List<Client> findAll();

    Optional<Client> findByCNP(String cnp);
}
