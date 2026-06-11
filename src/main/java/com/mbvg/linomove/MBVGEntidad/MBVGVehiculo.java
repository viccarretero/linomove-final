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
@Table(name = "vehiculo")
public class MBVGVehiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String placa;
    private String marca;
    private String modelo;
    private Integer anio;

    @Column(name = "tipo_vehiculo")
    private String tipoVehiculo;

    private String capacidad;
    private String estado;

    @Column(name = "conductor_asignado")
    private Integer conductorAsignado;

    @Column(name = "fecha_adquisicion")
    @Temporal(TemporalType.DATE)
    private Date fechaAdquisicion;

    @Column(name = "ultimo_mantenimiento")
    @Temporal(TemporalType.DATE)
    private Date ultimoMantenimiento;

    @Column(name = "proximo_mantenimiento")
    @Temporal(TemporalType.DATE)
    private Date proximoMantenimiento;

    private Double kilometraje;
    private String observaciones;
}