package com.mbvg.linomove.MBVGApi;

import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/microservicio/ia")
@CrossOrigin(origins = "*")
public class MBVGMicroservicioIaController {

    @GetMapping("/recomendar-categoria")
    public ResponseEntity<?> recomendarCategoria(@RequestParam String texto) {

        String textoAnalizado = normalizar(texto);

        if (textoAnalizado.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Debe ingresar datos del vehículo para analizar."
            ));
        }

        List<String> marcasPremium = List.of(
                "ferrari", "lamborghini", "porsche", "maserati",
                "bentley", "rolls royce", "jaguar", "tesla",
                "lexus", "audi", "bmw", "mercedes"
        );

        List<String> palabrasSuv = List.of(
                "suv", "camioneta", "hilux", "fortuner", "rav4",
                "jeep", "land cruiser", "prado", "4x4", "pickup"
        );

        List<String> palabrasComfort = List.of(
                "elantra", "corolla", "sentra", "civic",
                "mazda", "kia", "hyundai", "sedan", "cerato"
        );

        List<String> palabrasEconomico = List.of(
                "yaris", "picanto", "spark", "march",
                "etios", "rio", "accent", "aveo"
        );

        if (contieneAlguna(textoAnalizado, marcasPremium)) {
            return respuesta(
                    "premium",
                    0.95,
                    "Se detectó una marca de alta gama.",
                    texto
            );
        }

        if (contieneAlguna(textoAnalizado, palabrasSuv)) {
            return respuesta(
                    "suv",
                    0.90,
                    "Se detectó una camioneta o vehículo SUV.",
                    texto
            );
        }

        if (contieneAlguna(textoAnalizado, palabrasComfort)) {
            return respuesta(
                    "comfort",
                    0.80,
                    "Se detectó un vehículo de categoría intermedia.",
                    texto
            );
        }

        if (contieneAlguna(textoAnalizado, palabrasEconomico)) {
            return respuesta(
                    "economico",
                    0.80,
                    "Se detectó un vehículo de uso económico.",
                    texto
            );
        }

        return respuesta(
                "economico",
                0.55,
                "No se detectó una marca específica. Se recomienda revisión manual o categoría económica por defecto.",
                texto
        );
    }

    private boolean contieneAlguna(String texto, List<String> palabras) {
        return palabras.stream().anyMatch(texto::contains);
    }

    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }

        return Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .trim();
    }

    private ResponseEntity<?> respuesta(String categoria, Double confianza, String motivo, String textoOriginal) {
        return ResponseEntity.ok(Map.of(
                "modulo", "IA Linomove",
                "textoAnalizado", textoOriginal,
                "categoriaRecomendada", categoria,
                "confianza", confianza,
                "motivo", motivo
        ));
    }
}