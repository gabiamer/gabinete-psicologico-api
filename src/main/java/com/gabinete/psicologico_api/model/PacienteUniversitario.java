package com.gabinete.psicologico_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "paciente_universitario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacienteUniversitario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;
    
    private Integer semestre;
    
    @Column(name = "derivado_por", length = 255)
    private String derivadoPor;
}