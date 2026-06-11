package com.mbvg.linomove.MBVGRepositorio;

import com.mbvg.linomove.MBVGEntidad.MBVGCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MBVGClienteRepository extends JpaRepository<MBVGCliente, Integer> {
    // busca cliente para login
    MBVGCliente findByEmail(String email);
    
    // valida si el correo ya esta registrado
    boolean existsByEmail(String email);
}