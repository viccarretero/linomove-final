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
@Table(name = "evaluacion")
public class MBVGEvaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reserva_id")
    private Integer reservaId;

    @Column(name = "cliente_id")
    private Integer clienteId;

    @Column(name = "conductor_id")
    private Integer conductorId;

    private Integer puntuacion;
    private String comentario;

    @Column(name = "fecha_evaluacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaEvaluacion;

    // asigna fecha por defecto
    @PrePersist
    protected void onCreate() {
        if (fechaEvaluacion == null) {
            fechaEvaluacion = new Date();
        }
    }
}