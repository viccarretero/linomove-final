package com.mbvg.linomove.MBVGSeguridad;

import com.mbvg.linomove.MBVGServicio.MBVGRecaptchaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class MBVGRecaptchaFilter extends OncePerRequestFilter {

    @Autowired
    private MBVGRecaptchaService recaptchaService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String ruta = request.getServletPath();
        String metodo = request.getMethod();

        if ("/procesar-login".equals(ruta) && "POST".equalsIgnoreCase(metodo)) {

            String captchaToken = request.getParameter("g-recaptcha-response");
            String tipoUsuario = request.getParameter("tipoUsuario");

            boolean captchaValido = recaptchaService.validar(captchaToken);

            if (!captchaValido) {
                response.sendRedirect(obtenerLoginPorTipo(tipoUsuario) + "?captcha=error");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String obtenerLoginPorTipo(String tipoUsuario) {
        if ("admin".equalsIgnoreCase(tipoUsuario)) {
            return "/login-admin";
        }

        if ("conductor".equalsIgnoreCase(tipoUsuario)) {
            return "/login-conductor";
        }

        return "/login-cliente";
    }
}