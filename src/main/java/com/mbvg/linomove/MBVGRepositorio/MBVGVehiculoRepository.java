package com.mbvg.linomove.MBVGRepositorio;

import com.mbvg.linomove.MBVGEntidad.MBVGVehiculo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MBVGVehiculoRepository extends JpaRepository<MBVGVehiculo, Integer> {
    // validacion al crear vehiculo
    boolean existsByPlaca(String placa);
    
    // filtros para la gestion de flota
    List<MBVGVehiculo> findByEstadoOrderByPlacaAsc(String estado);
    List<MBVGVehiculo> findByTipoVehiculoOrderByPlacaAsc(String tipoVehiculo);
}