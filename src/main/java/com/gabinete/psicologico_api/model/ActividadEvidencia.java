package com.gabinete.psicologico_api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "actividad_evidencia")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadEvidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "actividad_psicologo_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @JsonIgnore
    private ActividadPsicologo actividadPsicologo;

    @JsonIgnore
    @Column(nullable = false, columnDefinition = "bytea")
    private byte[] imagen;

    @Column(nullable = false, length = 255)
    private String nombre;
}
