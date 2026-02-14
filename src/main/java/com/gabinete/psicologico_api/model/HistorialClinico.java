// src\main\java\com\gabinete\psicologico_api\model\HistorialClinico.java
package com.gabinete.psicologico_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "historial_clinico")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialClinico {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "sesion_paciente_id", nullable = false)
    private SesionPaciente sesionPaciente;
    
    @Column(name = "nro_sesion")
    private Integer nroSesion;
    
    private LocalDateTime fecha;
    
    @Column(columnDefinition = "TEXT")
    private String historia;
    
    private Integer tipologia;
    
    private Integer gravedad;
    
    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}