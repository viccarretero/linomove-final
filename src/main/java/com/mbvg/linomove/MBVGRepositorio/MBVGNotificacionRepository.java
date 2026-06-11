package com.mbvg.linomove.MBVGRepositorio;

import com.mbvg.linomove.MBVGEntidad.MBVGNotificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MBVGNotificacionRepository extends JpaRepository<MBVGNotificacion, Integer> {

    List<MBVGNotificacion> findByUsuarioIdAndTipoUsuarioOrderByFechaCreacionDesc(
            Integer usuarioId,
            String tipoUsuario
    );

    List<MBVGNotificacion> findTop5ByUsuarioIdAndTipoUsuarioOrderByFechaCreacionDesc(
            Integer usuarioId,
            String tipoUsuario
    );

    List<MBVGNotificacion> findByUsuarioIdAndTipoUsuarioAndEstadoOrderByFechaCreacionDesc(
            Integer usuarioId,
            String tipoUsuario,
            String estado
    );

    long countByUsuarioIdAndTipoUsuarioAndEstado(
            Integer usuarioId,
            String tipoUsuario,
            String estado
    );
}