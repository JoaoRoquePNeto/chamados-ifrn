package br.edu.ifrn.chamados.controller;

import br.edu.ifrn.chamados.dto.*;
import br.edu.ifrn.chamados.dto.Requisicoes.DadosCategoria;
import br.edu.ifrn.chamados.model.Categoria;
import br.edu.ifrn.chamados.service.CategoriaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categorias")
@RequiredArgsConstructor
public class CategoriaController {
    private final CategoriaService categorias;

    @GetMapping
    public Pagina<Categoria> listar(@RequestParam(required = false) String nome,
                                    @RequestParam(required = false) Long setorId, Pageable pageable) {
        return Pagina.de(categorias.listar(nome, setorId, pageable));
    }

    @GetMapping("/{id}")
    public Categoria buscar(@PathVariable Long id) {
        return categorias.buscar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Categoria criar(@Valid @RequestBody DadosCategoria dados) {
        return categorias.criar(dados);
    }

    @PutMapping("/{id}")
    public Categoria atualizar(@PathVariable Long id, @Valid @RequestBody DadosCategoria dados) {
        return categorias.atualizar(id, dados);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        categorias.excluir(id);
    }
}
