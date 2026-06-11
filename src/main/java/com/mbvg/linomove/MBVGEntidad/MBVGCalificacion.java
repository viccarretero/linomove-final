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
@Table(name = "calificaciones")
public class MBVGCalificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reserva_id")
    private Integer reservaId;

    @Column(name = "cliente_id")
    private Integer clienteId;

    @Column(name = "conductor_id")
    private Integer conductorId;

    @Column(name = "puntuacion_general")
    private Integer puntuacionGeneral;

    @Column(name = "puntuacion_puntualidad")
    private Integer puntuacionPuntualidad;

    @Column(name = "puntuacion_conduccion")
    private Integer puntuacionConduccion;

    @Column(name = "puntuacion_vehiculo")
    private Integer puntuacionVehiculo;

    @Column(name = "puntuacion_trato")
    private Integer puntuacionTrato;

    private String comentario;

    @Column(name = "fecha_evaluacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEvaluacion;

    // asigna fecha por defecto si es nueva
    @PrePersist
    protected void onCreate() {
        if (fechaEvaluacion == null) {
            fechaEvaluacion = new Date();
        }
    }
}