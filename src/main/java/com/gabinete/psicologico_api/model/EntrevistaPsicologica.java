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
@Table(name = "entrevista_psicologica")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EntrevistaPsicologica {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "sesion_paciente_id", nullable = false, unique = true)
    private SesionPaciente sesionPaciente;
    
    private LocalDate fecha;
    
    @Column(length = 50)
    private String codigo;
    
    private Integer version;
    
    @Column(columnDefinition = "TEXT")
    private String antecedentes;
    
    @Column(name = "relato_universidad", columnDefinition = "TEXT")
    private String relatoUniversidad;
    
    @Column(name = "historia_familiar", columnDefinition = "TEXT")
    private String historiaFamiliar;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_familia", columnDefinition = "jsonb")
    private Map<String, Object> datosFamilia;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> habitos;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> sintomas;
    
    @Column(name = "total_score_estres")
    private Integer totalScoreEstres;
    
    @Column(name = "total_score_ansiedad")
    private Integer totalScoreAnsiedad;
    
    @Column(name = "total_score_depresion")
    private Integer totalScoreDepresion;
    
    @PrePersist
    protected void onCreate() {
        if (fecha == null) {
            fecha = LocalDate.now();
        }
        if (version == null) {
            version = 1;
        }
    }
}