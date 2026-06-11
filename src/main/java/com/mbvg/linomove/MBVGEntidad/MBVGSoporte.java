package com.mbvg.linomove.MBVGEntidad;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "soporte")
public class MBVGSoporte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "cliente_id")
    private Integer clienteId;

    private String asunto;
    private String descripcion;
    private String estado;
    private String prioridad;

    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "fecha_resolucion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaResolucion;

    @Column(name = "administrador_id")
    private Integer administradorId;

    private String respuesta;

    // campos temporales para las vistas (no se guardan en la tabla)
    @Transient
    private String nombreCliente;

    @Transient
    private String nombreAdministrador;

    // valores por defecto al crear el ticket
    @PrePersist
    protected void onCreate() {
        if (fechaCreacion == null) {
            fechaCreacion = new Date();
        }
        if (estado == null) {
            estado = "abierto";
        }
    }
}