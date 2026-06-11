package com.mbvg.linomove.MBVGServicio;

import com.mbvg.linomove.MBVGEntidad.MBVGHistorialViaje;
import com.mbvg.linomove.MBVGRepositorio.MBVGHistorialViajeRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import com.mbvg.linomove.MBVGEntidad.MBVGConductor;
import com.mbvg.linomove.MBVGEntidad.MBVGReserva;
import com.mbvg.linomove.MBVGRepositorio.MBVGConductorRepository;
import com.mbvg.linomove.MBVGRepositorio.MBVGReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MBVGConductorService {
    @Autowired
    private MBVGHistorialViajeRepository historialRepo;

    @Autowired
    private MBVGConductorRepository conductorRepo;

    @Autowired
    private MBVGReservaRepository reservaRepo;

    @Transactional
    public MBVGConductor guardar(MBVGConductor conductor) {
        return conductorRepo.save(conductor);
    }

    public boolean existeEmail(String email) {
        return conductorRepo.existsByEmail(email);
    }

    public boolean existeLicencia(String licencia) {
        return conductorRepo.existsByLicencia(licencia);
    }

    public List<MBVGConductor> obtenerDisponiblesPorTipo(String tipoVehiculo) {
        return conductorRepo.findByEstadoAndTipoVehiculoOrderByPuntuacionPromedioDesc("disponible", tipoVehiculo);
    }

    @Transactional
    public void cambiarEstado(Integer id, String nuevoEstado) {
        MBVGConductor conductor = conductorRepo.findById(id).orElse(null);
        if (conductor != null) {
            conductor.setEstado(nuevoEstado);
            conductorRepo.save(conductor);
        }
    }

    @Transactional(readOnly = true)
    public MBVGReserva obtenerViajeActivo(Integer conductorId) {
        return reservaRepo.findFirstByConductorIdAndEstadoInOrderByFechaTrasladoAsc(
                conductorId,
                List.of("asignada", "en_ruta")
        ).orElse(null);
    }

    @Transactional
    public boolean iniciarViaje(Integer conductorId, Integer reservaId) {
        MBVGReserva reserva = reservaRepo.findById(reservaId).orElse(null);

        if (reserva == null) {
            return false;
        }

        if (reserva.getConductorId() == null || !reserva.getConductorId().equals(conductorId)) {
            return false;
        }

        if (!"asignada".equalsIgnoreCase(reserva.getEstado())) {
            return false;
        }

        reserva.setEstado("en_ruta");
        reservaRepo.save(reserva);

        cambiarEstado(conductorId, "ocupado");

        MBVGHistorialViaje historial = historialRepo.findByReservaId(reservaId);

        if (historial == null) {
            historial = new MBVGHistorialViaje();
        }

        historial.setReservaId(reserva.getId());
        historial.setConductorId(conductorId);
        historial.setClienteId(reserva.getClienteId());
        historial.setFechaInicio(new Date());
        historial.setFechaFin(null);
        historial.setDistanciaRecorrida(null);
        historial.setTarifaFinal(null);
        historial.setTiempoDuracion(null);

        historialRepo.save(historial);

        return true;
    }

    @Transactional
    public boolean finalizarViaje(Integer conductorId, Integer reservaId) {
        MBVGReserva reserva = reservaRepo.findById(reservaId).orElse(null);

        if (reserva == null) {
            return false;
        }

        if (reserva.getConductorId() == null || !reserva.getConductorId().equals(conductorId)) {
            return false;
        }

        if (!"en_ruta".equalsIgnoreCase(reserva.getEstado())) {
            return false;
        }

        reserva.setEstado("completada");
        reservaRepo.save(reserva);

        boolean tieneOtroViajeActivo = reservaRepo.existsByConductorIdAndEstadoIn(
                conductorId,
                List.of("asignada", "en_ruta")
        );

        if (tieneOtroViajeActivo) {
            cambiarEstado(conductorId, "ocupado");
        } else {
            cambiarEstado(conductorId, "disponible");
        }

        MBVGHistorialViaje historial = historialRepo.findByReservaId(reservaId);

        if (historial == null) {
            historial = new MBVGHistorialViaje();
            historial.setFechaInicio(new Date());
        }

        historial.setReservaId(reserva.getId());
        historial.setConductorId(conductorId);
        historial.setClienteId(reserva.getClienteId());

        Date fechaFin = new Date();
        historial.setFechaFin(fechaFin);
        historial.setDistanciaRecorrida(reserva.getDistanciaKm());
        historial.setTarifaFinal(reserva.getTarifa());

        if (historial.getFechaInicio() != null) {
            long diferenciaMilisegundos = fechaFin.getTime() - historial.getFechaInicio().getTime();
            int minutos = (int) (diferenciaMilisegundos / 60000);
            historial.setTiempoDuracion(Math.max(minutos, 1));
        } else {
            historial.setTiempoDuracion(1);
        }

        historialRepo.save(historial);

        return true;
    }
        @Transactional(readOnly = true)
    public long obtenerViajesDelMes(Integer conductorId) {
        LocalDate hoy = LocalDate.now();

        LocalDateTime inicioMes = hoy.withDayOfMonth(1).atStartOfDay();
        LocalDateTime inicioMesSiguiente = hoy.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        Date inicio = Date.from(inicioMes.atZone(ZoneId.systemDefault()).toInstant());
        Date fin = Date.from(inicioMesSiguiente.atZone(ZoneId.systemDefault()).toInstant());

        return historialRepo.countByConductorIdAndFechaFinBetween(conductorId, inicio, fin);
    }
}