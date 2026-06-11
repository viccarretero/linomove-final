package com.mbvg.linomove.MBVGServicio;

import com.mbvg.linomove.MBVGEntidad.MBVGSoporte;
import com.mbvg.linomove.MBVGRepositorio.MBVGSoporteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MBVGSoporteService {

    @Autowired
    private MBVGSoporteRepository soporteRepo;

    @Transactional
    public MBVGSoporte crearTicket(MBVGSoporte ticket) {
        ticket.setEstado("abierto");
        return soporteRepo.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<MBVGSoporte> misTickets(Integer clienteId) {
        return soporteRepo.findByClienteIdOrderByFechaCreacionDesc(clienteId);
    }

    // NUEVO MÉTODO: Trae un solo ticket por su ID
    @Transactional(readOnly = true)
    public MBVGSoporte obtenerTicketPorId(Integer id) {
        return soporteRepo.findById(id).orElse(null);
    }
}