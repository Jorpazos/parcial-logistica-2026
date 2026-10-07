package ar.edu.usal.logistica.domain;

import ar.edu.usal.logistica.exception.ValidacionException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Viaje {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Long id;
    private final Chofer chofer;
    private final Camion camion;
    private final Destino origen;
    private final Destino destino;
    private final EstimacionViaje estimacion;
    private EstadoViaje estado;
    private LocalDateTime fechaCarga;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    public Viaje(Long id, Chofer chofer, Camion camion, Destino origen, Destino destino,
                 EstimacionViaje estimacion, EstadoViaje estado,
                 LocalDateTime fechaCarga, LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.id = id;
        this.chofer = chofer;
        this.camion = camion;
        this.origen = origen;
        this.destino = destino;
        this.estimacion = estimacion;
        this.estado = estado;
        this.fechaCarga = fechaCarga;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    /* Crea un viaje nuevo en estado ASIGNADO */
    public static Viaje nuevo(Chofer chofer, Camion camion, Destino origen, Destino destino, int distanciaKm) {
        EstimacionViaje estimacion = CalculadoraViaje.calcular(camion, distanciaKm);
        return new Viaje(null, chofer, camion, origen, destino, estimacion,
                EstadoViaje.ASIGNADO, LocalDateTime.now(), null, null);
    }

    /* Reglas que debe cumplir un viaje antes de guardarse. */
    public void validar() throws ValidacionException {
        if (chofer == null || chofer.getId() == null) {
            throw new ValidacionException("Debe seleccionar un chofer.");
        }
        if (camion == null || camion.getId() == null) {
            throw new ValidacionException("Debe seleccionar un camión.");
        }
        if (origen == null || destino == null) {
            throw new ValidacionException("Debe seleccionar origen y destino.");
        }
        if (origen == destino) {
            throw new ValidacionException("El origen y el destino no pueden ser iguales.");
        }
        if (!chofer.puedeManejar(camion)) {
            throw new ValidacionException("El chofer no está autorizado a manejar ese camión.");
        }
    }

    public boolean puedeIniciarse() { return estado == EstadoViaje.ASIGNADO; }
    public boolean puedeFinalizarse() { return estado == EstadoViaje.EN_CURSO; }

    public String getFechaCargaFormateada() { return formatear(fechaCarga); }
    public String getFechaInicioFormateada() { return formatear(fechaInicio); }
    public String getFechaFinFormateada() { return formatear(fechaFin); }

    private static String formatear(LocalDateTime fecha) {
        return fecha == null ? "-" : fecha.format(FORMATO);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Chofer getChofer() { return chofer; }
    public Camion getCamion() { return camion; }
    public Destino getOrigen() { return origen; }
    public Destino getDestino() { return destino; }
    public EstimacionViaje getEstimacion() { return estimacion; }
    public EstadoViaje getEstado() { return estado; }
    public LocalDateTime getFechaCarga() { return fechaCarga; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
}