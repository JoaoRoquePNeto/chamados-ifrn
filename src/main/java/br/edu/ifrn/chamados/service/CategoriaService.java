package br.edu.ifrn.chamados.service;

import br.edu.ifrn.chamados.dto.Requisicoes.DadosCategoria;
import br.edu.ifrn.chamados.exception.*;
import br.edu.ifrn.chamados.model.*;
import br.edu.ifrn.chamados.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoriaService {
    private final CategoriaRepository categorias;
    private final SetorService setores;
    private final ChamadoRepository chamados;

    public Page<Categoria> listar(String nome, Long setorId, Pageable pageable) {
        return categorias.filtrar(nome == null ? "" : nome, setorId, pageable);
    }

    public Page<Categoria> listarPorSetor(Long setorId, String nome, Pageable pageable) {
        setores.buscar(setorId);
        return listar(nome, setorId, pageable);
    }

    public Categoria buscar(Long id) {
        return categorias.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Categoria"));
    }

    @Transactional
    public Categoria criar(DadosCategoria dados) {
        return salvar(new Categoria(), dados);
    }

    @Transactional
    public Categoria atualizar(Long id, DadosCategoria dados) {
        Categoria categoria = buscar(id);
        if (!categoria.getSetor().getId().equals(dados.setorId()) && chamados.existsByCategoriaId(id)) {
            throw new RegraNegocioException("Categoria com chamados não pode mudar de setor.");
        }
        return salvar(categoria, dados);
    }

    private Categoria salvar(Categoria categoria, DadosCategoria dados) {
        Setor setor = setores.buscar(dados.setorId());
        categoria.setNome(dados.nome().trim());
        categoria.setDescricao(dados.descricao().trim());
        categoria.setSetor(setor);
        return categorias.save(categoria);
    }

    @Transactional
    public void excluir(Long id) {
        Categoria categoria = buscar(id);
        if (chamados.existsByCategoriaId(id)) {
            throw new RegraNegocioException("Não é permitido excluir categoria com chamados vinculados.");
        }
        categorias.delete(categoria);
    }
}
