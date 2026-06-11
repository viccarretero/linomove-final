package com.mbvg.linomove.MBVGServicio;

import com.mbvg.linomove.MBVGEntidad.MBVGNotificacion;
import com.mbvg.linomove.MBVGRepositorio.MBVGNotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.sql.Timestamp;
import java.util.List;

@Service
public class MBVGNotificacionService {

    @Autowired
    private MBVGNotificacionRepository notificacionRepo;

    public void crearNotificacion(Integer usuarioId, String tipoUsuario, String titulo, String mensaje) {
        crearNotificacion(usuarioId, tipoUsuario, titulo, mensaje, "estado");
    }

    public void crearNotificacion(Integer usuarioId, String tipoUsuario, String titulo, String mensaje, String tipo) {
        if (usuarioId == null || tipoUsuario == null) {
            return;
        }

        MBVGNotificacion notif = new MBVGNotificacion();
        notif.setUsuarioId(usuarioId);
        notif.setTipoUsuario(tipoUsuario);
        notif.setTitulo(titulo);
        notif.setMensaje(mensaje);
        notif.setTipo(tipo);
        notif.setEstado("enviada");
        notif.setFechaEnvio(new Timestamp(System.currentTimeMillis()));

        notificacionRepo.save(notif);
    }

    public List<MBVGNotificacion> listarNotificaciones(Integer usuarioId, String tipoUsuario) {
        return notificacionRepo.findByUsuarioIdAndTipoUsuarioOrderByFechaCreacionDesc(usuarioId, tipoUsuario);
    }

    public List<MBVGNotificacion> listarRecientes(Integer usuarioId, String tipoUsuario) {
        return notificacionRepo.findTop5ByUsuarioIdAndTipoUsuarioOrderByFechaCreacionDesc(usuarioId, tipoUsuario);
    }

    public long contarNoLeidas(Integer usuarioId, String tipoUsuario) {
        return notificacionRepo.countByUsuarioIdAndTipoUsuarioAndEstado(usuarioId, tipoUsuario, "enviada");
    }

    public void marcarComoLeida(Integer notificacionId, Integer usuarioId, String tipoUsuario) {
        MBVGNotificacion notificacion = notificacionRepo.findById(notificacionId).orElse(null);

        if (notificacion == null) {
            return;
        }

        if (!notificacion.getUsuarioId().equals(usuarioId)) {
            return;
        }

        if (!notificacion.getTipoUsuario().equals(tipoUsuario)) {
            return;
        }

        notificacion.setEstado("leida");
        notificacionRepo.save(notificacion);
    }

    public void marcarTodasComoLeidas(Integer usuarioId, String tipoUsuario) {
        List<MBVGNotificacion> notificaciones = notificacionRepo
                .findByUsuarioIdAndTipoUsuarioAndEstadoOrderByFechaCreacionDesc(usuarioId, tipoUsuario, "enviada");

        for (MBVGNotificacion notificacion : notificaciones) {
            notificacion.setEstado("leida");
        }

        notificacionRepo.saveAll(notificaciones);
    }
}