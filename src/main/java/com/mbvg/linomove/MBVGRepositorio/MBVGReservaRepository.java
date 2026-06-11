package com.mbvg.linomove.MBVGRepositorio;

import com.mbvg.linomove.MBVGEntidad.MBVGReserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface MBVGReservaRepository extends JpaRepository<MBVGReserva, Integer> {
    
    List<MBVGReserva> findByClienteIdOrderByFechaSolicitudDesc(Integer clienteId);
    
    List<MBVGReserva> findByConductorIdAndEstadoOrderByFechaSolicitudDesc(Integer conductorId, String estado);
    
    List<MBVGReserva> findByEstadoOrderByFechaSolicitudDesc(String estado);

    Optional<MBVGReserva> findFirstByConductorIdAndEstadoInOrderByFechaTrasladoAsc(
            Integer conductorId,
            List<String> estados
    );

    boolean existsByConductorIdAndEstadoIn(Integer conductorId, List<String> estados);

    Optional<MBVGReserva> findFirstByClienteIdAndEstadoOrderByFechaSolicitudDesc(
            Integer clienteId,
            String estado
    );
}