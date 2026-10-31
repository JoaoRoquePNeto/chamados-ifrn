package br.edu.ifrn.chamados.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "setores")
@Getter
@Setter
public class Setor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 100)
    private String nome;
    @Column(nullable = false, unique = true, length = 20)
    private String sigla;
    @JsonIgnore
    @OneToMany(mappedBy = "setor")
    private List<Categoria> categorias = new ArrayList<>();
    @JsonIgnore
    @OneToMany(mappedBy = "setor")
    private List<Usuario> tecnicos = new ArrayList<>();
}
