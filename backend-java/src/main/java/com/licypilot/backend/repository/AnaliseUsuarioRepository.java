package com.licypilot.backend.repository;

import com.licypilot.backend.model.AnaliseUsuario;
import com.licypilot.backend.model.Empresa;
import com.licypilot.backend.model.Licitacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnaliseUsuarioRepository extends JpaRepository<AnaliseUsuario, UUID> {
    List<AnaliseUsuario> findByLicitacao(Licitacao licitacao);
    List<AnaliseUsuario> findByEmpresa(Empresa empresa);
    Optional<AnaliseUsuario> findByLicitacaoAndEmpresa(Licitacao licitacao, Empresa empresa);
}
