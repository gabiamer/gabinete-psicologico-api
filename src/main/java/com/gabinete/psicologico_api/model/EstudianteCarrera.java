// src/main/java/com/gabinete/psicologico_api/model/EstudianteCarrera.java
package com.gabinete.psicologico_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "estudiante_carrera")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstudianteCarrera {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "paciente_universitario_id", nullable = false)
    private PacienteUniversitario pacienteUniversitario;
    
    @ManyToOne
    @JoinColumn(name = "carrera_id", nullable = false)
    private Carrera carrera;
}