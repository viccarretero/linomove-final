package com.mbvg.linomove.MBVGControlador;

import java.util.regex.Pattern;
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
    private static final Pattern PLACA_VALIDA = Pattern.compile("^[A-Z0-9-]{6,10}$");
    private static final Pattern TEXTO_VEHICULO_VALIDO = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9 .-]{1,50}$");
    private static final Pattern EMAIL_VALIDO = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFONO_PERU = Pattern.compile("^9\\d{8}$");
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{3,80}$");
    private static final Pattern LICENCIA_VALIDA = Pattern.compile("^[A-Za-z0-9-]{6,20}$");
    
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
                             @RequestParam String telefono,
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

        nombre = limpiar(nombre);
        email = limpiar(email).toLowerCase();
        telefono = limpiar(telefono);
        nivelAcceso = limpiar(nivelAcceso).toUpperCase();
        password = limpiar(password);
        confirmarPassword = limpiar(confirmarPassword);

        String errorValidacion = validarDatosAdmin(nombre, email, telefono, nivelAcceso);

        if (errorValidacion != null) {
            return "redirect:/admin/administradores?error=" + errorValidacion;
        }

        if (password.length() < 6 || password.length() > 30) {
            return "redirect:/admin/administradores?error=La contraseña debe tener entre 6 y 30 caracteres.";
        }

        if (!password.equals(confirmarPassword)) {
            return "redirect:/admin/administradores?error=Las contraseñas ingresadas no coinciden.";
        }

        if (correoAdministradorExiste(email, null)) {
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
                              @RequestParam String telefono,
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

        nombre = limpiar(nombre);
        email = limpiar(email).toLowerCase();
        telefono = limpiar(telefono);
        nivelAcceso = limpiar(nivelAcceso).toUpperCase();
        estado = limpiar(estado).toLowerCase();

        if (!"SUPERADMIN".equals(currentUser.getNivelAcceso())) {
            nivelAcceso = existente.getNivelAcceso();
        }

        String errorValidacion = validarDatosAdmin(nombre, email, telefono, nivelAcceso);

        if (errorValidacion != null) {
            return "redirect:/admin/administradores?error=" + errorValidacion;
        }

        if (!estadoAdminValido(estado)) {
            return "redirect:/admin/administradores?error=Debes seleccionar un estado válido.";
        }

        if (id.equals(miId) && !"activo".equals(estado)) {
            return "redirect:/admin/administradores?error=No puedes desactivar tu propia cuenta.";
        }

        if (id.equals(miId)
                && "SUPERADMIN".equals(currentUser.getNivelAcceso())
                && !"SUPERADMIN".equals(nivelAcceso)) {
            return "redirect:/admin/administradores?error=No puedes quitarte tu propio rol de SUPERADMIN.";
        }

        if (correoAdministradorExiste(email, id)) {
            return "redirect:/admin/administradores?error=El correo ingresado ya pertenece a otro administrador.";
        }

        existente.setNombre(nombre);
        existente.setEmail(email);
        existente.setTelefono(telefono);
        existente.setEstado(estado);

        if ("SUPERADMIN".equals(currentUser.getNivelAcceso())) {
            existente.setNivelAcceso(nivelAcceso);
        }

        nuevaPassword = limpiar(nuevaPassword);
        confirmarPassword = limpiar(confirmarPassword);

        if (!nuevaPassword.isEmpty()) {
            if (nuevaPassword.length() < 6 || nuevaPassword.length() > 30) {
                return "redirect:/admin/administradores?error=La nueva contraseña debe tener entre 6 y 30 caracteres.";
            }

            if (!nuevaPassword.equals(confirmarPassword)) {
                return "redirect:/admin/administradores?error=La confirmación de contraseña no coincide.";
            }

            existente.setPassword(nuevaPassword);
        }

        if (nuevaPassword.isEmpty() && !confirmarPassword.isEmpty()) {
            return "redirect:/admin/administradores?error=Para confirmar una contraseña primero debes escribir la nueva contraseña.";
        }

        adminRepo.save(existente);

        if (id.equals(miId)) {
            session.setAttribute("usuarioNombre", nombre);
        }

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
private String validarDatosAdmin(String nombre, String email, String telefono, String nivelAcceso) {
    if (nombre.isEmpty()) {
        return "El nombre completo es obligatorio.";
    }

    if (!NOMBRE_VALIDO.matcher(nombre).matches()) {
        return "El nombre debe tener entre 3 y 80 caracteres y solo debe contener letras y espacios.";
    }

    if (email.isEmpty()) {
        return "El correo electrónico es obligatorio.";
    }

    if (email.length() > 100 || !EMAIL_VALIDO.matcher(email).matches()) {
        return "El correo ingresado no tiene un formato válido.";
    }

    if (telefono.isEmpty()) {
        return "El teléfono es obligatorio.";
    }

    if (!TELEFONO_PERU.matcher(telefono).matches()) {
        return "El teléfono debe tener 9 dígitos y empezar con 9.";
    }

    if (!nivelAdminValido(nivelAcceso)) {
        return "Debes seleccionar un nivel de acceso válido.";
    }

    return null;
}

private boolean nivelAdminValido(String nivelAcceso) {
    return "ADMIN".equals(nivelAcceso) || "SUPERADMIN".equals(nivelAcceso);
}

private boolean estadoAdminValido(String estado) {
    return "activo".equals(estado) || "inactivo".equals(estado);
}

private boolean correoAdministradorExiste(String email, Integer idExcluir) {
    for (MBVGAdministrador admin : adminRepo.findAll()) {
        if (admin.getEmail() != null
                && email.equalsIgnoreCase(admin.getEmail().trim())
                && (idExcluir == null || !admin.getId().equals(idExcluir))) {
            return true;
        }
    }

    return false;
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

        nombre = limpiar(nombre);
        email = limpiar(email).toLowerCase();
        telefono = limpiar(telefono);
        licencia = limpiar(licencia).toUpperCase();
        tipoVehiculo = limpiar(tipoVehiculo).toLowerCase();

        String errorValidacion = validarDatosConductor(nombre, email, telefono, licencia, tipoVehiculo);

        if (errorValidacion != null) {
            return "redirect:/admin/conductores?error=" + errorValidacion;
        }

        password = limpiar(password);
        confirmarPassword = limpiar(confirmarPassword);

        if (password.length() < 6 || password.length() > 30) {
            return "redirect:/admin/conductores?error=La contraseña debe tener entre 6 y 30 caracteres.";
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

        String nombre = limpiar(conductor.getNombre());
        String email = limpiar(conductor.getEmail()).toLowerCase();
        String telefono = limpiar(conductor.getTelefono());
        String licencia = limpiar(conductor.getLicencia()).toUpperCase();
        String tipoVehiculo = limpiar(conductor.getTipoVehiculo()).toLowerCase();
        String estado = limpiar(conductor.getEstado()).toLowerCase();

        String errorValidacion = validarDatosConductor(nombre, email, telefono, licencia, tipoVehiculo);

        if (errorValidacion != null) {
            return "redirect:/admin/conductores?error=" + errorValidacion;
        }

        if (!estadoConductorValido(estado)) {
            return "redirect:/admin/conductores?error=Debes seleccionar un estado válido para el conductor.";
        }

        MBVGConductor conductorConMismoCorreo = conductorRepo.findByEmail(email);

        if (conductorConMismoCorreo != null && !conductorConMismoCorreo.getId().equals(conductor.getId())) {
            return "redirect:/admin/conductores?error=El correo ingresado ya pertenece a otro conductor.";
        }

        for (MBVGConductor c : conductorRepo.findAll()) {
            if (c.getLicencia() != null
                    && licencia.equalsIgnoreCase(limpiar(c.getLicencia()))
                    && !c.getId().equals(conductor.getId())) {
                return "redirect:/admin/conductores?error=La licencia ingresada ya pertenece a otro conductor.";
            }
        }

        existente.setNombre(nombre);
        existente.setEmail(email);
        existente.setTelefono(telefono);
        existente.setLicencia(licencia);
        existente.setTipoVehiculo(tipoVehiculo);
        existente.setEstado(estado);

        nuevaPassword = limpiar(nuevaPassword);
        confirmarPassword = limpiar(confirmarPassword);

        if (!nuevaPassword.isEmpty()) {
            if (nuevaPassword.length() < 6 || nuevaPassword.length() > 30) {
                return "redirect:/admin/conductores?error=La nueva contraseña debe tener entre 6 y 30 caracteres.";
            }

            if (!nuevaPassword.equals(confirmarPassword)) {
                return "redirect:/admin/conductores?error=La confirmación de contraseña no coincide.";
            }

            existente.setPassword(nuevaPassword);
        }

        if (nuevaPassword.isEmpty() && !confirmarPassword.isEmpty()) {
            return "redirect:/admin/conductores?error=Para confirmar una contraseña primero debes escribir la nueva contraseña.";
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
    private String validarDatosConductor(String nombre, String email, String telefono, String licencia, String tipoVehiculo) {
        if (nombre.isEmpty()) {
            return "El nombre completo es obligatorio.";
        }

        if (!NOMBRE_VALIDO.matcher(nombre).matches()) {
            return "El nombre debe tener entre 3 y 80 caracteres y solo debe contener letras y espacios.";
        }

        if (email.isEmpty()) {
            return "El correo electrónico es obligatorio.";
        }

        if (email.length() > 100 || !EMAIL_VALIDO.matcher(email).matches()) {
            return "El correo ingresado no tiene un formato válido.";
        }

        if (telefono.isEmpty()) {
            return "El teléfono es obligatorio.";
        }

        if (!TELEFONO_PERU.matcher(telefono).matches()) {
            return "El teléfono debe tener 9 dígitos y empezar con 9.";
        }

        if (licencia.isEmpty()) {
            return "La licencia es obligatoria.";
        }

        if (!LICENCIA_VALIDA.matcher(licencia).matches()) {
            return "La licencia debe tener entre 6 y 20 caracteres. Puede contener letras, números y guion.";
        }

        if (!tipoVehiculoValido(tipoVehiculo)) {
            return "Debes seleccionar un tipo de vehículo válido.";
        }

        return null;
    }

    private boolean tipoVehiculoValido(String tipoVehiculo) {
        return "economico".equals(tipoVehiculo)
                || "comfort".equals(tipoVehiculo)
                || "premium".equals(tipoVehiculo)
                || "suv".equals(tipoVehiculo);
    }

    private boolean estadoConductorValido(String estado) {
        return "disponible".equals(estado)
                || "ocupado".equals(estado)
                || "inactivo".equals(estado);
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }

    // --- GESTIÓN DE FLOTA ---
    @GetMapping("/flota")
    public String gestionFlota() {
        return "redirect:/admin/conductores";
    }

    @PostMapping("/flota/crear")
    public String crearVehiculo(@RequestParam String placa,
                                @RequestParam String marca,
                                @RequestParam String modelo,
                                @RequestParam Integer anio,
                                @RequestParam Double kilometraje,
                                @RequestParam String tipoVehiculo,
                                @RequestParam String estado,
                                @RequestParam(required = false) Integer conductorAsignado) {

        placa = limpiar(placa).toUpperCase();
        marca = limpiar(marca);
        modelo = limpiar(modelo);
        tipoVehiculo = limpiar(tipoVehiculo).toLowerCase();
        estado = limpiar(estado).toLowerCase();

        String errorValidacion = validarDatosVehiculo(
                placa,
                marca,
                modelo,
                anio,
                kilometraje,
                tipoVehiculo,
                estado,
                conductorAsignado,
                null
        );

        if (errorValidacion != null) {
            return "redirect:/admin/flota?error=" + errorValidacion;
        }

        if (placaVehiculoExiste(placa, null)) {
            return "redirect:/admin/flota?error=Ya existe un vehículo registrado con esa placa.";
        }

        MBVGVehiculo vehiculo = new MBVGVehiculo();
        vehiculo.setPlaca(placa);
        vehiculo.setMarca(marca);
        vehiculo.setModelo(modelo);
        vehiculo.setAnio(anio);
        vehiculo.setKilometraje(kilometraje);
        vehiculo.setTipoVehiculo(tipoVehiculo);
        vehiculo.setEstado(estado);
        vehiculo.setConductorAsignado(conductorAsignado);

        vehiculoRepo.save(vehiculo);

        return "redirect:/admin/flota?exito=Vehículo registrado exitosamente.";
    }

    @PostMapping("/flota/editar")
    public String editarVehiculo(@RequestParam Integer id,
                                 @RequestParam String placa,
                                 @RequestParam String marca,
                                 @RequestParam String modelo,
                                 @RequestParam Integer anio,
                                 @RequestParam Double kilometraje,
                                 @RequestParam String tipoVehiculo,
                                 @RequestParam String estado,
                                 @RequestParam(required = false) Integer conductorAsignado) {

        MBVGVehiculo existente = vehiculoRepo.findById(id).orElse(null);

        if (existente == null) {
            return "redirect:/admin/flota?error=No se encontró el vehículo seleccionado.";
        }

        placa = limpiar(placa).toUpperCase();
        marca = limpiar(marca);
        modelo = limpiar(modelo);
        tipoVehiculo = limpiar(tipoVehiculo).toLowerCase();
        estado = limpiar(estado).toLowerCase();

        String errorValidacion = validarDatosVehiculo(
                placa,
                marca,
                modelo,
                anio,
                kilometraje,
                tipoVehiculo,
                estado,
                conductorAsignado,
                id
        );

        if (errorValidacion != null) {
            return "redirect:/admin/flota?error=" + errorValidacion;
        }

        if (placaVehiculoExiste(placa, id)) {
            return "redirect:/admin/flota?error=La placa ingresada ya pertenece a otro vehículo.";
        }

        existente.setPlaca(placa);
        existente.setMarca(marca);
        existente.setModelo(modelo);
        existente.setAnio(anio);
        existente.setKilometraje(kilometraje);
        existente.setTipoVehiculo(tipoVehiculo);
        existente.setEstado(estado);
        existente.setConductorAsignado(conductorAsignado);

        vehiculoRepo.save(existente);

        return "redirect:/admin/flota?exito=Vehículo actualizado correctamente.";
    }

    @GetMapping("/flota/eliminar")
    public String eliminarVehiculo(@RequestParam Integer id) {
        MBVGVehiculo vehiculo = vehiculoRepo.findById(id).orElse(null);

        if (vehiculo == null) {
            return "redirect:/admin/flota?error=No se encontró el vehículo seleccionado.";
        }

        if ("ocupado".equalsIgnoreCase(limpiar(vehiculo.getEstado()))) {
            return "redirect:/admin/flota?error=No se puede eliminar un vehículo que está ocupado.";
        }

        vehiculoRepo.deleteById(id);

        return "redirect:/admin/flota?exito=Vehículo eliminado del sistema.";
    }

    private String validarDatosVehiculo(String placa,
                                        String marca,
                                        String modelo,
                                        Integer anio,
                                        Double kilometraje,
                                        String tipoVehiculo,
                                        String estado,
                                        Integer conductorAsignado,
                                        Integer vehiculoIdExcluir) {

        if (placa.isEmpty()) {
            return "La placa del vehículo es obligatoria.";
        }

        if (!PLACA_VALIDA.matcher(placa).matches()) {
            return "La placa debe tener entre 6 y 10 caracteres. Puede contener letras, números y guion.";
        }

        if (marca.isEmpty()) {
            return "La marca del vehículo es obligatoria.";
        }

        if (marca.length() < 2 || marca.length() > 50 || !TEXTO_VEHICULO_VALIDO.matcher(marca).matches()) {
            return "La marca debe tener entre 2 y 50 caracteres válidos.";
        }

        if (modelo.isEmpty()) {
            return "El modelo del vehículo es obligatorio.";
        }

        if (modelo.length() > 50 || !TEXTO_VEHICULO_VALIDO.matcher(modelo).matches()) {
            return "El modelo debe tener máximo 50 caracteres válidos.";
        }

        int anioActual = Calendar.getInstance().get(Calendar.YEAR);

        if (anio == null || anio < 1990 || anio > anioActual + 1) {
            return "El año debe estar entre 1990 y " + (anioActual + 1) + ".";
        }

        if (kilometraje == null || kilometraje < 0 || kilometraje > 1000000) {
            return "El kilometraje debe ser mayor o igual a 0 y menor o igual a 1,000,000.";
        }

        if (!tipoVehiculoValido(tipoVehiculo)) {
            return "Debes seleccionar un tipo de vehículo válido.";
        }

        if (!estadoVehiculoValido(estado)) {
            return "Debes seleccionar un estado válido para el vehículo.";
        }

        if (conductorAsignado != null) {
            MBVGConductor conductor = conductorRepo.findById(conductorAsignado).orElse(null);

            if (conductor == null) {
                return "El conductor asignado no existe.";
            }

            if ("inactivo".equalsIgnoreCase(limpiar(conductor.getEstado()))) {
                return "No puedes asignar un conductor inactivo al vehículo.";
            }

            String tipoConductor = limpiar(conductor.getTipoVehiculo()).toLowerCase();

            if (!tipoVehiculo.equals(tipoConductor)) {
                return "El tipo de vehículo no coincide con el tipo que maneja el conductor asignado.";
            }

            if (conductorYaTieneVehiculo(conductorAsignado, vehiculoIdExcluir)) {
                return "El conductor seleccionado ya tiene otro vehículo asignado.";
            }
        }

        return null;
    }

    private boolean estadoVehiculoValido(String estado) {
        return "disponible".equals(estado)
                || "ocupado".equals(estado)
                || "mantenimiento".equals(estado);
    }

    private boolean placaVehiculoExiste(String placa, Integer idExcluir) {
        for (MBVGVehiculo vehiculo : vehiculoRepo.findAll()) {
            if (vehiculo.getPlaca() != null
                    && placa.equalsIgnoreCase(limpiar(vehiculo.getPlaca()))
                    && (idExcluir == null || !vehiculo.getId().equals(idExcluir))) {
                return true;
            }
        }

        return false;
    }

    private boolean conductorYaTieneVehiculo(Integer conductorId, Integer vehiculoIdExcluir) {
        for (MBVGVehiculo vehiculo : vehiculoRepo.findAll()) {
            if (vehiculo.getConductorAsignado() != null
                    && vehiculo.getConductorAsignado().equals(conductorId)
                    && (vehiculoIdExcluir == null || !vehiculo.getId().equals(vehiculoIdExcluir))) {
                return true;
            }
        }

        return false;
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
        String tipoReserva = limpiar(reserva.getTipoVehiculo()).toLowerCase();
        String tipoConductor = limpiar(conductor.getTipoVehiculo()).toLowerCase();

        if (!tipoReserva.equals(tipoConductor)) {
            return "redirect:/admin/asignacion?error=El conductor seleccionado no corresponde a la categoría del vehículo del cliente.";
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
        @PostMapping("/asignacion/rechazar")
    public String rechazarSolicitud(@RequestParam Integer reservaId,
                                    @RequestParam(required = false) String motivo) {

        MBVGReserva reserva = reservaRepo.findById(reservaId).orElse(null);

        if (reserva == null) {
            return "redirect:/admin/asignacion?error=No se encontró la solicitud seleccionada.";
        }

        if (!"pendiente".equalsIgnoreCase(limpiar(reserva.getEstado()))) {
            return "redirect:/admin/asignacion?error=Solo se pueden rechazar solicitudes pendientes.";
        }

        motivo = limpiar(motivo);

        if (motivo.isEmpty()) {
            motivo = "La ruta solicitada no se encuentra dentro de la cobertura del servicio.";
        }

        reserva.setEstado("rechazada");
        reservaRepo.save(reserva);

        notificacionService.crearNotificacion(
                reserva.getClienteId(),
                "cliente",
                "Solicitud rechazada",
                "Tu solicitud de traslado " + reserva.getOrigen() + " → " + reserva.getDestino()
                        + " fue rechazada. Motivo: " + motivo,
                "rechazo"
        );

        return "redirect:/admin/asignacion?exito=Solicitud rechazada correctamente.";
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