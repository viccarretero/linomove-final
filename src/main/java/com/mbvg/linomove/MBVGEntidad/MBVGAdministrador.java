package com.mbvg.linomove.MBVGEntidad;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "administrador")
public class MBVGAdministrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String email;
    @JsonIgnore
    private String password;
    private String telefono;
    
    @Column(name = "nivel_acceso")
    private String nivelAcceso;

    @Column(name = "fecha_registro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;

    private String estado;
    
    // fecha por defecto al registrar
    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = new Date();
        }
    }
}