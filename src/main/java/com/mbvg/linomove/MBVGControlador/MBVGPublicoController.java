package com.mbvg.linomove.MBVGControlador;

import com.mbvg.linomove.MBVGEntidad.MBVGCliente;
import com.mbvg.linomove.MBVGServicio.MBVGClienteService;
import java.util.regex.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MBVGPublicoController {

    @Autowired
    private MBVGClienteService clienteService;

    private static final Pattern EMAIL_VALIDO = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFONO_PERU = Pattern.compile("^9\\d{8}$");
    private static final Pattern DNI_VALIDO = Pattern.compile("^\\d{8}$");
    private static final Pattern NOMBRE_VALIDO = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{3,80}$");

    @GetMapping("/")
    public String index() {
        return "MBVGindex";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("cliente", new MBVGCliente());
        return "MBVGCliente/MBVGregistro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute("cliente") MBVGCliente cliente,
                                   @RequestParam(required = false) String confirmarPassword,
                                   Model model) {

        String errorValidacion = validarDatosCliente(cliente, confirmarPassword);

        if (errorValidacion != null) {
            model.addAttribute("errorRegistro", errorValidacion);
            return "MBVGCliente/MBVGregistro";
        }

        cliente.setNombre(limpiar(cliente.getNombre()));
        cliente.setDocumentoIdentidad(limpiar(cliente.getDocumentoIdentidad()));
        cliente.setEmail(limpiar(cliente.getEmail()).toLowerCase());
        cliente.setTelefono(limpiar(cliente.getTelefono()));
        cliente.setDireccion(limpiar(cliente.getDireccion()));

        if (clienteService.existeEmail(cliente.getEmail())) {
            model.addAttribute("errorRegistro", "El correo ingresado ya se encuentra registrado.");
            return "MBVGCliente/MBVGregistro";
        }

        cliente.setEstado("activo");
        clienteService.guardar(cliente);

        return "redirect:/login-cliente?registro=exitoso";
    }

    private String validarDatosCliente(MBVGCliente cliente, String confirmarPassword) {
        String nombre = limpiar(cliente.getNombre());
        String documento = limpiar(cliente.getDocumentoIdentidad());
        String email = limpiar(cliente.getEmail());
        String password = limpiar(cliente.getPassword());
        String telefono = limpiar(cliente.getTelefono());
        String direccion = limpiar(cliente.getDireccion());

        if (nombre.isEmpty()) {
            return "El nombre completo es obligatorio.";
        }

        if (!NOMBRE_VALIDO.matcher(nombre).matches()) {
            return "El nombre debe tener entre 3 y 80 caracteres y solo debe contener letras y espacios.";
        }

        if (documento.isEmpty()) {
            return "El documento de identidad es obligatorio.";
        }

        if (!DNI_VALIDO.matcher(documento).matches()) {
            return "El DNI debe tener exactamente 8 números.";
        }

        if (email.isEmpty()) {
            return "El correo electrónico es obligatorio.";
        }

        if (email.length() > 100 || !EMAIL_VALIDO.matcher(email).matches()) {
            return "El correo ingresado no tiene un formato válido.";
        }

        if (password.isEmpty()) {
            return "La contraseña es obligatoria.";
        }

        if (password.length() < 6 || password.length() > 30) {
            return "La contraseña debe tener entre 6 y 30 caracteres.";
        }

        if (confirmarPassword == null || confirmarPassword.trim().isEmpty()) {
            return "Debes confirmar la contraseña.";
        }

        if (!password.equals(confirmarPassword.trim())) {
            return "Las contraseñas ingresadas no coinciden.";
        }

        if (telefono.isEmpty()) {
            return "El teléfono es obligatorio.";
        }

        if (!TELEFONO_PERU.matcher(telefono).matches()) {
            return "El teléfono debe tener 9 dígitos y empezar con 9.";
        }

        if (direccion.isEmpty()) {
            return "La dirección es obligatoria.";
        }

        if (direccion.length() > 120) {
            return "La dirección no puede superar los 120 caracteres.";
        }

        return null;
    }

    private String limpiar(String valor) {
        return valor == null ? "" : valor.trim();
    }
}