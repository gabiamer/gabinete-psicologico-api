package com.gabinete.psicologico_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "paciente_externo")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PacienteExterno {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "paciente_id", nullable = false, unique = true)
    private Paciente paciente;
    
    @Column(length = 255)
    private String escuela;
    
    private Integer anio;
    
    @Column(length = 50)
    private String correo;
}