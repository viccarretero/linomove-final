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
@Table(name = "traslado")
public class MBVGTraslado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reserva_id")
    private Integer reservaId;

    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicio;

    @Column(name = "fecha_fin")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFin;

    @Column(name = "tiempo_estimado_minutos")
    private Integer tiempoEstimadoMinutos;

    @Column(name = "tiempo_real_minutos")
    private Integer tiempoRealMinutos;

    @Column(name = "ruta_optimizada")
    private String rutaOptimizada;

    private String estado;
    private String incidencias;

    @Column(name = "polyline_ruta", columnDefinition = "TEXT")
    private String polylineRuta;

    @Column(name = "distancia_total")
    private Double distanciaTotal;

    @Column(name = "tiempo_estimado_total")
    private Integer tiempoEstimadoTotal;

    @Column(name = "instrucciones_conductor")
    private String instruccionesConductor;

    @Column(name = "ruta_oficial_coordenadas", columnDefinition = "TEXT")
    private String rutaOficialCoordenadas;

    @Column(name = "puntos_ruta", columnDefinition = "TEXT")
    private String puntosRuta;

    @Column(name = "instrucciones_ruta", columnDefinition = "TEXT")
    private String instruccionesRuta;
}