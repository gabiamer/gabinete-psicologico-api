package com.gabinete.psicologico_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "orientacion_vocacional")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrientacionVocacional {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "paciente_externo_id", nullable = false)
    private PacienteExterno pacienteExterno;
    
    private LocalDate fecha;
    
    @Column(columnDefinition = "TEXT")
    private String observaciones;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> respuestas;
    
    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDate.now();
        }
    }
}