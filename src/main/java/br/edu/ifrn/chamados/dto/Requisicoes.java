package br.edu.ifrn.chamados.dto;

import br.edu.ifrn.chamados.model.*;
import jakarta.validation.constraints.*;

public class Requisicoes {
    public record DadosSetor(@NotBlank @Size(max = 100) String nome,
                             @NotBlank @Size(max = 20) String sigla) {}

    public record DadosCategoria(@NotBlank @Size(max = 100) String nome,
                                 @NotBlank @Size(max = 1000) String descricao,
                                 @NotNull @Positive Long setorId) {}

    public record DadosUsuario(@NotBlank @Size(max = 100) String nome,
                               @NotBlank @Email @Size(max = 150) String email,
                               @NotBlank @Size(max = 30) String matricula,
                               @NotNull TipoUsuario tipo,
                               @Positive Long setorId) {}

    public record NovoChamado(@NotBlank @Size(max = 150) String titulo,
                              @NotBlank @Size(max = 4000) String descricao,
                              @NotBlank @Size(max = 150) String local,
                              Prioridade prioridade,
                              @NotNull @Positive Long solicitanteId,
                              @NotNull @Positive Long categoriaId) {}

    public record Assumir(@NotNull @Positive Long tecnicoId) {}

    public record Resolver(@NotNull @Positive Long tecnicoId,
                           @NotBlank @Size(max = 4000) String solucao) {}

    public record Cancelar(@NotNull @Positive Long solicitanteId) {}

    public record NovaInteracao(@NotNull @Positive Long autorId,
                                @NotBlank @Size(max = 4000) String texto) {}
}
