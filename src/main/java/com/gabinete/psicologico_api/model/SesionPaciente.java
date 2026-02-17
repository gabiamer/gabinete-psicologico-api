//src/main/java/com/gabinete/psicologico_api/model/SesionPaciente.java
package com.gabinete.psicologico_api.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
    
    @Column(length = 255)
    private String tipo;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String acuerdos; // Cambiado de Map<String, Object> a String
    
    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }
}