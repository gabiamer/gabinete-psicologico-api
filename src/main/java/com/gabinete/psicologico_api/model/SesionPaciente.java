//src/main/java/com/gabinete/psicologico_api/model/SesionPaciente.java
package com.gabinete.psicologico_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Entity
@Table(name = "sesion_paciente")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SesionPaciente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "paciente_universitario_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private PacienteUniversitario pacienteUniversitario;
    
    @ManyToOne
    @JoinColumn(name = "psicologo_id", nullable = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Psicologo psicologo;
    
    @Column(nullable = false)
    private LocalDateTime fecha;

    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}