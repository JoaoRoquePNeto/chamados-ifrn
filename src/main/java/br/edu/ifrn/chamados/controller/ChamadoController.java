package br.edu.ifrn.chamados.controller;

import br.edu.ifrn.chamados.dto.*;
import br.edu.ifrn.chamados.dto.Requisicoes.*;
import br.edu.ifrn.chamados.model.*;
import br.edu.ifrn.chamados.service.ChamadoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@RequestMapping("/chamados")
@RequiredArgsConstructor
public class ChamadoController {
    private final ChamadoService chamados;

    @GetMapping
    public Pagina<Chamado> listar(@RequestParam(required = false) Status status,
                                  @RequestParam(required = false) Prioridade prioridade,
                                  @RequestParam(required = false) Long setorId,
                                  @RequestParam(required = false) Long categoriaId,
                                  @RequestParam(required = false) Long solicitanteId,
                                  @RequestParam(required = false) Long tecnicoId,
                                  @RequestParam(required = false) LocalDate dataInicio,
                                  @RequestParam(required = false) LocalDate dataFim,
                                  Pageable pageable) {
        return Pagina.de(chamados.listar(status, prioridade, setorId, categoriaId, solicitanteId,
                tecnicoId, dataInicio, dataFim, pageable));
    }

    @GetMapping("/{id}")
    public DetalheChamado detalhar(@PathVariable Long id) {
        return chamados.detalhar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Chamado abrir(@Valid @RequestBody NovoChamado dados) {
        return chamados.abrir(dados);
    }

    @PatchMapping("/{id}/assumir")
    public Chamado assumir(@PathVariable Long id, @Valid @RequestBody Assumir dados) {
        return chamados.assumir(id, dados);
    }

    @PatchMapping("/{id}/resolver")
    public Chamado resolver(@PathVariable Long id, @Valid @RequestBody Resolver dados) {
        return chamados.resolver(id, dados);
    }

    @PatchMapping("/{id}/cancelar")
    public Chamado cancelar(@PathVariable Long id, @Valid @RequestBody Cancelar dados) {
        return chamados.cancelar(id, dados);
    }

    @PostMapping("/{id}/interacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public Interacao interagir(@PathVariable Long id, @Valid @RequestBody NovaInteracao dados) {
        return chamados.interagir(id, dados);
    }
}
