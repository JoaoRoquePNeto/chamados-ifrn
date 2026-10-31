package br.edu.ifrn.chamados.service;

import br.edu.ifrn.chamados.dto.Requisicoes.DadosSetor;
import br.edu.ifrn.chamados.exception.*;
import br.edu.ifrn.chamados.model.Setor;
import br.edu.ifrn.chamados.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SetorService {
    private final SetorRepository setores;
    private final CategoriaRepository categorias;
    private final UsuarioRepository usuarios;
    private final ChamadoRepository chamados;

    public Page<Setor> listar(String nome, Pageable pageable) {
        return setores.findByNomeContainingIgnoreCase(nome == null ? "" : nome, pageable);
    }

    public Setor buscar(Long id) {
        return setores.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Setor"));
    }

    @Transactional
    public Setor criar(DadosSetor dados) {
        return salvar(new Setor(), dados);
    }

    @Transactional
    public Setor atualizar(Long id, DadosSetor dados) {
        return salvar(buscar(id), dados);
    }

    private Setor salvar(Setor setor, DadosSetor dados) {
        String sigla = dados.sigla().trim().toUpperCase(Locale.ROOT);
        if (setores.existsBySiglaIgnoreCaseAndIdNot(sigla, setor.getId() == null ? 0L : setor.getId())) {
            throw new RegraNegocioException("Já existe um setor com esta sigla.");
        }
        setor.setNome(dados.nome().trim());
        setor.setSigla(sigla);
        return setores.save(setor);
    }

    @Transactional
    public void excluir(Long id) {
        Setor setor = buscar(id);
        if (chamados.existsByCategoriaSetorId(id)) {
            throw new RegraNegocioException("Não é permitido excluir setor com chamados vinculados.");
        }
        if (categorias.existsBySetorId(id) || usuarios.existsBySetorId(id)) {
            throw new RegraNegocioException("Remova as categorias e transfira os técnicos antes de excluir o setor.");
        }
        setores.delete(setor);
    }
}
