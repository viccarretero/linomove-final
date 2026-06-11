package com.mbvg.linomove.MBVGServicio;

import com.mbvg.linomove.MBVGEntidad.MBVGReserva;
import com.mbvg.linomove.MBVGRepositorio.MBVGReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MBVGReservaService {

    @Autowired
    private MBVGReservaRepository reservaRepo;

    @Autowired
    private MBVGPagoService pagoService;

    @Transactional
    public MBVGReserva solicitarTraslado(MBVGReserva reserva) {
        reserva.setEstado("pendiente");
        return reservaRepo.save(reserva);
    }

    @Transactional(readOnly = true)
    public List<MBVGReserva> misReservas(Integer clienteId) {
        return reservaRepo.findByClienteIdOrderByFechaSolicitudDesc(clienteId);
    }

    @Transactional
    public void asignarConductor(Integer reservaId, Integer conductorId, double distanciaKm) {
        MBVGReserva reserva = reservaRepo.findById(reservaId).orElse(null);

        if (reserva != null) {
            reserva.setConductorId(conductorId);
            reserva.setEstado("asignada");

            // Guardamos la distancia usada para calcular el precio
            reserva.setDistanciaKm(distanciaKm);

            // Calculamos con la tarifa actual: S/ 3.10 por kilometro
            double tarifaTotal = pagoService.calcularTarifaBase(distanciaKm, reserva.getTipoVehiculo());
            reserva.setTarifa(tarifaTotal);

            reservaRepo.save(reserva);

            // Se genera el pago pendiente por el monto calculado
            pagoService.crearPagoPendiente(reservaId, tarifaTotal);

            // TODO: compañero llamara a MBVGNotificacionService aqui
        }
    }
}