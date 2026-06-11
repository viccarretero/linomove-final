package com.mbvg.linomove.MBVGControlador;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.Map;
import com.mbvg.linomove.MBVGServicio.MBVGNotificacionService;
import com.mbvg.linomove.MBVGEntidad.*;
import com.mbvg.linomove.MBVGRepositorio.*;
import com.mbvg.linomove.MBVGServicio.MBVGReservaService;
import com.mbvg.linomove.MBVGServicio.MBVGPagoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;

@Controller
@RequestMapping("/admin")
public class MBVGAdminController {
    
    @Autowired private MBVGNotificacionService notificacionService;

    @Autowired private MBVGClienteRepository clienteRepo;
    @Autowired private MBVGConductorRepository conductorRepo;
    @Autowired private MBVGReservaRepository reservaRepo;
    @Autowired private MBVGSoporteRepository soporteRepo;
    @Autowired private MBVGVehiculoRepository vehiculoRepo;
    @Autowired private MBVGAdministradorRepository adminRepo;
    @Autowired private MBVGReservaService reservaService;
    @Autowired private MBVGPagoService pagoService;
    @Autowired private MBVGPagoRepository pagoRepo;
    @Value("${google.maps.api.key:}")
    private String googleMapsApiKey;

    @Value("${linomove.tarifa.km:3.10}")
    private Double tarifaKm;
    
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        model.addAttribute("totalClientes", clienteRepo.count());
        model.addAttribute("totalConductores", conductorRepo.count());
        model.addAttribute("reservasHoy", reservaRepo.count()); 
        return "MBVGAdmin/MBVGdashboard";
    }

    // --- NUEVO: MI PERFIL (ADMINISTRADOR) ---
    @GetMapping("/perfil")
    public String miPerfil(HttpSession session, Model model) {
        Integer miId = (Integer) session.getAttribute("usuarioId");
        if(miId == null) return "redirect:/login-admin";
        
        MBVGAdministrador admin = adminRepo.findById(miId).orElse(null);
        model.addAttribute("admin", admin);
        return "MBVGAdmin/MBVGperfil";
    }

    @PostMapping("/perfil/actualizar")
    public String actualizarMiPerfil(@RequestParam String nombre, @RequestParam String email, HttpSession session) {
        Integer miId = (Integer) session.getAttribute("usuarioId");
        MBVGAdministrador admin = adminRepo.findById(miId).orElse(null);
        
        if (admin != null) {
            admin.setNombre(nombre);
            admin.setEmail(email);
            adminRepo.save(admin);
            session.setAttribute("usuarioNombre", nombre); // Actualizamos la sesión para que el menú cambie
        }
        return "redirect:/admin/perfil?exito=Tus datos personales han sido actualizados";
    }

    @PostMapping("/perfil/password")
    public String cambiarMiPassword(@RequestParam String passwordActual, @RequestParam String nuevaPassword, HttpSession session) {
        Integer miId = (Integer) session.getAttribute("usuarioId");
        MBVGAdministrador admin = adminRepo.findById(miId).orElse(null);
        
        if (admin != null) {
            if (admin.getPassword().equals(passwordActual)) {
                admin.setPassword(nuevaPassword);
                adminRepo.save(admin);
                return "redirect:/admin/perfil?exito=Tu contraseña se ha cambiado exitosamente. Úsala en tu próximo inicio de sesión.";
            } else {
                return "redirect:/admin/perfil?error=La contraseña actual ingresada es incorrecta. Inténtalo nuevamente.";
            }
        }
        return "redirect:/admin/perfil";
    }

    // --- GESTIÓN DE ADMINISTRADORES ---
    @GetMapping("/administradores")
    public String gestionAdmins(HttpSession session, Model model) {
        Integer miId = (Integer) session.getAttribute("usuarioId");
        MBVGAdministrador currentUser = adminRepo.findById(miId).orElse(null);
        
        model.addAttribute("administradores", adminRepo.findAll());
        model.addAttribute("miNivel", currentUser != null ? currentUser.getNivelAcceso() : "ADMIN");
        model.addAttribute("miId", miId);
        
        return "MBVGAdmin/MBVGgestionadmins";
    }

@PostMapping("/administradores/crear")
public String crearAdmin(@RequestParam String nombre,
                         @RequestParam String email,
                         @RequestParam(required = false) String telefono,
                         @RequestParam String nivelAcceso,
                         @RequestParam String password,
                         @RequestParam String confirmarPassword,
                         HttpSession session) {

    Integer miId = (Integer) session.getAttribute("usuarioId");

    if (miId == null) {
        return "redirect:/login-admin";
    }

    MBVGAdministrador currentUser = adminRepo.findById(miId).orElse(null);

    if (currentUser == null || !"SUPERADMIN".equals(currentUser.getNivelAcceso())) {
        return "redirect:/admin/administradores?error=No tienes permisos para crear nuevas cuentas.";
    }

    if (password == null || password.trim().length() < 6) {
        return "redirect:/admin/administradores?error=La contraseña debe tener como mínimo 6 caracteres.";
    }

    if (!password.equals(confirmarPassword)) {
        return "redirect:/admin/administradores?error=Las contraseñas ingresadas no coinciden.";
    }

    if (adminRepo.findByEmail(email) != null) {
        return "redirect:/admin/administradores?error=Ya existe un administrador registrado con ese correo.";
    }

    MBVGAdministrador administrador = new MBVGAdministrador();
    administrador.setNombre(nombre);
    administrador.setEmail(email);
    administrador.setTelefono(telefono);
    administrador.setNivelAcceso(nivelAcceso);
    administrador.setPassword(password);
    administrador.setEstado("activo");
    administrador.setFechaRegistro(new Date());

    adminRepo.save(administrador);

    return "redirect:/admin/administradores?exito=Administrador registrado correctamente.";
    }

    @PostMapping("/administradores/editar")
    public String editarAdmin(@RequestParam Integer id,
                              @RequestParam String nombre,
                              @RequestParam String email,
                              @RequestParam(required = false) String telefono,
                              @RequestParam String nivelAcceso,
                              @RequestParam String estado,
                              @RequestParam(required = false) String nuevaPassword,
                              @RequestParam(required = false) String confirmarPassword,
                              HttpSession session) {

        Integer miId = (Integer) session.getAttribute("usuarioId");

        if (miId == null) {
            return "redirect:/login-admin";
        }

        MBVGAdministrador currentUser = adminRepo.findById(miId).orElse(null);
        MBVGAdministrador existente = adminRepo.findById(id).orElse(null);

        if (existente == null || currentUser == null) {
            return "redirect:/admin/administradores?error=No se encontró el administrador seleccionado.";
        }

        if ("SUPERADMIN".equals(existente.getNivelAcceso())
                && !existente.getId().equals(miId)
                && !"SUPERADMIN".equals(currentUser.getNivelAcceso())) {
            return "redirect:/admin/administradores?error=No tienes permisos para modificar a un SUPERADMIN.";
        }

        MBVGAdministrador adminConMismoCorreo = adminRepo.findByEmail(email);

        if (adminConMismoCorreo != null && !adminConMismoCorreo.getId().equals(id)) {
            return "redirect:/admin/administradores?error=El correo ingresado ya pertenece a otro administrador.";
        }

        existente.setNombre(nombre);
        existente.setEmail(email);
        existente.setTelefono(telefono);
        existente.setEstado(estado);

        if ("SUPERADMIN".equals(currentUser.getNivelAcceso())) {
            existente.setNivelAcceso(nivelAcceso);
        }

        if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
            if (nuevaPassword.trim().length() < 6) {
                return "redirect:/admin/administradores?error=La nueva contraseña debe tener como mínimo 6 caracteres.";
            }

            if (!nuevaPassword.equals(confirmarPassword)) {
                return "redirect:/admin/administradores?error=La confirmación de contraseña no coincide.";
            }

            existente.setPassword(nuevaPassword);
        }

        adminRepo.save(existente);

        return "redirect:/admin/administradores?exito=Datos del administrador actualizados correctamente.";
    }
    @GetMapping("/administradores/eliminar")
public String eliminarAdmin(@RequestParam Integer id, HttpSession session) {

    Integer miId = (Integer) session.getAttribute("usuarioId");

    if (miId == null) {
        return "redirect:/login-admin";
    }

    MBVGAdministrador currentUser = adminRepo.findById(miId).orElse(null);

    if (currentUser == null || !"SUPERADMIN".equalsIgnoreCase(currentUser.getNivelAcceso())) {
        return "redirect:/admin/administradores?error=No tienes permisos para desactivar administradores.";
    }

    if (id.equals(miId)) {
        return "redirect:/admin/administradores?error=No puedes desactivar tu propia cuenta.";
    }

    MBVGAdministrador administrador = adminRepo.findById(id).orElse(null);

    if (administrador == null) {
        return "redirect:/admin/administradores?error=No se encontró el administrador seleccionado.";
    }

    administrador.setEstado("inactivo");
    adminRepo.save(administrador);

    return "redirect:/admin/administradores?exito=Administrador desactivado correctamente.";
}

    // --- GESTIÓN DE CONDUCTORES ---
    @GetMapping("/conductores")
    public String gestionConductores(Model model) {
        model.addAttribute("conductores", conductorRepo.findAll());
        return "MBVGAdmin/MBVGgestionconductores";
    }

    @PostMapping("/conductores/crear")
    public String crearConductor(@RequestParam String nombre,
                                  @RequestParam String email,
                                  @RequestParam String telefono,
                                  @RequestParam String licencia,
                                  @RequestParam String tipoVehiculo,
                                  @RequestParam String password,
                                  @RequestParam String confirmarPassword) {

        if (password == null || password.trim().length() < 6) {
            return "redirect:/admin/conductores?error=La contraseña debe tener como mínimo 6 caracteres.";
        }

        if (!password.equals(confirmarPassword)) {
            return "redirect:/admin/conductores?error=Las contraseñas ingresadas no coinciden.";
        }

        if (conductorRepo.existsByEmail(email)) {
            return "redirect:/admin/conductores?error=Ya existe un conductor registrado con ese correo.";
        }

        if (conductorRepo.existsByLicencia(licencia)) {
            return "redirect:/admin/conductores?error=Ya existe un conductor registrado con esa licencia.";
        }

        MBVGConductor conductor = new MBVGConductor();
        conductor.setNombre(nombre);
        conductor.setEmail(email);
        conductor.setTelefono(telefono);
        conductor.setLicencia(licencia);
        conductor.setTipoVehiculo(tipoVehiculo);
        conductor.setPassword(password);
        conductor.setEstado("disponible");
        conductor.setFechaRegistro(new Date());
        conductor.setPuntuacionPromedio(0.0);

        conductorRepo.save(conductor);

        return "redirect:/admin/conductores?exito=Conductor registrado correctamente.";
    }

    @PostMapping("/conductores/editar")
    public String editarConductor(@ModelAttribute MBVGConductor conductor,
                                  @RequestParam(required = false) String nuevaPassword,
                                  @RequestParam(required = false) String confirmarPassword) {

        MBVGConductor existente = conductorRepo.findById(conductor.getId()).orElse(null);

        if (existente == null) {
            return "redirect:/admin/conductores?error=No se encontró el conductor seleccionado.";
        }

        MBVGConductor conductorConMismoCorreo = conductorRepo.findByEmail(conductor.getEmail());

        if (conductorConMismoCorreo != null && !conductorConMismoCorreo.getId().equals(conductor.getId())) {
            return "redirect:/admin/conductores?error=El correo ingresado ya pertenece a otro conductor.";
        }

        for (MBVGConductor c : conductorRepo.findAll()) {
            if (c.getLicencia() != null
                    && conductor.getLicencia() != null
                    && c.getLicencia().equalsIgnoreCase(conductor.getLicencia())
                    && !c.getId().equals(conductor.getId())) {
                return "redirect:/admin/conductores?error=La licencia ingresada ya pertenece a otro conductor.";
            }
        }

        existente.setNombre(conductor.getNombre());
        existente.setEmail(conductor.getEmail());
        existente.setTelefono(conductor.getTelefono());
        existente.setLicencia(conductor.getLicencia());
        existente.setTipoVehiculo(conductor.getTipoVehiculo());
        existente.setEstado(conductor.getEstado());

        if (nuevaPassword != null && !nuevaPassword.trim().isEmpty()) {
            if (nuevaPassword.trim().length() < 6) {
                return "redirect:/admin/conductores?error=La nueva contraseña debe tener como mínimo 6 caracteres.";
            }

            if (!nuevaPassword.equals(confirmarPassword)) {
                return "redirect:/admin/conductores?error=La confirmación de contraseña no coincide.";
            }

            existente.setPassword(nuevaPassword);
        }

        conductorRepo.save(existente);

        return "redirect:/admin/conductores?exito=Conductor actualizado correctamente.";
    }
    @GetMapping("/conductores/eliminar")
public String eliminarConductor(@RequestParam Integer id) {

    MBVGConductor conductor = conductorRepo.findById(id).orElse(null);

    if (conductor == null) {
        return "redirect:/admin/conductores?error=No se encontró el conductor seleccionado.";
    }

    boolean tieneViajeActivo = reservaRepo.existsByConductorIdAndEstadoIn(
            id,
            List.of("asignada", "en_ruta")
    );

    if (tieneViajeActivo) {
        return "redirect:/admin/conductores?error=No se puede desactivar un conductor con viaje activo.";
    }

    conductor.setEstado("inactivo");
    conductorRepo.save(conductor);

    return "redirect:/admin/conductores?exito=Conductor desactivado correctamente.";
}

    // --- GESTIÓN DE FLOTA ---
    @GetMapping("/flota")
    public String flota(Model model) {
        model.addAttribute("vehiculos", vehiculoRepo.findAll());
        model.addAttribute("conductores", conductorRepo.findAll());
        return "MBVGAdmin/MBVGgestionflota";
    }

    @PostMapping("/flota/crear")
    public String crearVehiculo(@ModelAttribute MBVGVehiculo vehiculo) {
        vehiculo.setEstado("disponible");
        vehiculoRepo.save(vehiculo);
        return "redirect:/admin/flota?exito=Vehículo registrado exitosamente";
    }

    @PostMapping("/flota/editar")
    public String editarVehiculo(@ModelAttribute MBVGVehiculo vehiculo, @RequestParam(required = false) Integer conductorAsignado) {
        MBVGVehiculo existente = vehiculoRepo.findById(vehiculo.getId()).orElse(null);
        if (existente != null) {
            existente.setPlaca(vehiculo.getPlaca());
            existente.setMarca(vehiculo.getMarca());
            existente.setModelo(vehiculo.getModelo());
            existente.setAnio(vehiculo.getAnio());
            existente.setKilometraje(vehiculo.getKilometraje());
            existente.setTipoVehiculo(vehiculo.getTipoVehiculo());
            existente.setEstado(vehiculo.getEstado());
            existente.setConductorAsignado(conductorAsignado);
            vehiculoRepo.save(existente);
        }
        return "redirect:/admin/flota?exito=Vehículo actualizado correctamente";
    }

    @GetMapping("/flota/eliminar")
    public String eliminarVehiculo(@RequestParam Integer id) {
        vehiculoRepo.deleteById(id);
        return "redirect:/admin/flota?exito=Vehículo eliminado del sistema";
    }

// --- ASIGNACIÓN DE TRASLADOS ---
    @GetMapping("/asignacion")
    public String asignacion(Model model) {
        List<MBVGReserva> pendientes = reservaRepo.findAll().stream()
                .filter(r -> "pendiente".equalsIgnoreCase(r.getEstado()))
                .collect(Collectors.toList());

        List<MBVGConductor> disponibles = conductorRepo.findAll().stream()
                .filter(c -> "disponible".equalsIgnoreCase(c.getEstado()))
                .collect(Collectors.toList());

        model.addAttribute("reservasPendientes", pendientes);
        model.addAttribute("conductoresDisponibles", disponibles);
        model.addAttribute("googleMapsApiKey", googleMapsApiKey);
        model.addAttribute("tarifaKm", tarifaKm);

        return "MBVGAdmin/MBVGasignacion";
    }

    @PostMapping("/asignar")
    public String procesarAsignacion(@RequestParam(required = false) Integer reservaId, 
                                     @RequestParam(required = false) Integer conductorId,
                                     @RequestParam(required = false) Double distanciaMapeada) {

        if (reservaId == null || conductorId == null) {
            return "redirect:/admin/asignacion?error=Debe seleccionar una reserva y un conductor.";
        }

        if (distanciaMapeada == null || distanciaMapeada <= 0) {
            return "redirect:/admin/asignacion?error=Debe calcular o ingresar una distancia válida.";
        }

        MBVGReserva reserva = reservaRepo.findById(reservaId).orElse(null);

        if (reserva == null) {
            return "redirect:/admin/asignacion?error=Reserva no encontrada.";
        }

        MBVGConductor conductor = conductorRepo.findById(conductorId).orElse(null);

        if (conductor == null) {
            return "redirect:/admin/asignacion?error=Conductor no encontrado.";
        }

        if (!"disponible".equalsIgnoreCase(conductor.getEstado())) {
            return "redirect:/admin/asignacion?error=El conductor seleccionado ya no está disponible.";
        }

        boolean tieneViajeActivo = reservaRepo.existsByConductorIdAndEstadoIn(
                conductorId,
                List.of("asignada", "en_ruta")
        );

        if (tieneViajeActivo) {
            return "redirect:/admin/asignacion?error=Este conductor ya tiene un viaje activo.";
        }

        reservaService.asignarConductor(reservaId, conductorId, distanciaMapeada);

        conductor.setEstado("ocupado");
        conductorRepo.save(conductor);

        notificacionService.crearNotificacion(
                reserva.getClienteId(),
                "cliente",
                "Conductor asignado",
                "Tu traslado " + reserva.getOrigen() + " → " + reserva.getDestino() +
                        " fue asignado al conductor " + conductor.getNombre() + ".",
                "asignacion"
        );

        return "redirect:/admin/asignacion?exito=Traslado asignado correctamente.";
    }
        // --- VALIDACIÓN DE PAGOS ---
    @GetMapping("/pagos")
    public String gestionPagos(Model model) {
        model.addAttribute("pagosPendientes", pagoService.listarPagosPendientesValidacion());
        return "MBVGAdmin/MBVGpagos";
    }

    @PostMapping("/pagos/aprobar")
    public String aprobarPago(@RequestParam Integer pagoId, HttpSession session) {
        boolean aprobado = pagoService.aprobarPago(pagoId);

        if (aprobado) {
            session.setAttribute("mensajeExito", "Pago aprobado correctamente.");
        } else {
            session.setAttribute("mensajeError", "No se pudo aprobar el pago. Verifica que siga pendiente de validación.");
        }

        return "redirect:/admin/pagos";
    }

    @PostMapping("/pagos/rechazar")
    public String rechazarPago(@RequestParam Integer pagoId, HttpSession session) {
        boolean rechazado = pagoService.rechazarPago(pagoId);

        if (rechazado) {
            session.setAttribute("mensajeExito", "Pago rechazado correctamente.");
        } else {
            session.setAttribute("mensajeError", "No se pudo rechazar el pago. Verifica que siga pendiente de validación.");
        }

        return "redirect:/admin/pagos";
    }

    // --- REPORTES Y SOPORTE ---
    @GetMapping("/reportes")
    public String reportes(Model model) {
        List<MBVGReserva> reservas = reservaRepo.findAll();
        List<MBVGPago> pagos = pagoRepo.findAll();
        List<MBVGSoporte> tickets = soporteRepo.findAll();

        double totalIngresos = pagos.stream()
                .filter(p -> p.getEstado() != null && "completado".equalsIgnoreCase(p.getEstado()))
                .mapToDouble(p -> p.getMonto() != null ? p.getMonto() : 0.0)
                .sum();

        totalIngresos = Math.round(totalIngresos * 100.0) / 100.0;

        long viajesCompletados = reservas.stream()
                .filter(r -> r.getEstado() != null &&
                        ("completada".equalsIgnoreCase(r.getEstado()) || "completado".equalsIgnoreCase(r.getEstado())))
                .count();

        long viajesEnRuta = reservas.stream()
                .filter(r -> r.getEstado() != null && "en_ruta".equalsIgnoreCase(r.getEstado()))
                .count();

        long reservasPendientes = reservas.stream()
                .filter(r -> r.getEstado() != null && "pendiente".equalsIgnoreCase(r.getEstado()))
                .count();

        long pagosPendientes = pagos.stream()
                .filter(p -> p.getEstado() != null && "pendiente_validacion".equalsIgnoreCase(p.getEstado()))
                .count();

        long ticketsAbiertos = tickets.stream()
                .filter(t -> t.getEstado() != null && "abierto".equalsIgnoreCase(t.getEstado()))
                .count();

        long totalReservas = reservas.size();
        long totalPagosCompletados = pagos.stream()
                .filter(p -> p.getEstado() != null && "completado".equalsIgnoreCase(p.getEstado()))
                .count();

        String[] meses = {"Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

        Map<String, Double> ingresosPorMes = new LinkedHashMap<>();
        Map<String, String> etiquetasPorMes = new LinkedHashMap<>();

        Calendar inicio = Calendar.getInstance();
        inicio.set(Calendar.DAY_OF_MONTH, 1);
        inicio.add(Calendar.MONTH, -5);

        for (int i = 0; i < 6; i++) {
            Calendar mes = (Calendar) inicio.clone();
            mes.add(Calendar.MONTH, i);

            int anio = mes.get(Calendar.YEAR);
            int numeroMes = mes.get(Calendar.MONTH) + 1;
            String clave = anio + "-" + String.format("%02d", numeroMes);

            ingresosPorMes.put(clave, 0.0);
            etiquetasPorMes.put(clave, meses[numeroMes - 1] + " " + anio);
        }

        for (MBVGPago pago : pagos) {
            if (pago.getEstado() != null
                    && "completado".equalsIgnoreCase(pago.getEstado())
                    && pago.getFechaPago() != null) {

                Calendar fechaPago = Calendar.getInstance();
                fechaPago.setTime(pago.getFechaPago());

                int anio = fechaPago.get(Calendar.YEAR);
                int numeroMes = fechaPago.get(Calendar.MONTH) + 1;
                String clave = anio + "-" + String.format("%02d", numeroMes);

                if (ingresosPorMes.containsKey(clave)) {
                    double montoActual = ingresosPorMes.get(clave);
                    double montoPago = pago.getMonto() != null ? pago.getMonto() : 0.0;
                    ingresosPorMes.put(clave, Math.round((montoActual + montoPago) * 100.0) / 100.0);
                }
            }
        }

        Map<String, Long> tiposVehiculo = new LinkedHashMap<>();

        for (MBVGReserva reserva : reservas) {
            String tipo = reserva.getTipoVehiculo();

            if (tipo == null || tipo.isBlank()) {
                tipo = "Sin tipo";
            } else {
                tipo = tipo.substring(0, 1).toUpperCase() + tipo.substring(1).toLowerCase();
            }

            tiposVehiculo.put(tipo, tiposVehiculo.getOrDefault(tipo, 0L) + 1);
        }

        List<MBVGPago> ultimosPagos = pagos.stream()
                .filter(p -> p.getEstado() != null && "completado".equalsIgnoreCase(p.getEstado()))
                .sorted((a, b) -> {
                    if (a.getFechaPago() == null && b.getFechaPago() == null) return 0;
                    if (a.getFechaPago() == null) return 1;
                    if (b.getFechaPago() == null) return -1;
                    return b.getFechaPago().compareTo(a.getFechaPago());
                })
                .limit(5)
                .collect(Collectors.toList());

        List<MBVGReserva> ultimosViajes = reservas.stream()
                .filter(r -> r.getEstado() != null &&
                        ("completada".equalsIgnoreCase(r.getEstado()) || "completado".equalsIgnoreCase(r.getEstado())))
                .sorted((a, b) -> {
                    Date fechaA = a.getFechaTraslado() != null ? a.getFechaTraslado() : a.getFechaSolicitud();
                    Date fechaB = b.getFechaTraslado() != null ? b.getFechaTraslado() : b.getFechaSolicitud();

                    if (fechaA == null && fechaB == null) return 0;
                    if (fechaA == null) return 1;
                    if (fechaB == null) return -1;
                    return fechaB.compareTo(fechaA);
                })
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("totalIngresos", totalIngresos);
        model.addAttribute("viajesCompletados", viajesCompletados);
        model.addAttribute("viajesEnRuta", viajesEnRuta);
        model.addAttribute("reservasPendientes", reservasPendientes);
        model.addAttribute("pagosPendientes", pagosPendientes);
        model.addAttribute("ticketsAbiertos", ticketsAbiertos);
        model.addAttribute("totalReservas", totalReservas);
        model.addAttribute("totalPagosCompletados", totalPagosCompletados);

        model.addAttribute("ingresosMesLabels", new ArrayList<>(etiquetasPorMes.values()));
        model.addAttribute("ingresosMesData", new ArrayList<>(ingresosPorMes.values()));

        model.addAttribute("tiposVehiculoLabels", new ArrayList<>(tiposVehiculo.keySet()));
        model.addAttribute("tiposVehiculoData", new ArrayList<>(tiposVehiculo.values()));

        model.addAttribute("ultimosPagos", ultimosPagos);
        model.addAttribute("ultimosViajes", ultimosViajes);

        return "MBVGAdmin/MBVGreportes";
    }

    @GetMapping("/soporte")
    public String soporte(Model model) {
        model.addAttribute("tickets", soporteRepo.findAll());
        return "MBVGAdmin/MBVGsoporte";
    }

    @GetMapping("/soporte/detalle")
    public String detalleSoporte(@RequestParam Integer id, Model model) {
        MBVGSoporte ticket = soporteRepo.findById(id).orElse(null);
        if (ticket == null) {
            return "redirect:/admin/soporte";
        }
        model.addAttribute("ticket", ticket);
        return "MBVGAdmin/MBVGdetalleSoporte";
    }

    @PostMapping("/soporte/responder")
    public String responderTicket(@RequestParam Integer ticketId, @RequestParam String respuesta) {
        MBVGSoporte ticket = soporteRepo.findById(ticketId).orElse(null);
        if(ticket != null) {
            ticket.setRespuesta(respuesta);
            ticket.setEstado("resuelto");
            ticket.setFechaResolucion(new Date());
            soporteRepo.save(ticket);
        }
        return "redirect:/admin/soporte/detalle?id=" + ticketId + "&success=true";
    }
}