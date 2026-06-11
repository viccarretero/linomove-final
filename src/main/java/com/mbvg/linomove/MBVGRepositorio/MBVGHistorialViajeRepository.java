package com.mbvg.linomove.MBVGRepositorio;

import com.mbvg.linomove.MBVGEntidad.MBVGHistorialViaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Date;
import java.util.List;

@Repository
public interface MBVGHistorialViajeRepository extends JpaRepository<MBVGHistorialViaje, Integer> {
    
    // historial de viajes de un conductor
    List<MBVGHistorialViaje> findByConductorIdOrderByFechaFinDesc(Integer conductorId);
    
    // historial de viajes de un cliente
    List<MBVGHistorialViaje> findByClienteIdOrderByFechaFinDesc(Integer clienteId);
    
    // busca un registro especifico por la reserva
    MBVGHistorialViaje findByReservaId(Integer reservaId);

    // cuenta viajes completados del conductor dentro de un rango de fechas
    long countByConductorIdAndFechaFinBetween(Integer conductorId, Date inicio, Date fin);
}