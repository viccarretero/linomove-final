package com.mbvg.linomove.MBVGApi;

import com.mbvg.linomove.MBVGEntidad.MBVGCliente;
import com.mbvg.linomove.MBVGEntidad.MBVGConductor;
import com.mbvg.linomove.MBVGEntidad.MBVGReserva;
import com.mbvg.linomove.MBVGEntidad.MBVGPago;
import com.mbvg.linomove.MBVGRepositorio.MBVGClienteRepository;
import com.mbvg.linomove.MBVGRepositorio.MBVGConductorRepository;
import com.mbvg.linomove.MBVGRepositorio.MBVGReservaRepository;
import com.mbvg.linomove.MBVGRepositorio.MBVGPagoRepository;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mbvg.linomove.MBVGServicio.MBVGReservaService;
import com.mbvg.linomove.MBVGServicio.MBVGConductorService;
import com.mbvg.linomove.MBVGServicio.MBVGPagoService;


@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class MBVGApiController {
    
    
    @Autowired
    private MBVGReservaService reservaService;
    
    @Autowired
    private MBVGConductorService conductorService;
    
    @Autowired
    private MBVGPagoService pagoService;
    
    @Autowired
    private MBVGReservaRepository reservaRepo;

    @Autowired
    private MBVGConductorRepository conductorRepo;

    @Autowired
    private MBVGClienteRepository clienteRepo;

    @Autowired
    private MBVGPagoRepository pagoRepo;

    // =====================================================
    // ENDPOINT DE PRUEBA
    // =====================================================

    @GetMapping("/salud")
    public ResponseEntity<?> salud() {
        return ResponseEntity.ok(Map.of(
                "estado", "OK",
                "mensaje", "API REST de LinoMove funcionando correctamente"
        ));
    }

    // =====================================================
    // RESERVAS
    // =====================================================
    @PostMapping("/reservas/{reservaId}/asignar")
    public ResponseEntity<?> asignarConductorAReserva(@PathVariable Integer reservaId,
                                                      @RequestParam Integer conductorId,
                                                      @RequestParam Double distanciaKm) {

        MBVGReserva reserva = reservaRepo.findById(reservaId).orElse(null);

        if (reserva == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Reserva no encontrada",
                    "reservaId", reservaId
            ));
        }

        if (!"pendiente".equalsIgnoreCase(reserva.getEstado())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "La reserva no está pendiente",
                    "estadoActual", reserva.getEstado()
            ));
        }

        MBVGConductor conductor = conductorRepo.findById(conductorId).orElse(null);

        if (conductor == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Conductor no encontrado",
                    "conductorId", conductorId
            ));
        }

        if (!"disponible".equalsIgnoreCase(conductor.getEstado())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "El conductor no está disponible",
                    "estadoActual", conductor.getEstado()
            ));
        }

        boolean tieneViajeActivo = reservaRepo.existsByConductorIdAndEstadoIn(
                conductorId,
                List.of("asignada", "en_ruta")
        );

        if (tieneViajeActivo) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Este conductor ya tiene un viaje activo"
            ));
        }

        if (distanciaKm == null || distanciaKm <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "La distancia debe ser mayor a cero"
            ));
        }

        reservaService.asignarConductor(reservaId, conductorId, distanciaKm);

        conductor.setEstado("ocupado");
        conductorRepo.save(conductor);

        MBVGReserva reservaActualizada = reservaRepo.findById(reservaId).orElse(null);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Reserva asignada correctamente desde API REST",
                "reserva", reservaActualizada
        ));
    }
    // =====================================================
// FLUJO OPERATIVO DEL CONDUCTOR POR API REST
// =====================================================

@PostMapping("/reservas/{reservaId}/iniciar")
public ResponseEntity<?> iniciarViajePorApi(@PathVariable Integer reservaId,
                                            @RequestParam Integer conductorId) {

    MBVGReserva reserva = reservaRepo.findById(reservaId).orElse(null);

    if (reserva == null) {
        return ResponseEntity.status(404).body(Map.of(
                "error", "Reserva no encontrada",
                "reservaId", reservaId
        ));
    }

    MBVGConductor conductor = conductorRepo.findById(conductorId).orElse(null);

    if (conductor == null) {
        return ResponseEntity.status(404).body(Map.of(
                "error", "Conductor no encontrado",
                "conductorId", conductorId
        ));
    }

    if (reserva.getConductorId() == null || !reserva.getConductorId().equals(conductorId)) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "La reserva no está asignada a este conductor"
        ));
    }

    if (!"asignada".equalsIgnoreCase(reserva.getEstado())) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Solo se puede iniciar una reserva en estado asignada",
                "estadoActual", reserva.getEstado()
        ));
    }

    boolean iniciado = conductorService.iniciarViaje(conductorId, reservaId);

    if (!iniciado) {
        return ResponseEntity.badRequest().body(Map.of(
                "error", "No se pudo iniciar el viaje"
        ));
    }

    MBVGReserva reservaActualizada = reservaRepo.findById(reservaId).orElse(null);
    MBVGConductor conductorActualizado = conductorRepo.findById(conductorId).orElse(null);

    return ResponseEntity.ok(Map.of(
            "mensaje", "Viaje iniciado correctamente desde API REST",
            "reserva", reservaActualizada,
            "conductor", conductorActualizado
    ));
}

    @PostMapping("/reservas/{reservaId}/finalizar")
    public ResponseEntity<?> finalizarViajePorApi(@PathVariable Integer reservaId,
                                                  @RequestParam Integer conductorId) {

        MBVGReserva reserva = reservaRepo.findById(reservaId).orElse(null);

        if (reserva == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Reserva no encontrada",
                    "reservaId", reservaId
            ));
        }

        MBVGConductor conductor = conductorRepo.findById(conductorId).orElse(null);

        if (conductor == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Conductor no encontrado",
                    "conductorId", conductorId
            ));
        }

        if (reserva.getConductorId() == null || !reserva.getConductorId().equals(conductorId)) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "La reserva no está asignada a este conductor"
            ));
        }

        if (!"en_ruta".equalsIgnoreCase(reserva.getEstado())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Solo se puede finalizar una reserva en estado en_ruta",
                    "estadoActual", reserva.getEstado()
            ));
        }

        boolean finalizado = conductorService.finalizarViaje(conductorId, reservaId);

        if (!finalizado) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No se pudo finalizar el viaje"
            ));
        }

        MBVGReserva reservaActualizada = reservaRepo.findById(reservaId).orElse(null);
        MBVGConductor conductorActualizado = conductorRepo.findById(conductorId).orElse(null);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Viaje finalizado correctamente desde API REST",
                "reserva", reservaActualizada,
                "conductor", conductorActualizado
        ));
    }
    @GetMapping("/reservas")
    public ResponseEntity<List<MBVGReserva>> listarReservas() {
        return ResponseEntity.ok(reservaRepo.findAll());
    }

    @GetMapping("/reservas/{id}")
    public ResponseEntity<?> obtenerReservaPorId(@PathVariable Integer id) {
        MBVGReserva reserva = reservaRepo.findById(id).orElse(null);

        if (reserva == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Reserva no encontrada",
                    "id", id
            ));
        }

        return ResponseEntity.ok(reserva);
    }

    @GetMapping("/reservas/estado/{estado}")
    public ResponseEntity<List<MBVGReserva>> listarReservasPorEstado(@PathVariable String estado) {
        return ResponseEntity.ok(reservaRepo.findByEstadoOrderByFechaSolicitudDesc(estado));
    }

    @PostMapping("/reservas")
    public ResponseEntity<?> crearReserva(@RequestBody MBVGReserva reserva) {
        if (reserva.getEstado() == null || reserva.getEstado().isBlank()) {
            reserva.setEstado("pendiente");
        }

        MBVGReserva reservaGuardada = reservaRepo.save(reserva);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Reserva creada correctamente",
                "reserva", reservaGuardada
        ));
    }

    @PutMapping("/reservas/{id}/estado")
    public ResponseEntity<?> actualizarEstadoReserva(@PathVariable Integer id,
                                                     @RequestParam String estado) {
        MBVGReserva reserva = reservaRepo.findById(id).orElse(null);

        if (reserva == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Reserva no encontrada",
                    "id", id
            ));
        }

        reserva.setEstado(estado);
        reservaRepo.save(reserva);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Estado de reserva actualizado correctamente",
                "reservaId", id,
                "nuevoEstado", estado
        ));
    }

    // =====================================================
    // CONDUCTORES
    // =====================================================

    @GetMapping("/conductores")
    public ResponseEntity<List<MBVGConductor>> listarConductores() {
        return ResponseEntity.ok(conductorRepo.findAll());
    }

    @GetMapping("/conductores/{id}")
    public ResponseEntity<?> obtenerConductorPorId(@PathVariable Integer id) {
        MBVGConductor conductor = conductorRepo.findById(id).orElse(null);

        if (conductor == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Conductor no encontrado",
                    "id", id
            ));
        }

        return ResponseEntity.ok(conductor);
    }

    @GetMapping("/conductores/disponibles/{tipoVehiculo}")
    public ResponseEntity<List<MBVGConductor>> listarConductoresDisponiblesPorTipo(@PathVariable String tipoVehiculo) {
        return ResponseEntity.ok(
                conductorRepo.findByEstadoAndTipoVehiculoOrderByPuntuacionPromedioDesc(
                        "disponible",
                        tipoVehiculo
                )
        );
    }

    @PutMapping("/conductores/{id}/estado")
    public ResponseEntity<?> actualizarEstadoConductor(@PathVariable Integer id,
                                                       @RequestParam String estado) {
        MBVGConductor conductor = conductorRepo.findById(id).orElse(null);

        if (conductor == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Conductor no encontrado",
                    "id", id
            ));
        }

        conductor.setEstado(estado);
        conductorRepo.save(conductor);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Estado del conductor actualizado correctamente",
                "conductorId", id,
                "nuevoEstado", estado
        ));
    }

    // =====================================================
    // CLIENTES
    // =====================================================

    @GetMapping("/clientes")
    public ResponseEntity<List<MBVGCliente>> listarClientes() {
        return ResponseEntity.ok(clienteRepo.findAll());
    }

    @GetMapping("/clientes/{id}")
    public ResponseEntity<?> obtenerClientePorId(@PathVariable Integer id) {
        MBVGCliente cliente = clienteRepo.findById(id).orElse(null);

        if (cliente == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Cliente no encontrado",
                    "id", id
            ));
        }

        return ResponseEntity.ok(cliente);
    }

    // =====================================================
    // PAGOS
    // =====================================================

    @GetMapping("/pagos")
    public ResponseEntity<List<MBVGPago>> listarPagos() {
        return ResponseEntity.ok(pagoRepo.findAll());
    }

    @GetMapping("/pagos/{id}")
    public ResponseEntity<?> obtenerPagoPorId(@PathVariable Integer id) {
        MBVGPago pago = pagoRepo.findById(id).orElse(null);

        if (pago == null) {
            return ResponseEntity.status(404).body(Map.of(
                    "error", "Pago no encontrado",
                    "id", id
            ));
        }

        return ResponseEntity.ok(pago);
    }
        // =====================================================
    // PAGOS POR API REST
    // =====================================================

    @GetMapping("/pagos/pendientes-validacion")
    public ResponseEntity<?> listarPagosPendientesValidacion() {
        return ResponseEntity.ok(pagoService.listarPagosPendientesValidacion());
    }

    @PostMapping("/pagos/{pagoId}/aprobar")
    public ResponseEntity<?> aprobarPagoPorApi(@PathVariable Integer pagoId) {
        boolean aprobado = pagoService.aprobarPago(pagoId);

        if (!aprobado) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No se pudo aprobar el pago",
                    "detalle", "Verifica que el pago exista y esté en estado pendiente_validacion",
                    "pagoId", pagoId
            ));
        }

        MBVGPago pagoActualizado = pagoRepo.findById(pagoId).orElse(null);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Pago aprobado correctamente desde API REST",
                "pago", pagoActualizado
        ));
    }

    @PostMapping("/pagos/{pagoId}/rechazar")
    public ResponseEntity<?> rechazarPagoPorApi(@PathVariable Integer pagoId) {
        boolean rechazado = pagoService.rechazarPago(pagoId);

        if (!rechazado) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "No se pudo rechazar el pago",
                    "detalle", "Verifica que el pago exista y esté en estado pendiente_validacion",
                    "pagoId", pagoId
            ));
        }

        MBVGPago pagoActualizado = pagoRepo.findById(pagoId).orElse(null);

        return ResponseEntity.ok(Map.of(
                "mensaje", "Pago rechazado correctamente desde API REST",
                "pago", pagoActualizado
        ));
    }
}