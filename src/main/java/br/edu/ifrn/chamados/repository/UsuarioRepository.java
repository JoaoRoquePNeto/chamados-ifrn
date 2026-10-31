package br.edu.ifrn.chamados.repository;

import br.edu.ifrn.chamados.model.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    @Query("""
            select u from Usuario u
            where (:nome is null or lower(u.nome) like lower(concat('%', :nome, '%')))
            and (:tipo is null or u.tipo = :tipo)
            and (:setorId is null or u.setor.id = :setorId)
            """)
    Page<Usuario> filtrar(String nome, TipoUsuario tipo, Long setorId, Pageable pageable);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsBySetorId(Long setorId);
}
