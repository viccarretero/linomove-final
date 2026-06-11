package com.mbvg.linomove.MBVGServicio;

import com.mbvg.linomove.MBVGEntidad.MBVGHistorialViaje;
import com.mbvg.linomove.MBVGRepositorio.MBVGHistorialViajeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MBVGHistorialService {

    @Autowired
    private MBVGHistorialViajeRepository historialRepo;

    // registra un viaje cuando finaliza
    public MBVGHistorialViaje registrarHistorial(MBVGHistorialViaje historial) {
        return historialRepo.save(historial);
    }

    // historial para el panel del conductor
    public List<MBVGHistorialViaje> obtenerPorConductor(Integer conductorId) {
        return historialRepo.findByConductorIdOrderByFechaFinDesc(conductorId);
    }

    // historial para el panel del cliente
    public List<MBVGHistorialViaje> obtenerPorCliente(Integer clienteId) {
        return historialRepo.findByClienteIdOrderByFechaFinDesc(clienteId);
    }

    // trae todos para los reportes del admin
    public List<MBVGHistorialViaje> listarTodos() {
        return historialRepo.findAll();
    }

    // busca un registro especifico por la reserva
    public MBVGHistorialViaje buscarPorReserva(Integer reservaId) {
        return historialRepo.findByReservaId(reservaId);
    }
}