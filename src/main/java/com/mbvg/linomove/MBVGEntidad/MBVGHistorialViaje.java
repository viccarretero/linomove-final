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
@Table(name = "historial_viajes")
public class MBVGHistorialViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reserva_id")
    private Integer reservaId;

    @Column(name = "conductor_id")
    private Integer conductorId;

    @Column(name = "cliente_id")
    private Integer clienteId;

    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicio;

    @Column(name = "fecha_fin")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFin;

    @Column(name = "distancia_recorrida")
    private Double distanciaRecorrida;

    @Column(name = "tiempo_duracion")
    private Integer tiempoDuracion;

    @Column(name = "tarifa_final")
    private Double tarifaFinal;

    private Integer calificacion;
    private String comentario;

    @Column(name = "fecha_registro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;

    // asigna fecha de registro por defecto
    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = new Date();
        }
    }
}