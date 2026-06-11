package com.mbvg.linomove.MBVGControlador;

import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class MBVGApiRestController {

    // actualiza la ubicacion gps del conductor en tiempo real
    @PostMapping("/ubicacion/actualizar")
    public Map<String, Object> actualizarUbicacion(
            @RequestParam Integer conductorId,
            @RequestParam Double latitud,
            @RequestParam Double longitud) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // TODO: guardar coordenadas en la bd
            response.put("success", true);
            response.put("message", "ubicacion actualizada");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "error al actualizar");
        }
        
        return response;
    }

    // marca una notificacion de la campanita como leida via ajax
    @PostMapping("/notificaciones/leer")
    public Map<String, Object> marcarNotificacionLeida(@RequestParam Integer notificacionId) {
        Map<String, Object> response = new HashMap<>();
        
        // TODO: actualizar estado de la notificacion
        response.put("success", true);
        return response;
    }

    // boton switch del dashboard para que el conductor se ponga ocupado/disponible
    @PostMapping("/conductor/estado")
    public Map<String, Object> cambiarEstadoConductor(
            @RequestParam Integer conductorId, 
            @RequestParam String estado) {
        
        Map<String, Object> response = new HashMap<>();
        
        // TODO: actualizar estado en la bd
        response.put("success", true);
        return response;
    }
}