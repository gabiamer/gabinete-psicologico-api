// src/main/java/com/gabinete/psicologico_api/model/Person.java

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
    
    @Column(name = "primer_nombre", nullable = false, length = 100)
    private String primerNombre;
    
    @Column(name = "segundo_nombre", length = 100)
    private String segundoNombre;
    
    @Column(name = "apellido_paterno", nullable = false, length = 100)
    private String apellidoPaterno;
    
    @Column(name = "apellido_materno", length = 100)
    private String apellidoMaterno;
    
    @Column(length = 20)
    private String celular;
}