package com.mbvg.linomove.MBVGControlador;

import com.mbvg.linomove.MBVGServicio.MBVGNotificacionService;
import com.mbvg.linomove.MBVGEntidad.MBVGReserva;
import com.mbvg.linomove.MBVGEntidad.MBVGSoporte;
import com.mbvg.linomove.MBVGServicio.MBVGReservaService;
import com.mbvg.linomove.MBVGServicio.MBVGSoporteService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import com.mbvg.linomove.MBVGEntidad.MBVGPago;
import com.mbvg.linomove.MBVGRepositorio.MBVGReservaRepository;
import com.mbvg.linomove.MBVGRepositorio.MBVGPagoRepository;
import com.mbvg.linomove.MBVGServicio.MBVGPagoService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.mbvg.linomove.MBVGEntidad.MBVGConductor;
import com.mbvg.linomove.MBVGRepositorio.MBVGConductorRepository;
import org.springframework.beans.factory.annotation.Value;
import com.mbvg.linomove.MBVGEntidad.MBVGCalificacion;
import com.mbvg.linomove.MBVGRepositorio.MBVGCalificacionRepository;

@Controller
@RequestMapping("/cliente")
public class MBVGClienteController {
    
    @Autowired
    private MBVGCalificacionRepository calificacionRepo;
    
    @Autowired
    private MBVGNotificacionService notificacionService;
    
    @Autowired
    private MBVGConductorRepository conductorRepo;

    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;
    

    @Autowired
    private MBVGReservaService reservaService;

    @Autowired
    private MBVGSoporteService soporteService;
    
    @Autowired
    private MBVGReservaRepository reservaRepo;

    @Autowired
    private MBVGPagoRepository pagoRepo;

    @Autowired
    private MBVGPagoService pagoService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Integer clienteId = (Integer) session.getAttribute("usuarioId");

        if (clienteId == null) {
            return "redirect:/login-cliente";
        }

        model.addAttribute("reserva", new MBVGReserva());
        model.addAttribute("notificacionesRecientes", notificacionService.listarRecientes(clienteId, "cliente"));
        model.addAttribute("totalNoLeidas", notificacionService.contarNoLeidas(clienteId, "cliente"));

        return "MBVGCliente/MBVGdashboard";
    }

    @PostMapping("/solicitar-traslado")
    public String solicitarTraslado(@ModelAttribute MBVGReserva reserva, BindingResult result, HttpSession session) {
        Integer clienteId = (Integer) session.getAttribute("usuarioId");
        if (clienteId == null) {
            return "redirect:/login-cliente";
        }

        if (result.hasErrors()) {
            session.setAttribute("mensajeError", "Verifique que todos los datos ingresados sean correctos.");
            return "redirect:/cliente/dashboard";
        }
        
        try {
        reserva.setClienteId(clienteId);
        reservaService.solicitarTraslado(reserva);

        notificacionService.crearNotificacion(
                clienteId,
                "cliente",
                "Solicitud registrada",
                "Tu solicitud de traslado " + reserva.getOrigen() + " → " + reserva.getDestino() + " fue enviada correctamente.",
                "reserva"
        );

        session.setAttribute("mensajeExito", "Solicitud de traslado enviada exitosamente");
        return "redirect:/cliente/historial";
        } catch (Exception e) {
            session.setAttribute("mensajeError", "Error al registrar la solicitud en el sistema.");
            return "redirect:/cliente/dashboard";
        }
    }

    @GetMapping("/historial")
    public String historial(HttpSession session, Model model) {
        Integer clienteId = (Integer) session.getAttribute("usuarioId");
        if (clienteId == null) {
            return "redirect:/login-cliente";
        }

        List<MBVGReserva> reservasCliente = reservaService.misReservas(clienteId);

        Map<Integer, String> pagosPorReserva = new HashMap<>();
        Map<Integer, Boolean> calificacionesPorReserva = new HashMap<>();

        for (MBVGReserva reserva : reservasCliente) {
            MBVGPago pago = pagoRepo.findFirstByReservaIdOrderByIdDesc(reserva.getId());

            if (pago != null) {
                pagosPorReserva.put(reserva.getId(), pago.getEstado());
            }

            boolean yaCalificado = calificacionRepo.existsByReservaIdAndClienteId(reserva.getId(), clienteId);
            calificacionesPorReserva.put(reserva.getId(), yaCalificado);
        }

        model.addAttribute("historialViajes", reservasCliente);
        model.addAttribute("pagosPorReserva", pagosPorReserva);
        model.addAttribute("calificacionesPorReserva", calificacionesPorReserva);

        return "MBVGCliente/MBVGhistorial";
    }

@GetMapping("/calificar")
public String verFormularioCalificacion(@RequestParam Integer reservaId,
                                        HttpSession session,
                                        Model model) {
    Integer clienteId = (Integer) session.getAttribute("usuarioId");

    if (clienteId == null) {
        return "redirect:/login-cliente";
    }

    MBVGReserva reserva = reservaRepo.findById(reservaId).orElse(null);

    if (reserva == null || !reserva.getClienteId().equals(clienteId)) {
        session.setAttribute("mensajeError", "No se encontró la reserva o no pertenece a tu cuenta.");
        return "redirect:/cliente/historial";
    }

    if (!"completada".equalsIgnoreCase(reserva.getEstado()) && !"completado".equalsIgnoreCase(reserva.getEstado())) {
        session.setAttribute("mensajeError", "Solo puedes calificar viajes completados.");
        return "redirect:/cliente/historial";
    }

    if (reserva.getConductorId() == null) {
        session.setAttribute("mensajeError", "La reserva no tiene conductor asignado.");
        return "redirect:/cliente/historial";
    }

    if (calificacionRepo.existsByReservaIdAndClienteId(reservaId, clienteId)) {
        session.setAttribute("mensajeError", "Este viaje ya fue calificado.");
        return "redirect:/cliente/historial";
    }

    MBVGConductor conductor = conductorRepo.findById(reserva.getConductorId()).orElse(null);

    model.addAttribute("reserva", reserva);
    model.addAttribute("conductor", conductor);

    return "MBVGCliente/MBVGcalificacion";
    }

    @PostMapping("/calificar")
    public String calificarViaje(@RequestParam Integer reservaId,
                                 @RequestParam Integer conductorId,
                                 @RequestParam Integer puntuacionGeneral,
                                 @RequestParam Integer puntuacionPuntualidad,
                                 @RequestParam Integer puntuacionConduccion,
                                 @RequestParam Integer puntuacionVehiculo,
                                 @RequestParam Integer puntuacionTrato,
                                 @RequestParam(required = false) String comentario,
                                 HttpSession session) {

        Integer clienteId = (Integer) session.getAttribute("usuarioId");

        if (clienteId == null) {
            return "redirect:/login-cliente";
        }

        MBVGReserva reserva = reservaRepo.findById(reservaId).orElse(null);

        if (reserva == null || !reserva.getClienteId().equals(clienteId)) {
            session.setAttribute("mensajeError", "No se pudo validar la reserva del cliente.");
            return "redirect:/cliente/historial";
        }

        if (!"completada".equalsIgnoreCase(reserva.getEstado()) && !"completado".equalsIgnoreCase(reserva.getEstado())) {
            session.setAttribute("mensajeError", "Solo puedes calificar viajes completados.");
            return "redirect:/cliente/historial";
        }

        if (reserva.getConductorId() == null || !reserva.getConductorId().equals(conductorId)) {
            session.setAttribute("mensajeError", "El conductor no coincide con la reserva.");
            return "redirect:/cliente/historial";
        }

        if (calificacionRepo.existsByReservaIdAndClienteId(reservaId, clienteId)) {
            session.setAttribute("mensajeError", "Este viaje ya fue calificado anteriormente.");
            return "redirect:/cliente/historial";
        }

        if (puntuacionGeneral == null || puntuacionGeneral < 1 || puntuacionGeneral > 5) {
            session.setAttribute("mensajeError", "Debes seleccionar una calificación general válida.");
            return "redirect:/cliente/calificar?reservaId=" + reservaId;
        }

        MBVGCalificacion calificacion = new MBVGCalificacion();
        calificacion.setReservaId(reservaId);
        calificacion.setClienteId(clienteId);
        calificacion.setConductorId(conductorId);
        calificacion.setPuntuacionGeneral(puntuacionGeneral);
        calificacion.setPuntuacionPuntualidad(puntuacionPuntualidad);
        calificacion.setPuntuacionConduccion(puntuacionConduccion);
        calificacion.setPuntuacionVehiculo(puntuacionVehiculo);
        calificacion.setPuntuacionTrato(puntuacionTrato);
        calificacion.setComentario(comentario);

        calificacionRepo.save(calificacion);

        MBVGConductor conductor = conductorRepo.findById(conductorId).orElse(null);

        if (conductor != null) {
            Double promedio = calificacionRepo.obtenerPromedioGeneralPorConductor(conductorId);

            if (promedio == null) {
                promedio = 0.0;
            }

            double promedioRedondeado = Math.round(promedio * 10.0) / 10.0;
            conductor.setPuntuacionPromedio(promedioRedondeado);
            conductorRepo.save(conductor);
        }

        session.setAttribute("mensajeExito", "Calificación registrada correctamente.");
        return "redirect:/cliente/historial";
    }

    @GetMapping("/pago")
    public String verPago(@RequestParam Integer reservaId, HttpSession session, Model model) {
        Integer clienteId = (Integer) session.getAttribute("usuarioId");

        if (clienteId == null) {
            return "redirect:/login-cliente";
        }

        MBVGReserva reserva = reservaRepo.findById(reservaId).orElse(null);

        if (reserva == null || !reserva.getClienteId().equals(clienteId)) {
            session.setAttribute("mensajeError", "No se encontró la reserva o no pertenece a tu cuenta.");
            return "redirect:/cliente/historial";
        }

        if (reserva.getTarifa() == null || reserva.getTarifa() <= 0) {
            session.setAttribute("mensajeError", "La reserva aún no tiene una tarifa asignada.");
            return "redirect:/cliente/historial";
        }

        MBVGPago pago = pagoRepo.findFirstByReservaIdOrderByIdDesc(reservaId);

        if (pago == null) {
            pago = pagoService.crearPagoPendiente(reservaId, reserva.getTarifa());
        }

        model.addAttribute("reserva", reserva);
        model.addAttribute("pago", pago);

        return "MBVGCliente/MBVGpago";
    }

    @PostMapping("/pago/transferencia")
    public String procesarPagoTransferencia(@RequestParam Integer reservaId,
                                            @RequestParam String numeroOperacion,
                                            @RequestParam String banco,
                                            @RequestParam Double monto,
                                            HttpSession session) {

        Integer clienteId = (Integer) session.getAttribute("usuarioId");

        if (clienteId == null) {
            return "redirect:/login-cliente";
        }

        MBVGReserva reserva = reservaRepo.findById(reservaId).orElse(null);

        if (reserva == null || !reserva.getClienteId().equals(clienteId)) {
            session.setAttribute("mensajeError", "No se pudo validar la reserva del cliente.");
            return "redirect:/cliente/historial";
        }

        if (monto == null || monto <= 0) {
            session.setAttribute("mensajeError", "El monto del pago no es válido.");
            return "redirect:/cliente/pago?reservaId=" + reservaId;
        }

        pagoService.registrarTransferencia(reservaId, monto, banco, numeroOperacion);

        notificacionService.crearNotificacion(
                clienteId,
                "cliente",
                "Transferencia registrada",
                "Tu transferencia para la reserva #" + reservaId + " fue registrada y queda pendiente de validación.",
                "pago"
        );

        session.setAttribute("mensajeExito", "Transferencia registrada correctamente. Queda pendiente de validación.");
        return "redirect:/cliente/historial";
    }

    @GetMapping("/rastreo")
    public String rastrearViaje(HttpSession session, Model model) {
        Integer clienteId = (Integer) session.getAttribute("usuarioId");

        if (clienteId == null) {
            return "redirect:/login-cliente";
        }

        MBVGReserva reservaActiva = reservaRepo
                .findFirstByClienteIdAndEstadoOrderByFechaSolicitudDesc(clienteId, "en_ruta")
                .orElse(null);

        MBVGConductor conductor = null;

        if (reservaActiva != null && reservaActiva.getConductorId() != null) {
            conductor = conductorRepo.findById(reservaActiva.getConductorId()).orElse(null);
        }

        model.addAttribute("reservaActiva", reservaActiva);
        model.addAttribute("conductor", conductor);
        model.addAttribute("googleMapsApiKey", googleMapsApiKey);

        return "MBVGCliente/MBVGrastreo";
    }
        @GetMapping("/rastreo/ubicacion")
    @ResponseBody
    public Map<String, Object> obtenerUbicacionConductor(HttpSession session) {
        Map<String, Object> respuesta = new HashMap<>();

        Integer clienteId = (Integer) session.getAttribute("usuarioId");

        if (clienteId == null) {
            respuesta.put("disponible", false);
            respuesta.put("mensaje", "Sesión de cliente no válida.");
            return respuesta;
        }

        MBVGReserva reservaActiva = reservaRepo
                .findFirstByClienteIdAndEstadoOrderByFechaSolicitudDesc(clienteId, "en_ruta")
                .orElse(null);

        if (reservaActiva == null) {
            respuesta.put("disponible", false);
            respuesta.put("mensaje", "No hay viaje en ruta.");
            return respuesta;
        }

        if (reservaActiva.getLatitudConductor() == null || reservaActiva.getLongitudConductor() == null) {
            respuesta.put("disponible", false);
            respuesta.put("mensaje", "Esperando ubicación del conductor.");
            return respuesta;
        }

        respuesta.put("disponible", true);
        respuesta.put("reservaId", reservaActiva.getId());
        respuesta.put("latitud", reservaActiva.getLatitudConductor());
        respuesta.put("longitud", reservaActiva.getLongitudConductor());
        respuesta.put("ultimaUbicacion", reservaActiva.getUltimaUbicacionConductor());

        MBVGConductor conductor = null;

        if (reservaActiva.getConductorId() != null) {
            conductor = conductorRepo.findById(reservaActiva.getConductorId()).orElse(null);
        }

        respuesta.put("conductor", conductor != null ? conductor.getNombre() : "Conductor asignado");

        return respuesta;
    }

    @GetMapping("/soporte")
    public String soporte(HttpSession session, Model model) {
        Integer clienteId = (Integer) session.getAttribute("usuarioId");
        if (clienteId == null) {
            return "redirect:/login-cliente";
        }
        
        model.addAttribute("tickets", soporteService.misTickets(clienteId));
        return "MBVGCliente/MBVGsoporte";
    }

    @PostMapping("/soporte/crear")
    public String crearTicketSoporte(@RequestParam String asunto, @RequestParam String descripcion, 
                                     @RequestParam String prioridad, HttpSession session) {
        
        Integer clienteId = (Integer) session.getAttribute("usuarioId");
        if (clienteId == null) {
            return "redirect:/login-cliente";
        }

        MBVGSoporte nuevoTicket = new MBVGSoporte();
        nuevoTicket.setClienteId(clienteId);
        nuevoTicket.setAsunto(asunto);
        nuevoTicket.setDescripcion(descripcion);
        nuevoTicket.setPrioridad(prioridad);
        
        soporteService.crearTicket(nuevoTicket);

        session.setAttribute("mensajeExito", "Ticket creado y enviado a soporte correctamente.");
        return "redirect:/cliente/soporte";
    }

    // --- ESTA ES LA RUTA QUE FALTABA PARA EVITAR EL 404 ---
    @GetMapping("/soporte/detalle")
    public String detalleTicket(@RequestParam Integer id, HttpSession session, Model model) {
        Integer clienteId = (Integer) session.getAttribute("usuarioId");
        if (clienteId == null) {
            return "redirect:/login-cliente";
        }
        
        // Busca el ticket en la BD
        MBVGSoporte ticket = soporteService.obtenerTicketPorId(id);
        
        // Validamos que exista y que sea de este cliente
        if (ticket == null || !ticket.getClienteId().equals(clienteId)) {
            return "redirect:/cliente/soporte"; 
        }
        
        // Lo mandamos al HTML
        model.addAttribute("ticket", ticket);
        return "MBVGCliente/MBVGdetalleTicket";
    }

    @GetMapping("/notificaciones")
    public String notificaciones(HttpSession session, Model model) {
        Integer clienteId = (Integer) session.getAttribute("usuarioId");

        if (clienteId == null) {
            return "redirect:/login-cliente";
        }

        model.addAttribute("notificaciones", notificacionService.listarNotificaciones(clienteId, "cliente"));
        model.addAttribute("totalNoLeidas", notificacionService.contarNoLeidas(clienteId, "cliente"));

        return "MBVGCliente/MBVGnotificaciones";
    }

    @PostMapping("/notificaciones/leer")
    public String marcarNotificacionLeida(@RequestParam Integer notificacionId, HttpSession session) {
        Integer clienteId = (Integer) session.getAttribute("usuarioId");

        if (clienteId == null) {
            return "redirect:/login-cliente";
        }

        notificacionService.marcarComoLeida(notificacionId, clienteId, "cliente");

        return "redirect:/cliente/notificaciones";
    }

    @PostMapping("/notificaciones/leer-todas")
    public String marcarTodasLeidas(HttpSession session) {
        Integer clienteId = (Integer) session.getAttribute("usuarioId");

        if (clienteId == null) {
            return "redirect:/login-cliente";
        }

        notificacionService.marcarTodasComoLeidas(clienteId, "cliente");

        return "redirect:/cliente/notificaciones";
    }
}