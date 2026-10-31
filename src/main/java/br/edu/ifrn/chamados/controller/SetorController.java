package br.edu.ifrn.chamados.controller;

import br.edu.ifrn.chamados.dto.*;
import br.edu.ifrn.chamados.dto.Requisicoes.DadosSetor;
import br.edu.ifrn.chamados.model.*;
import br.edu.ifrn.chamados.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/setores")
@RequiredArgsConstructor
public class SetorController {
    private final SetorService setores;
    private final CategoriaService categorias;

    @GetMapping
    public Pagina<Setor> listar(@RequestParam(required = false) String nome, Pageable pageable) {
        return Pagina.de(setores.listar(nome, pageable));
    }

    @GetMapping("/{id}")
    public Setor buscar(@PathVariable Long id) {
        return setores.buscar(id);
    }

    @GetMapping("/{id}/categorias")
    public Pagina<Categoria> categorias(@PathVariable Long id,
                                        @RequestParam(required = false) String nome, Pageable pageable) {
        return Pagina.de(categorias.listarPorSetor(id, nome, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Setor criar(@Valid @RequestBody DadosSetor dados) {
        return setores.criar(dados);
    }

    @PutMapping("/{id}")
    public Setor atualizar(@PathVariable Long id, @Valid @RequestBody DadosSetor dados) {
        return setores.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        setores.excluir(id);
    }
}
