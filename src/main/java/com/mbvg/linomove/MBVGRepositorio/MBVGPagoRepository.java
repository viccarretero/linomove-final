package com.mbvg.linomove.MBVGRepositorio;

import com.mbvg.linomove.MBVGEntidad.MBVGPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MBVGPagoRepository extends JpaRepository<MBVGPago, Integer> {
    // busca el ultimo pago asociado a una reserva
    MBVGPago findFirstByReservaIdOrderByIdDesc(Integer reservaId);
    
    // lista pagos por estado
    List<MBVGPago> findByEstadoOrderByIdAsc(String estado);
}