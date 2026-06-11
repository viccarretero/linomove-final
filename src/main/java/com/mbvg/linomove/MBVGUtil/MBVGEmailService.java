package com.mbvg.linomove.MBVGUtil;

import com.mbvg.linomove.MBVGEntidad.MBVGReserva;
import org.springframework.stereotype.Service;

@Service
public class MBVGEmailService {

    // TODO: compañero inyectara private JavaMailSender mailSender; (usando spring-boot-starter-mail)

    // envia la confirmacion al cliente
    public boolean enviarConfirmacionReserva(String emailCliente, MBVGReserva reserva) {
        try {
            // TODO: compañero armara el MimeMessage aqui con el diseño html original
            // mailSender.send(message);
            
            System.out.println("email de reserva simulado para: " + emailCliente);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // notifica al cliente que su conductor va en camino
    public boolean enviarConfirmacionConductor(String emailCliente, MBVGReserva reserva, String nombreConductor) {
        try {
            // TODO: compañero armara y enviara el correo de asignacion de conductor
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}