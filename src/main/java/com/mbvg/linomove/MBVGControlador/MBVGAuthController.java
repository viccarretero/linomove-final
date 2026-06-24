package com.mbvg.linomove.MBVGControlador;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MBVGAuthController {

    @Value("${google.recaptcha.site-key:}")
    private String recaptchaSiteKey;

    @GetMapping("/login-cliente")
    public String loginCliente(Model model) {
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        return "MBVGCliente/MBVGlogin";
    }

    @GetMapping("/login-conductor")
    public String loginConductor(Model model) {
        model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
        return "MBVGConductor/MBVGlogin";
    }

    // Panel admin web deshabilitado.
    // La administración se realizará desde el sistema de escritorio.
    @GetMapping("/login-admin")
    public String loginAdmin() {
        return "redirect:/";
    }
}