package br.edu.ifrn.chamados.service;

import br.edu.ifrn.chamados.dto.Requisicoes.DadosUsuario;
import br.edu.ifrn.chamados.exception.*;
import br.edu.ifrn.chamados.model.*;
import br.edu.ifrn.chamados.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final SetorService setores;
    private final ChamadoRepository chamados;
    private final InteracaoRepository interacoes;

    public Page<Usuario> listar(String nome, TipoUsuario tipo, Long setorId, Pageable pageable) {
        return usuarios.filtrar(nome, tipo, setorId, pageable);
    }

    public Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário"));
    }

    @Transactional
    public Usuario criar(DadosUsuario dados) {
        return salvar(new Usuario(), dados);
    }

    @Transactional
    public Usuario atualizar(Long id, DadosUsuario dados) {
        Usuario usuario = buscar(id);
        Long setorAtual = usuario.getSetor() == null ? null : usuario.getSetor().getId();
        boolean mudouPerfil = usuario.getTipo() != dados.tipo() || !Objects.equals(setorAtual, dados.setorId());
        if (mudouPerfil && (chamados.existsBySolicitanteIdOrTecnicoId(id, id) || interacoes.existsByAutorId(id))) {
            throw new RegraNegocioException("Usuário com histórico não pode mudar de tipo ou setor.");
        }
        return salvar(usuario, dados);
    }

    private Usuario salvar(Usuario usuario, DadosUsuario dados) {
        String email = dados.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.existsByEmailIgnoreCaseAndIdNot(email, usuario.getId() == null ? 0L : usuario.getId())) {
            throw new RegraNegocioException("Já existe um usuário com este email.");
        }
        if (dados.tipo() == TipoUsuario.TECNICO && dados.setorId() == null) {
            throw new RegraNegocioException("O técnico deve pertencer a um setor.");
        }
        if (dados.tipo() == TipoUsuario.SOLICITANTE && dados.setorId() != null) {
            throw new RegraNegocioException("Somente técnicos pertencem a um setor.");
        }
        Setor setor = dados.setorId() == null ? null : setores.buscar(dados.setorId());
        usuario.setNome(dados.nome().trim());
        usuario.setEmail(email);
        usuario.setMatricula(dados.matricula().trim());
        usuario.setTipo(dados.tipo());
        usuario.setSetor(setor);
        return usuarios.save(usuario);
    }
}
