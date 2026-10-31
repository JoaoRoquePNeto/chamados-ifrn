package br.edu.ifrn.chamados.repository;

import br.edu.ifrn.chamados.model.Categoria;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    @Query("""
            select c from Categoria c
            where (:nome is null or lower(c.nome) like lower(concat('%', :nome, '%')))
            and (:setorId is null or c.setor.id = :setorId)
            """)
    Page<Categoria> filtrar(String nome, Long setorId, Pageable pageable);

    boolean existsBySetorId(Long setorId);
}
