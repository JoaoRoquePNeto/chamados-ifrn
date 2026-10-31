package br.edu.ifrn.chamados.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chamados")
@Getter
@Setter
public class Chamado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String titulo;
    @Column(nullable = false, length = 4000)
    private String descricao;
    @Column(nullable = false, length = 150)
    private String local;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridade prioridade;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;
    @Column(nullable = false)
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    @Column(length = 4000)
    private String solucao;
    @ManyToOne(optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;
    @ManyToOne(optional = false)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;
    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Usuario tecnico;
    @JsonIgnore
    @OneToMany(mappedBy = "chamado")
    @OrderBy("dataHora ASC, id ASC")
    private List<Interacao> interacoes = new ArrayList<>();
}
