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
@Table(name = "cliente")
public class MBVGCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String email;
    @JsonIgnore
    private String password;
    private String telefono;
    private String direccion;

    @Column(name = "documento_identidad")
    private String documentoIdentidad;

    @Column(name = "fecha_registro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;

    private String estado;

    // establece la fecha actual si es nuevo
    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = new Date();
        }
    }
}