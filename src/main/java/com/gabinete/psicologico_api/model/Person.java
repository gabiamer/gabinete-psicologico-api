//src/main/java/com/gabinete/psicologico_api/model/Person.java
package com.gabinete.psicologico_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "person")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "primer_nombre", nullable = false)
    private String primerNombre;
    
    @Column(name = "segundo_nombre")
    private String segundoNombre;
    
    // CAMBIADO: Ya no es nullable=false
    @Column(name = "apellido_paterno")
    private String apellidoPaterno;
    
    // CAMBIADO: Ya no es nullable=false
    @Column(name = "apellido_materno")
    private String apellidoMaterno;
    
    @Column
    private String celular;
}