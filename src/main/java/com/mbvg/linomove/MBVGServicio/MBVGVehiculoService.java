package com.mbvg.linomove.MBVGServicio;

import com.mbvg.linomove.MBVGEntidad.MBVGVehiculo;
import com.mbvg.linomove.MBVGRepositorio.MBVGVehiculoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class MBVGVehiculoService {

    @Autowired
    private MBVGVehiculoRepository vehiculoRepo;

    // crear o editar vehiculo
    public MBVGVehiculo guardar(MBVGVehiculo vehiculo) {
        return vehiculoRepo.save(vehiculo);
    }

    // trae toda la flota
    public List<MBVGVehiculo> listarTodos() {
        return vehiculoRepo.findAll();
    }

    // busca vehiculo por id
    public MBVGVehiculo obtenerPorId(Integer id) {
        return vehiculoRepo.findById(id).orElse(null);
    }

    // validar si la placa ya existe al registrar
    public boolean existePlaca(String placa) {
        return vehiculoRepo.existsByPlaca(placa);
    }

    // borra un vehiculo 
    public void eliminar(Integer id) {
        vehiculoRepo.deleteById(id);
    }

    // programa el mantenimiento del vehiculo
    public void programarMantenimiento(Integer id, Date fecha, String observaciones) {
        MBVGVehiculo vehiculo = obtenerPorId(id);
        if (vehiculo != null) {
            vehiculo.setProximoMantenimiento(fecha);
            vehiculo.setEstado("mantenimiento");
            
            // concatena la observacion nueva
            String obsActuales = vehiculo.getObservaciones() != null ? vehiculo.getObservaciones() : "";
            vehiculo.setObservaciones(obsActuales + " | Mantenimiento: " + observaciones);
            
            vehiculoRepo.save(vehiculo);
        }
    }
}