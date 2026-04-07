package com.gabinete.psicologico_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "horas_designadas_ap")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorasDesignadasAp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "psicologo_id", nullable = false)
    private Psicologo psicologo;

    @Column(nullable = false)
    private Integer anio;

    /** 1 = enero … 12 = diciembre */
    @Column(nullable = false)
    private Integer mes;

    /** "manana" | "tarde" */
    @Column(nullable = false, length = 20)
    private String turno;

    @Column(nullable = false)
    private Integer horas;
}
