package com.mbvg.linomove.MBVGServicio;

import com.mbvg.linomove.MBVGEntidad.MBVGCliente;
import com.mbvg.linomove.MBVGRepositorio.MBVGClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MBVGClienteService {

    @Autowired
    private MBVGClienteRepository clienteRepo;

    @Transactional
    public MBVGCliente guardar(MBVGCliente cliente) {
        return clienteRepo.save(cliente);
    }

    public boolean existeEmail(String email) {
        return clienteRepo.existsByEmail(email);
    }

    public List<MBVGCliente> listarTodos() {
        return clienteRepo.findAll();
    }

    public MBVGCliente obtenerPorId(Integer id) {
        return clienteRepo.findById(id).orElse(null);
    }
}