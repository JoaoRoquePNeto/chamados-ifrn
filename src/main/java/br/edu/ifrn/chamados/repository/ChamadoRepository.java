package br.edu.ifrn.chamados.repository;

import br.edu.ifrn.chamados.model.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import java.time.LocalDateTime;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {
    @Query("""
            select c from Chamado c
            where (:status is null or c.status = :status)
            and (:prioridade is null or c.prioridade = :prioridade)
            and (:setorId is null or c.categoria.setor.id = :setorId)
            and (:categoriaId is null or c.categoria.id = :categoriaId)
            and (:solicitanteId is null or c.solicitante.id = :solicitanteId)
            and (:tecnicoId is null or c.tecnico.id = :tecnicoId)
            and c.dataAbertura >= :inicio and c.dataAbertura < :fim
            """)
    Page<Chamado> filtrar(Status status, Prioridade prioridade, Long setorId, Long categoriaId,
                         Long solicitanteId, Long tecnicoId, LocalDateTime inicio,
                         LocalDateTime fim, Pageable pageable);

    Page<Chamado> findBySolicitanteId(Long id, Pageable pageable);
    Page<Chamado> findBySolicitanteIdAndStatus(Long id, Status status, Pageable pageable);
    Page<Chamado> findByTecnicoId(Long id, Pageable pageable);
    Page<Chamado> findByTecnicoIdAndStatus(Long id, Status status, Pageable pageable);
    boolean existsByCategoriaId(Long categoriaId);
    boolean existsByCategoriaSetorId(Long setorId);
    boolean existsBySolicitanteIdOrTecnicoId(Long solicitanteId, Long tecnicoId);
}
