package br.edu.ifrn.chamados.service;

import br.edu.ifrn.chamados.dto.DetalheChamado;
import br.edu.ifrn.chamados.dto.Requisicoes.*;
import br.edu.ifrn.chamados.exception.*;
import br.edu.ifrn.chamados.model.*;
import br.edu.ifrn.chamados.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChamadoService {
    private final ChamadoRepository chamados;
    private final InteracaoRepository interacoes;
    private final UsuarioService usuarios;
    private final CategoriaService categorias;

    public Page<Chamado> listar(Status status, Prioridade prioridade, Long setorId, Long categoriaId,
                               Long solicitanteId, Long tecnicoId, LocalDate dataInicio,
                               LocalDate dataFim, Pageable pageable) {
        if (dataInicio != null && dataFim != null && dataInicio.isAfter(dataFim)) {
            throw new IllegalArgumentException("Período inválido.");
        }
        LocalDateTime inicio = (dataInicio == null ? LocalDate.of(1900, 1, 1) : dataInicio).atStartOfDay();
        LocalDateTime fim = (dataFim == null ? LocalDate.of(9999, 12, 30) : dataFim).plusDays(1).atStartOfDay();
        return chamados.filtrar(status, prioridade, setorId, categoriaId, solicitanteId, tecnicoId,
                inicio, fim, pageable);
    }

    public DetalheChamado detalhar(Long id) {
        return DetalheChamado.de(buscar(id));
    }

    public Page<Chamado> listarPorUsuario(Long usuarioId, Status status, Pageable pageable) {
        Usuario usuario = usuarios.buscar(usuarioId);
        if (usuario.getTipo() == TipoUsuario.SOLICITANTE) {
            return status == null ? chamados.findBySolicitanteId(usuarioId, pageable)
                    : chamados.findBySolicitanteIdAndStatus(usuarioId, status, pageable);
        }
        return status == null ? chamados.findByTecnicoId(usuarioId, pageable)
                : chamados.findByTecnicoIdAndStatus(usuarioId, status, pageable);
    }

    @Transactional
    public Chamado abrir(NovoChamado dados) {
        Usuario solicitante = usuarios.buscar(dados.solicitanteId());
        if (solicitante.getTipo() != TipoUsuario.SOLICITANTE) {
            throw new RegraNegocioException("Apenas solicitantes podem abrir chamados.");
        }
        Categoria categoria = categorias.buscar(dados.categoriaId());
        Chamado chamado = new Chamado();
        chamado.setTitulo(dados.titulo().trim());
        chamado.setDescricao(dados.descricao().trim());
        chamado.setLocal(dados.local().trim());
        chamado.setPrioridade(dados.prioridade() == null ? Prioridade.MEDIA : dados.prioridade());
        chamado.setStatus(Status.ABERTO);
        chamado.setDataAbertura(LocalDateTime.now());
        chamado.setSolicitante(solicitante);
        chamado.setCategoria(categoria);
        return chamados.save(chamado);
    }

    @Transactional
    public Chamado assumir(Long id, Assumir dados) {
        Chamado chamado = buscar(id);
        exigirStatus(chamado, Status.ABERTO);
        Usuario tecnico = usuarios.buscar(dados.tecnicoId());
        if (tecnico.getTipo() != TipoUsuario.TECNICO ||
                !tecnico.getSetor().getId().equals(chamado.getCategoria().getSetor().getId())) {
            throw new RegraNegocioException("O chamado deve ser assumido por um técnico do setor responsável.");
        }
        chamado.setTecnico(tecnico);
        chamado.setStatus(Status.EM_ATENDIMENTO);
        return chamados.save(chamado);
    }

    @Transactional
    public Chamado resolver(Long id, Resolver dados) {
        Chamado chamado = buscar(id);
        exigirStatus(chamado, Status.EM_ATENDIMENTO);
        usuarios.buscar(dados.tecnicoId());
        if (!chamado.getTecnico().getId().equals(dados.tecnicoId())) {
            throw new RegraNegocioException("Somente o técnico atribuído pode resolver o chamado.");
        }
        if (dados.solucao() == null || dados.solucao().isBlank()) {
            throw new RegraNegocioException("Informe a solução do chamado.");
        }
        chamado.setSolucao(dados.solucao().trim());
        chamado.setStatus(Status.RESOLVIDO);
        chamado.setDataFechamento(LocalDateTime.now());
        return chamados.save(chamado);
    }

    @Transactional
    public Chamado cancelar(Long id, Cancelar dados) {
        Chamado chamado = buscar(id);
        exigirStatus(chamado, Status.ABERTO);
        usuarios.buscar(dados.solicitanteId());
        if (!chamado.getSolicitante().getId().equals(dados.solicitanteId())) {
            throw new RegraNegocioException("Somente o solicitante do chamado pode cancelá-lo.");
        }
        chamado.setStatus(Status.CANCELADO);
        chamado.setDataFechamento(LocalDateTime.now());
        return chamados.save(chamado);
    }

    @Transactional
    public Interacao interagir(Long id, NovaInteracao dados) {
        Chamado chamado = buscar(id);
        if (chamado.getStatus() == Status.RESOLVIDO || chamado.getStatus() == Status.CANCELADO) {
            throw new RegraNegocioException("Chamados encerrados não aceitam novas interações.");
        }
        Usuario autor = usuarios.buscar(dados.autorId());
        boolean solicitante = chamado.getSolicitante().getId().equals(autor.getId());
        boolean tecnico = chamado.getTecnico() != null && chamado.getTecnico().getId().equals(autor.getId());
        if (!solicitante && !tecnico) {
            throw new RegraNegocioException("Apenas o solicitante e o técnico atribuído podem interagir.");
        }
        Interacao interacao = new Interacao();
        interacao.setChamado(chamado);
        interacao.setAutor(autor);
        interacao.setTexto(dados.texto().trim());
        interacao.setDataHora(LocalDateTime.now());
        return interacoes.save(interacao);
    }

    private Chamado buscar(Long id) {
        return chamados.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Chamado"));
    }

    private void exigirStatus(Chamado chamado, Status esperado) {
        if (chamado.getStatus() != esperado) {
            throw new RegraNegocioException("Esta ação exige um chamado com status " + esperado + ".");
        }
    }
}
