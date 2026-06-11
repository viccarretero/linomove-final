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
@Table(name = "conductor")
public class MBVGConductor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String email;
    @JsonIgnore
    private String password;
    private String telefono;
    private String licencia;

    @Column(name = "vehiculo_asignado")
    private String vehiculoAsignado;

    @Column(name = "tipo_vehiculo")
    private String tipoVehiculo;

    @Column(name = "puntuacion_promedio")
    private Double puntuacionPromedio;

    @Column(name = "fecha_registro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;

    private String estado;

    @Column(name = "latitud_actual")
    private Double latitudActual;

    @Column(name = "longitud_actual")
    private Double longitudActual;

    @Column(name = "ultima_actualizacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date ultimaActualizacion;

    // valores iniciales
    @PrePersist
    protected void onCreate() {
        if (fechaRegistro == null) {
            fechaRegistro = new Date();
        }
        if (puntuacionPromedio == null) {
            puntuacionPromedio = 0.0;
        }
    }
}