package com.mbvg.linomove.MBVGUtil;

import org.springframework.stereotype.Component;

@Component
public class MBVGGeoLocalizacion {

    // obtiene coordenadas desde una direccion
    public double[] obtenerCoordenadas(String direccion) {
        double[] coordenadas = new double[2];
        
        try {
            // TODO: compañero conectara con el api de Nominatim u OpenStreetMap aqui
            
            // coordenadas simuladas por defecto
            coordenadas[0] = -18.0056; 
            coordenadas[1] = -70.2461;
            
        } catch (Exception e) {
            coordenadas[0] = -12.046374;
            coordenadas[1] = -77.042793;
        }
        
        return coordenadas;
    }

    // calcula la distancia en linea recta con la formula de haversine
    public double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // radio de la tierra
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c; 
    }

    // calcula el tiempo aproximado del viaje segun el vehiculo
    public int calcularTiempoEstimado(double distanciaKm, String tipoVehiculo) {
        double velocidadPromedio = 40.0;
        
        if (tipoVehiculo != null) {
            switch (tipoVehiculo.toLowerCase()) {
                case "gr a": velocidadPromedio = 35.0; break;
                case "transportador": velocidadPromedio = 38.0; break;
            }
        }
        
        double tiempoHoras = distanciaKm / velocidadPromedio;
        // añade 25% por temas de trafico
        return (int) Math.ceil(tiempoHoras * 60 * 1.25);
    }
}