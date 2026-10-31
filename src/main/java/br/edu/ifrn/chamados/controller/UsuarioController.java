package br.edu.ifrn.chamados.controller;

import br.edu.ifrn.chamados.dto.*;
import br.edu.ifrn.chamados.dto.Requisicoes.DadosUsuario;
import br.edu.ifrn.chamados.model.*;
import br.edu.ifrn.chamados.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarios;
    private final ChamadoService chamados;

    @GetMapping
    public Pagina<Usuario> listar(@RequestParam(required = false) String nome,
                                  @RequestParam(required = false) TipoUsuario tipo,
                                  @RequestParam(required = false) Long setorId, Pageable pageable) {
        return Pagina.de(usuarios.listar(nome, tipo, setorId, pageable));
    }

    @GetMapping("/{id}")
    public Usuario buscar(@PathVariable Long id) {
        return usuarios.buscar(id);
    }

    @GetMapping("/{id}/chamados")
    public Pagina<Chamado> chamados(@PathVariable Long id,
                                    @RequestParam(required = false) Status status, Pageable pageable) {
        return Pagina.de(chamados.listarPorUsuario(id, status, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Usuario criar(@Valid @RequestBody DadosUsuario dados) {
        return usuarios.criar(dados);
    }

    @PutMapping("/{id}")
    public Usuario atualizar(@PathVariable Long id, @Valid @RequestBody DadosUsuario dados) {
        return usuarios.atualizar(id, dados);
    }
}
