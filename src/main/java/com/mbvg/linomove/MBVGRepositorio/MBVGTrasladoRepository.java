package com.mbvg.linomove.MBVGRepositorio;

import com.mbvg.linomove.MBVGEntidad.MBVGTraslado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MBVGTrasladoRepository extends JpaRepository<MBVGTraslado, Integer> {
    // obtener el traslado asociado a una reserva
    MBVGTraslado findByReservaId(Integer reservaId);
}