package com.mbvg.linomove.MBVGRepositorio;

import com.mbvg.linomove.MBVGEntidad.MBVGConductor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MBVGConductorRepository extends JpaRepository<MBVGConductor, Integer> {
    // busca conductor para login
    MBVGConductor findByEmail(String email);
    
    // validaciones de registro
    boolean existsByEmail(String email);
    boolean existsByLicencia(String licencia);
    
    // trae conductores disponibles segun el tipo de vehiculo y ordenados por su puntaje
    List<MBVGConductor> findByEstadoAndTipoVehiculoOrderByPuntuacionPromedioDesc(String estado, String tipoVehiculo);
}