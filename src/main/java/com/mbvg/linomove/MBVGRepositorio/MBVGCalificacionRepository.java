package com.mbvg.linomove.MBVGRepositorio;

import com.mbvg.linomove.MBVGEntidad.MBVGCalificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MBVGCalificacionRepository extends JpaRepository<MBVGCalificacion, Integer> {

    List<MBVGCalificacion> findByConductorIdOrderByFechaEvaluacionDesc(Integer conductorId);

    boolean existsByReservaId(Integer reservaId);

    boolean existsByReservaIdAndClienteId(Integer reservaId, Integer clienteId);

    @Query("SELECT AVG(c.puntuacionGeneral) FROM MBVGCalificacion c WHERE c.conductorId = :conductorId")
    Double obtenerPromedioGeneralPorConductor(@Param("conductorId") Integer conductorId);
}