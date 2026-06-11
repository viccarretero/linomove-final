package com.mbvg.linomove.MBVGUtil;

import org.springframework.stereotype.Service;

@Service
public class MBVGCaptchaService {

    // valida el check de google recaptcha v2
    public boolean validar(String recaptchaResponse) {
        if (recaptchaResponse == null || recaptchaResponse.isEmpty()) {
            return false;
        }
        
        // TODO: compañero implementara la peticion http a la api de google siteverify aqui
        // se enviara el token recibido y la clave secreta del servidor
        
        return true; // retorno true simulado
    }
}