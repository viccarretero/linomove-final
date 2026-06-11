package com.mbvg.linomove.MBVGSeguridad;

import com.mbvg.linomove.MBVGEntidad.*;
import com.mbvg.linomove.MBVGRepositorio.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class MBVGAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired private MBVGClienteRepository clienteRepo;
    @Autowired private MBVGConductorRepository conductorRepo;
    @Autowired private MBVGAdministradorRepository adminRepo;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        HttpSession session = request.getSession();
        String email = authentication.getName();
        String role = authentication.getAuthorities().iterator().next().getAuthority();

        if (role.equals("ROLE_ADMIN")) {
            MBVGAdministrador admin = adminRepo.findByEmail(email);
            session.setAttribute("usuarioId", admin.getId());
            session.setAttribute("usuarioNombre", admin.getNombre());
            response.sendRedirect("/admin/dashboard");
        } else if (role.equals("ROLE_CONDUCTOR")) {
            MBVGConductor cond = conductorRepo.findByEmail(email);
            session.setAttribute("usuarioId", cond.getId());
            session.setAttribute("usuarioNombre", cond.getNombre());
            response.sendRedirect("/conductor/dashboard");
        } else {
            MBVGCliente cli = clienteRepo.findByEmail(email);
            session.setAttribute("usuarioId", cli.getId());
            session.setAttribute("usuarioNombre", cli.getNombre());
            response.sendRedirect("/cliente/dashboard");
        }
    }
}