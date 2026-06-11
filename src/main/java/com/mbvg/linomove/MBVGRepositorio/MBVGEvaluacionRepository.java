package com.mbvg.linomove.MBVGRepositorio;

import com.mbvg.linomove.MBVGEntidad.MBVGEvaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MBVGEvaluacionRepository extends JpaRepository<MBVGEvaluacion, Integer> {
    // trae las evaluaciones de un conductor
    List<MBVGEvaluacion> findByConductorId(Integer conductorId);
}