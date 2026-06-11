package com.mbvg.linomove.MBVGServicio;

import com.mbvg.linomove.MBVGEntidad.MBVGPago;
import com.mbvg.linomove.MBVGEntidad.MBVGReserva;
import com.mbvg.linomove.MBVGRepositorio.MBVGPagoRepository;
import com.mbvg.linomove.MBVGRepositorio.MBVGReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MBVGPagoService {

    @Autowired
    private MBVGPagoRepository pagoRepo;

    @Autowired
    private MBVGReservaRepository reservaRepo;

    @Autowired
    private MBVGNotificacionService notificacionService;

    @Value("${linomove.tarifa.km:3.10}")
    private double tarifaPorKm;

    @Value("${linomove.cobro.minimo:20.00}")
    private double cobroMinimo;

    public double calcularTarifaBase(double distanciaKm, String tipoVehiculo) {
        if (distanciaKm <= 0) {
            return cobroMinimo;
        }

        double tarifa = distanciaKm * tarifaPorKm;
        tarifa = Math.max(tarifa, cobroMinimo);

        return Math.round(tarifa * 100.0) / 100.0;
    }

    @Transactional
    public MBVGPago crearPagoPendiente(Integer reservaId, Double monto) {
        MBVGPago pagoExistente = pagoRepo.findFirstByReservaIdOrderByIdDesc(reservaId);

        if (pagoExistente != null) {
            pagoExistente.setMonto(monto);

            if (!"completado".equalsIgnoreCase(pagoExistente.getEstado())) {
                pagoExistente.setEstado("pendiente");
            }

            return pagoRepo.save(pagoExistente);
        }

        MBVGPago pago = new MBVGPago();
        pago.setReservaId(reservaId);
        pago.setMonto(monto);
        pago.setEstado("pendiente");

        return pagoRepo.save(pago);
    }

    @Transactional
    public boolean registrarTransferencia(Integer reservaId, Double monto, String banco, String numeroOperacion) {
        MBVGPago pago = pagoRepo.findFirstByReservaIdOrderByIdDesc(reservaId);

        if (pago == null) {
            pago = new MBVGPago();
            pago.setReservaId(reservaId);
        }

        pago.setMonto(monto);
        pago.setMetodoPago("TRANSFERENCIA_" + banco);
        pago.setTransaccionId(numeroOperacion);
        pago.setCodigoConfirmacion("OP-" + numeroOperacion);
        pago.setEstado("pendiente_validacion");

        return pagoRepo.save(pago) != null;
    }

    public java.util.List<MBVGPago> listarPagosPendientesValidacion() {
        return pagoRepo.findByEstadoOrderByIdAsc("pendiente_validacion");
    }

    @Transactional
    public boolean aprobarPago(Integer pagoId) {
        MBVGPago pago = pagoRepo.findById(pagoId).orElse(null);

        if (pago == null) {
            return false;
        }

        if (!"pendiente_validacion".equalsIgnoreCase(pago.getEstado())) {
            return false;
        }

        pago.setEstado("completado");
        pago.setFechaPago(new java.util.Date());

        if (pago.getCodigoConfirmacion() == null || pago.getCodigoConfirmacion().isBlank()) {
            pago.setCodigoConfirmacion("VALIDADO-" + pago.getId());
        }

        pagoRepo.save(pago);

        MBVGReserva reserva = reservaRepo.findById(pago.getReservaId()).orElse(null);

        if (reserva != null) {
            notificacionService.crearNotificacion(
                    reserva.getClienteId(),
                    "cliente",
                    "Pago aprobado",
                    "Tu pago de la reserva #" + reserva.getId() +
                            " fue aprobado correctamente. Gracias por usar LinoMove.",
                    "pago"
            );
        }

        return true;
    }

    @Transactional
    public boolean rechazarPago(Integer pagoId) {
        MBVGPago pago = pagoRepo.findById(pagoId).orElse(null);

        if (pago == null) {
            return false;
        }

        if (!"pendiente_validacion".equalsIgnoreCase(pago.getEstado())) {
            return false;
        }

        pago.setEstado("rechazado");
        pago.setFechaPago(null);
        pagoRepo.save(pago);

        MBVGReserva reserva = reservaRepo.findById(pago.getReservaId()).orElse(null);

        if (reserva != null) {
            notificacionService.crearNotificacion(
                    reserva.getClienteId(),
                    "cliente",
                    "Pago rechazado",
                    "Tu pago de la reserva #" + reserva.getId() +
                            " fue rechazado. Verifica los datos de la transferencia y vuelve a registrarlo.",
                    "pago"
            );
        }

        return true;
    }

    @Transactional
    public boolean procesarPago(Integer pagoId, String metodo, String transaccionId) {
        MBVGPago pago = pagoRepo.findById(pagoId).orElse(null);

        if (pago != null) {
            pago.setEstado("completado");
            pago.setMetodoPago(metodo);
            pago.setTransaccionId(transaccionId);
            pagoRepo.save(pago);
            return true;
        }

        return false;
    }
}