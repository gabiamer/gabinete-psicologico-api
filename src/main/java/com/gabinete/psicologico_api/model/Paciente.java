package com.gabinete.psicologico_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "paciente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paciente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "person_id", nullable = false)
    private Person person;
    
    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;
    
    private Integer genero;
    
    @Column(length = 255)
    private String domicilio;
    
    @Column(name = "estado_civil")
    private Integer estadoCivil;
    
    @Column(name = "tipo_paciente")
    private Integer tipoPaciente; // 1: Universitario, 2: Externo
}