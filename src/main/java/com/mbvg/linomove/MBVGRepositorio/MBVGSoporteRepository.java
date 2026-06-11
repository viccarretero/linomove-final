package com.mbvg.linomove.MBVGRepositorio;

import com.mbvg.linomove.MBVGEntidad.MBVGSoporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MBVGSoporteRepository extends JpaRepository<MBVGSoporte, Integer> {
    // tickets de un cliente
    List<MBVGSoporte> findByClienteIdOrderByFechaCreacionDesc(Integer clienteId);
    
    // conteo de tickets para estadisticas del admin
    long countByEstado(String estado);
}