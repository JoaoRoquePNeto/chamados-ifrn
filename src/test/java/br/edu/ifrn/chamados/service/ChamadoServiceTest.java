package br.edu.ifrn.chamados.service;

import br.edu.ifrn.chamados.dto.Requisicoes.*;
import br.edu.ifrn.chamados.exception.*;
import br.edu.ifrn.chamados.model.*;
import br.edu.ifrn.chamados.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChamadoServiceTest {
    @Mock ChamadoRepository chamados;
    @Mock InteracaoRepository interacoes;
    @Mock UsuarioService usuarios;
    @Mock CategoriaService categorias;
    @InjectMocks ChamadoService service;
    private Usuario solicitante;
    private Usuario tecnico;
    private Categoria categoria;
    private Chamado chamado;

    @BeforeEach
    void preparar() {
        Setor setor = new Setor();
        setor.setId(1L);
        solicitante = new Usuario();
        solicitante.setId(1L);
        solicitante.setTipo(TipoUsuario.SOLICITANTE);
        tecnico = new Usuario();
        tecnico.setId(3L);
        tecnico.setTipo(TipoUsuario.TECNICO);
        tecnico.setSetor(setor);
        categoria = new Categoria();
        categoria.setId(1L);
        categoria.setSetor(setor);
        chamado = new Chamado();
        chamado.setId(1L);
        chamado.setStatus(Status.ABERTO);
        chamado.setSolicitante(solicitante);
        chamado.setCategoria(categoria);
    }

    @Test
    void abreComStatusDataEPrioridadePadrao() {
        when(usuarios.buscar(1L)).thenReturn(solicitante);
        when(categorias.buscar(1L)).thenReturn(categoria);
        when(chamados.save(any())).thenAnswer(i -> i.getArgument(0));
        LocalDateTime antes = LocalDateTime.now();
        Chamado resultado = service.abrir(new NovoChamado("Wi-Fi", "Sem conexão", "Lab 1", null, 1L, 1L));
        assertEquals(Status.ABERTO, resultado.getStatus());
        assertEquals(Prioridade.MEDIA, resultado.getPrioridade());
        assertFalse(resultado.getDataAbertura().isBefore(antes));
        assertNull(resultado.getDataFechamento());
        assertNull(resultado.getTecnico());
    }

    @Test
    void tecnicoNaoPodeAbrir() {
        when(usuarios.buscar(3L)).thenReturn(tecnico);
        assertThrows(RegraNegocioException.class,
                () -> service.abrir(new NovoChamado("Wi-Fi", "Sem rede", "Lab", null, 3L, 1L)));
        verify(chamados, never()).save(any());
    }

    @Test
    void tecnicoDoSetorAssume() {
        when(chamados.findById(1L)).thenReturn(Optional.of(chamado));
        when(usuarios.buscar(3L)).thenReturn(tecnico);
        when(chamados.save(any())).thenAnswer(i -> i.getArgument(0));
        Chamado resultado = service.assumir(1L, new Assumir(3L));
        assertEquals(Status.EM_ATENDIMENTO, resultado.getStatus());
        assertEquals(tecnico, resultado.getTecnico());
    }

    @Test
    void rejeitaTecnicoDeOutroSetor() {
        Setor outro = new Setor();
        outro.setId(2L);
        tecnico.setSetor(outro);
        when(chamados.findById(1L)).thenReturn(Optional.of(chamado));
        when(usuarios.buscar(3L)).thenReturn(tecnico);
        assertThrows(RegraNegocioException.class, () -> service.assumir(1L, new Assumir(3L)));
        assertEquals(Status.ABERTO, chamado.getStatus());
    }

    @Test
    void solicitanteNaoPodeAssumir() {
        when(chamados.findById(1L)).thenReturn(Optional.of(chamado));
        when(usuarios.buscar(1L)).thenReturn(solicitante);
        assertThrows(RegraNegocioException.class, () -> service.assumir(1L, new Assumir(1L)));
    }

    @Test
    void resolveComSolucaoEDataFechamento() {
        chamado.setStatus(Status.EM_ATENDIMENTO);
        chamado.setTecnico(tecnico);
        when(chamados.findById(1L)).thenReturn(Optional.of(chamado));
        when(usuarios.buscar(3L)).thenReturn(tecnico);
        when(chamados.save(any())).thenAnswer(i -> i.getArgument(0));
        Chamado resultado = service.resolver(1L, new Resolver(3L, "Roteador reiniciado"));
        assertEquals(Status.RESOLVIDO, resultado.getStatus());
        assertEquals("Roteador reiniciado", resultado.getSolucao());
        assertNotNull(resultado.getDataFechamento());
    }

    @Test
    void outroTecnicoNaoPodeResolver() {
        chamado.setStatus(Status.EM_ATENDIMENTO);
        chamado.setTecnico(tecnico);
        when(chamados.findById(1L)).thenReturn(Optional.of(chamado));
        when(usuarios.buscar(4L)).thenReturn(new Usuario());
        assertThrows(RegraNegocioException.class, () -> service.resolver(1L, new Resolver(4L, "Reparo")));
    }

    @Test
    void rejeitaSolucaoEmBranco() {
        chamado.setStatus(Status.EM_ATENDIMENTO);
        chamado.setTecnico(tecnico);
        when(chamados.findById(1L)).thenReturn(Optional.of(chamado));
        when(usuarios.buscar(3L)).thenReturn(tecnico);
        assertThrows(RegraNegocioException.class, () -> service.resolver(1L, new Resolver(3L, " ")));
    }

    @Test
    void solicitanteCancelaChamadoAberto() {
        when(chamados.findById(1L)).thenReturn(Optional.of(chamado));
        when(usuarios.buscar(1L)).thenReturn(solicitante);
        when(chamados.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals(Status.CANCELADO, service.cancelar(1L, new Cancelar(1L)).getStatus());
        assertNotNull(chamado.getDataFechamento());
    }

    @Test
    void outroSolicitanteNaoCancela() {
        when(chamados.findById(1L)).thenReturn(Optional.of(chamado));
        when(usuarios.buscar(2L)).thenReturn(new Usuario());
        assertThrows(RegraNegocioException.class, () -> service.cancelar(1L, new Cancelar(2L)));
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = {"EM_ATENDIMENTO", "RESOLVIDO", "CANCELADO"})
    void apenasAbertoPodeSerAssumidoOuCancelado(Status status) {
        chamado.setStatus(status);
        when(chamados.findById(1L)).thenReturn(Optional.of(chamado));
        assertThrows(RegraNegocioException.class, () -> service.assumir(1L, new Assumir(3L)));
        assertThrows(RegraNegocioException.class, () -> service.cancelar(1L, new Cancelar(1L)));
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = {"ABERTO", "RESOLVIDO", "CANCELADO"})
    void apenasEmAtendimentoPodeSerResolvido(Status status) {
        chamado.setStatus(status);
        when(chamados.findById(1L)).thenReturn(Optional.of(chamado));
        assertThrows(RegraNegocioException.class, () -> service.resolver(1L, new Resolver(3L, "Reparo")));
    }

    @ParameterizedTest
    @EnumSource(value = Status.class, names = {"RESOLVIDO", "CANCELADO"})
    void encerradosRejeitamInteracoes(Status status) {
        chamado.setStatus(status);
        when(chamados.findById(1L)).thenReturn(Optional.of(chamado));
        assertThrows(RegraNegocioException.class,
                () -> service.interagir(1L, new NovaInteracao(1L, "Mensagem")));
        verify(interacoes, never()).save(any());
    }

    @Test
    void terceiroNaoPodeInteragir() {
        Usuario terceiro = new Usuario();
        terceiro.setId(2L);
        when(chamados.findById(1L)).thenReturn(Optional.of(chamado));
        when(usuarios.buscar(2L)).thenReturn(terceiro);
        assertThrows(RegraNegocioException.class,
                () -> service.interagir(1L, new NovaInteracao(2L, "Mensagem")));
    }

    @Test
    void solicitanteETecnicoAtribuidoPodemInteragir() {
        chamado.setTecnico(tecnico);
        chamado.setStatus(Status.EM_ATENDIMENTO);
        when(chamados.findById(1L)).thenReturn(Optional.of(chamado));
        when(usuarios.buscar(1L)).thenReturn(solicitante);
        when(usuarios.buscar(3L)).thenReturn(tecnico);
        when(interacoes.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals(solicitante, service.interagir(1L, new NovaInteracao(1L, "Ainda sem rede")).getAutor());
        Interacao resposta = service.interagir(1L, new NovaInteracao(3L, "Verificando"));
        assertEquals(tecnico, resposta.getAutor());
        assertNotNull(resposta.getDataHora());
    }

    @Test
    void chamadoInexistenteRetornaExcecaoPropria() {
        when(chamados.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RecursoNaoEncontradoException.class, () -> service.detalhar(99L));
    }
}
