package com.mbvg.linomove.MBVGServicio;

import com.mbvg.linomove.MBVGEntidad.MBVGAdministrador;
import com.mbvg.linomove.MBVGRepositorio.MBVGAdministradorRepository;
import com.mbvg.linomove.MBVGRepositorio.MBVGClienteRepository;
import com.mbvg.linomove.MBVGRepositorio.MBVGConductorRepository;
import com.mbvg.linomove.MBVGRepositorio.MBVGReservaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class MBVGAdministradorService {

    @Autowired
    private MBVGAdministradorRepository adminRepo;

    // inyectamos otros repositorios para sacar las estadisticas rapidas
    @Autowired
    private MBVGClienteRepository clienteRepo;

    @Autowired
    private MBVGConductorRepository conductorRepo;

    @Autowired
    private MBVGReservaRepository reservaRepo;

    // guarda o actualiza un admin
    public MBVGAdministrador guardar(MBVGAdministrador admin) {
        return adminRepo.save(admin);
    }

    // trae todos los administradores
    public List<MBVGAdministrador> listarTodos() {
        return adminRepo.findAll();
    }

    // busca por email para el login
    public MBVGAdministrador buscarPorEmail(String email) {
        return adminRepo.findByEmail(email);
    }

    // metodos de estadisticas para el dashboard del admin
    public long contarClientesActivos() {
        // TODO: compañero puede afinar este count filtrando por estado activo si desea
        return clienteRepo.count();
    }

    public long contarConductores() {
        return conductorRepo.count();
    }

    public long contarReservas() {
        return reservaRepo.count();
    }
}