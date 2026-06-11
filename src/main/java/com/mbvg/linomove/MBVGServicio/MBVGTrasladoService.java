package com.mbvg.linomove.MBVGServicio;

import com.mbvg.linomove.MBVGEntidad.MBVGTraslado;
import com.mbvg.linomove.MBVGRepositorio.MBVGTrasladoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class MBVGTrasladoService {

    @Autowired
    private MBVGTrasladoRepository trasladoRepo;

    // inicia el viaje
    public MBVGTraslado iniciar(Integer reservaId, String polyline, Double distancia) {
        MBVGTraslado traslado = new MBVGTraslado();
        traslado.setReservaId(reservaId);
        traslado.setEstado("en_curso");
        traslado.setFechaInicio(new Date());
        traslado.setPolylineRuta(polyline);
        traslado.setDistanciaTotal(distancia);
        return trasladoRepo.save(traslado);
    }

    // obtiene coordenadas optimas
    public String obtenerRutaOptimizada(String origen, String destino) {
        // TODO: compañero integrara el API de Google Maps o Waze aqui para devolver el polyline real
        return "ruta-simulada-por-ahora";
    }

    // finaliza el recorrido
    public void finalizarTraslado(Integer trasladoId) {
        MBVGTraslado traslado = trasladoRepo.findById(trasladoId).orElse(null);
        if (traslado != null) {
            traslado.setEstado("completado");
            traslado.setFechaFin(new Date());
            trasladoRepo.save(traslado);
        }
    }
}