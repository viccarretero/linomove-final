package com.mbvg.linomove.MBVGControlador;

import com.mbvg.linomove.MBVGEntidad.MBVGCliente;
import com.mbvg.linomove.MBVGServicio.MBVGClienteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MBVGPublicoController {

    @Autowired
    private MBVGClienteService clienteService;

    // pagina principal
    @GetMapping("/")
    public String index() {
        return "MBVGindex";
    }

    // mostrar formulario de registro
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        // esto es lo que le dice al formulario a que objeto pertenecen los campos
        model.addAttribute("cliente", new MBVGCliente());
        return "MBVGCliente/MBVGregistro";
    }

    // procesar el registro del cliente
    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute MBVGCliente cliente) {
        // validacion basica antes de guardar
        if (cliente.getEmail() == null || cliente.getEmail().isEmpty()) {
            return "redirect:/registro?error=1";
        }
        
        // verificamos si ya existe el correo
        if (clienteService.existeEmail(cliente.getEmail())) {
            return "redirect:/registro?error=2";
        }
        
        // guardamos en la base de datos
        cliente.setEstado("activo");
        clienteService.guardar(cliente);
        
        return "redirect:/login-cliente?registro=exitoso";
    }
}