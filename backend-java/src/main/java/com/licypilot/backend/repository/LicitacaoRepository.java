package com.licypilot.backend.repository;

import com.licypilot.backend.model.Licitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LicitacaoRepository extends JpaRepository<Licitacao, UUID> {
}
