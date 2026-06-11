package com.mbvg.linomove;

import com.mbvg.linomove.MBVGEntidad.MBVGAdministrador;
import com.mbvg.linomove.MBVGRepositorio.MBVGAdministradorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class MBVGDataInitializer implements CommandLineRunner {

    @Autowired
    private MBVGAdministradorRepository adminRepo;

    @Override
    public void run(String... args) throws Exception {
        if (adminRepo.findByEmail("admin@linomove.com") == null) {
            MBVGAdministrador admin = new MBVGAdministrador();
            admin.setNombre("Gerald");
            admin.setEmail("admin@linomove.com");
            admin.setPassword("123456"); // Contraseña del admin
            admin.setNivelAcceso("SUPERADMIN");
            admin.setEstado("activo");
            admin.setFechaRegistro(new Date());
            adminRepo.save(admin);
        }
    }
}