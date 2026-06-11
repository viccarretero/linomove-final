package com.mbvg.linomove.MBVGRepositorio;

import com.mbvg.linomove.MBVGEntidad.MBVGAdministrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MBVGAdministradorRepository extends JpaRepository<MBVGAdministrador, Integer> {
    // busca admin para el login
    MBVGAdministrador findByEmail(String email);
}