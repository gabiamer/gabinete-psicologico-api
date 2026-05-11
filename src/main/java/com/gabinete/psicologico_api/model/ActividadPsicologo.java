package com.gabinete.psicologico_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "actividad_psicologo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActividadPsicologo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "psicologo_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Psicologo psicologo;

    @Column(nullable = false, length = 255)
    private String titulo;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDateTime fechaFin;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String objetivo;

    @Column(nullable = false, length = 255)
    private String poblacion;

    @Column(name = "num_asistentes", nullable = false)
    private Integer numAsistentes;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resumen;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String resultados;

    @OneToMany(mappedBy = "actividadPsicologo", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ActividadEvidencia> evidencias;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}