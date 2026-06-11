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
@Table(name = "pago")
public class MBVGPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "reserva_id")
    private Integer reservaId;

    private Double monto;

    @Column(name = "metodo_pago")
    private String metodoPago;

    private String estado;

    @Column(name = "fecha_pago")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaPago;

    @Column(name = "transaccion_id")
    private String transaccionId;

    @Column(name = "codigo_confirmacion")
    private String codigoConfirmacion;
}