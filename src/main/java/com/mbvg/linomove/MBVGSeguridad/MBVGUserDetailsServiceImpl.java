package com.mbvg.linomove.MBVGSeguridad;

import com.mbvg.linomove.MBVGEntidad.MBVGAdministrador;
import com.mbvg.linomove.MBVGEntidad.MBVGCliente;
import com.mbvg.linomove.MBVGEntidad.MBVGConductor;
import com.mbvg.linomove.MBVGRepositorio.MBVGAdministradorRepository;
import com.mbvg.linomove.MBVGRepositorio.MBVGClienteRepository;
import com.mbvg.linomove.MBVGRepositorio.MBVGConductorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MBVGUserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private MBVGClienteRepository clienteRepo;

    @Autowired
    private MBVGConductorRepository conductorRepo;

    @Autowired
    private MBVGAdministradorRepository adminRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        MBVGAdministrador admin = adminRepo.findByEmail(email);
        if (admin != null) {
            return User.builder()
                    .username(admin.getEmail())
                    .password(admin.getPassword())
                    .authorities("ROLE_ADMIN")
                    .build();
        }

        MBVGConductor conductor = conductorRepo.findByEmail(email);
        if (conductor != null) {
            return User.builder()
                    .username(conductor.getEmail())
                    .password(conductor.getPassword())
                    .authorities("ROLE_CONDUCTOR")
                    .build();
        }

        MBVGCliente cliente = clienteRepo.findByEmail(email);
        if (cliente != null) {
            return User.builder()
                    .username(cliente.getEmail())
                    .password(cliente.getPassword())
                    .authorities("ROLE_CLIENTE")
                    .build();
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + email);
    }
}