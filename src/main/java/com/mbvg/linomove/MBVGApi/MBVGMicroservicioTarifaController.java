package com.mbvg.linomove.MBVGApi;

import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/microservicio/tarifa")
@CrossOrigin(origins = "*")
public class MBVGMicroservicioTarifaController {

    @Value("${linomove.tarifa.km:3.10}")
    private Double tarifaKm;

    @GetMapping("/calcular")
    public ResponseEntity<?> calcularTarifa(@RequestParam Double distanciaKm) {

        if (distanciaKm == null || distanciaKm <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "La distancia debe ser mayor a cero"
            ));
        }

        double total = distanciaKm * tarifaKm;
        total = Math.round(total * 100.0) / 100.0;

        return ResponseEntity.ok(Map.of(
                "servicio", "Microservicio de cálculo de tarifa Linomove",
                "distanciaKm", distanciaKm,
                "tarifaKm", tarifaKm,
                "moneda", "PEN",
                "total", total
        ));
    }
}