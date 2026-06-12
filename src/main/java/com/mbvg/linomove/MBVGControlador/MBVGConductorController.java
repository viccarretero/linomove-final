package com.mbvg.linomove.MBVGControlador;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import com.mbvg.linomove.MBVGRepositorio.MBVGReservaRepository;
import com.mbvg.linomove.MBVGServicio.MBVGNotificacionService;
import com.mbvg.linomove.MBVGEntidad.MBVGConductor;
import com.mbvg.linomove.MBVGEntidad.MBVGReserva;
import com.mbvg.linomove.MBVGRepositorio.MBVGConductorRepository;
import com.mbvg.linomove.MBVGServicio.MBVGConductorService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/conductor")
public class MBVGConductorController {
    
    @Autowired
    private MBVGReservaRepository reservaRepo;

    @Autowired
    private MBVGNotificacionService notificacionService;

    @Autowired
    private MBVGConductorRepository conductorRepo;

    @Autowired
    private MBVGConductorService conductorService;
    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Integer conductorId = (Integer) session.getAttribute("usuarioId");
        if (conductorId == null) {
            return "redirect:/login-conductor"; 
        }
        
        MBVGConductor conductor = conductorRepo.findById(conductorId).orElse(null);
        if (conductor != null) {
            model.addAttribute("conductor", conductor);
        }

        MBVGReserva viajeActivo = conductorService.obtenerViajeActivo(conductorId);
        model.addAttribute("viajeActivo", viajeActivo);
        long viajesMes = conductorService.obtenerViajesDelMes(conductorId);
        model.addAttribute("viajesMes", viajesMes);
        model.addAttribute("googleMapsApiKey", googleMapsApiKey);
        
        return "MBVGConductor/MBVGdashboard";
    }

    @PostMapping("/cambiar-estado")
    public String cambiarEstado(HttpSession session) {
        Integer conductorId = (Integer) session.getAttribute("usuarioId");
        if (conductorId != null) {
            MBVGConductor conductor = conductorRepo.findById(conductorId).orElse(null);
            if (conductor != null) {
                if ("disponible".equals(conductor.getEstado())) {
                    conductor.setEstado("inactivo");
                } else {
                    conductor.setEstado("disponible");
                }
                conductorRepo.save(conductor);
            }
        }
        return "redirect:/conductor/dashboard";
    }

    @PostMapping("/viaje/iniciar/{reservaId}")
    public String iniciarViaje(@PathVariable Integer reservaId, HttpSession session) {
        Integer conductorId = (Integer) session.getAttribute("usuarioId");

        if (conductorId == null) {
            return "redirect:/login-conductor";
        }

        boolean iniciado = conductorService.iniciarViaje(conductorId, reservaId);

        if (iniciado) {
            MBVGReserva reserva = reservaRepo.findById(reservaId).orElse(null);

            if (reserva != null) {
                notificacionService.crearNotificacion(
                        reserva.getClienteId(),
                        "cliente",
                        "Viaje iniciado",
                        "Tu viaje " + reserva.getOrigen() + " → " + reserva.getDestino() +
                                " ha iniciado. Ya puedes rastrear la ruta en tiempo real.",
                        "estado"
                );
            }

            return "redirect:/conductor/dashboard?exito=Viaje iniciado correctamente";
        }

        return "redirect:/conductor/dashboard?error=No se pudo iniciar el viaje";
    }

    @PostMapping("/viaje/finalizar/{reservaId}")
    public String finalizarViaje(@PathVariable Integer reservaId, HttpSession session) {
        Integer conductorId = (Integer) session.getAttribute("usuarioId");

        if (conductorId == null) {
            return "redirect:/login-conductor";
        }

        boolean finalizado = conductorService.finalizarViaje(conductorId, reservaId);

        if (finalizado) {
            MBVGReserva reserva = reservaRepo.findById(reservaId).orElse(null);

            if (reserva != null) {
                notificacionService.crearNotificacion(
                        reserva.getClienteId(),
                        "cliente",
                        "Viaje finalizado",
                        "Tu viaje " + reserva.getOrigen() + " → " + reserva.getDestino() +
                                " ha finalizado. Ya puedes realizar el pago desde tu historial.",
                        "finalizado"
                );
            }

            return "redirect:/conductor/dashboard?exito=Viaje finalizado correctamente";
        }

        return "redirect:/conductor/dashboard?error=No se pudo finalizar el viaje";
    }
        @PostMapping("/viaje/ubicacion")
    @ResponseBody
    public Map<String, Object> actualizarUbicacionConductor(@RequestParam Double latitud,
                                                            @RequestParam Double longitud,
                                                            HttpSession session) {
        Map<String, Object> respuesta = new HashMap<>();

        Integer conductorId = (Integer) session.getAttribute("usuarioId");

        if (conductorId == null) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", "Sesión de conductor no válida.");
            return respuesta;
        }

        if (latitud == null || longitud == null ||
                latitud < -90 || latitud > 90 ||
                longitud < -180 || longitud > 180) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", "Coordenadas no válidas.");
            return respuesta;
        }

        MBVGReserva viajeActivo = reservaRepo
                .findFirstByConductorIdAndEstadoInOrderByFechaTrasladoAsc(
                        conductorId,
                        List.of("en_ruta")
                )
                .orElse(null);

        if (viajeActivo == null) {
            respuesta.put("ok", false);
            respuesta.put("mensaje", "No tienes un viaje en ruta.");
            return respuesta;
        }

        viajeActivo.setLatitudConductor(latitud);
        viajeActivo.setLongitudConductor(longitud);
        viajeActivo.setUltimaUbicacionConductor(new Date());

        reservaRepo.save(viajeActivo);

        respuesta.put("ok", true);
        respuesta.put("mensaje", "Ubicación actualizada.");
        respuesta.put("latitud", latitud);
        respuesta.put("longitud", longitud);

        return respuesta;
    }

    // --- MI PERFIL (CONDUCTOR) ---
    @GetMapping("/perfil")
    public String miPerfil(HttpSession session, Model model) {
        Integer conductorId = (Integer) session.getAttribute("usuarioId");
        if (conductorId == null) {
            return "redirect:/login-conductor";
        }
        
        MBVGConductor conductor = conductorRepo.findById(conductorId).orElse(null);
        model.addAttribute("conductor", conductor);
        return "MBVGConductor/MBVGperfil";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarMiPerfil(@RequestParam String nombre,
                                     @RequestParam String email,
                                     @RequestParam String telefono,
                                     HttpSession session) {
        Integer conductorId = (Integer) session.getAttribute("usuarioId");
        MBVGConductor conductor = conductorRepo.findById(conductorId).orElse(null);
        
        if (conductor != null) {
            conductor.setNombre(nombre);
            conductor.setEmail(email);
            conductor.setTelefono(telefono);
            conductorRepo.save(conductor);
            session.setAttribute("usuarioNombre", nombre); 
        }

        return "redirect:/conductor/perfil?exito=Tus datos personales han sido actualizados";
    }

    @PostMapping("/perfil/password")
    public String cambiarMiPassword(@RequestParam String passwordActual,
                                    @RequestParam String nuevaPassword,
                                    HttpSession session) {
        Integer conductorId = (Integer) session.getAttribute("usuarioId");
        MBVGConductor conductor = conductorRepo.findById(conductorId).orElse(null);
        
        if (conductor != null) {
            if (conductor.getPassword().equals(passwordActual)) {
                conductor.setPassword(nuevaPassword);
                conductorRepo.save(conductor);
                return "redirect:/conductor/perfil?exito=Tu contraseña se ha cambiado exitosamente. Recuerda usarla en tu próximo inicio de sesión.";
            } else {
                return "redirect:/conductor/perfil?error=La contraseña actual ingresada es incorrecta. Inténtalo nuevamente.";
            }
        }

        return "redirect:/conductor/perfil";
    }
}