package br.edu.ifrn.chamados.dto;

import br.edu.ifrn.chamados.model.*;
import java.time.LocalDateTime;
import java.util.List;

public record DetalheChamado(Long id, String titulo, String descricao, String local,
                             Prioridade prioridade, Status status, LocalDateTime dataAbertura,
                             LocalDateTime dataFechamento, String solucao, Usuario solicitante,
                             Categoria categoria, Usuario tecnico, List<Interacao> interacoes) {
    public static DetalheChamado de(Chamado c) {
        return new DetalheChamado(c.getId(), c.getTitulo(), c.getDescricao(), c.getLocal(),
                c.getPrioridade(), c.getStatus(), c.getDataAbertura(), c.getDataFechamento(),
                c.getSolucao(), c.getSolicitante(), c.getCategoria(), c.getTecnico(),
                List.copyOf(c.getInteracoes()));
    }
}
