package ar.edu.usal.logistica.domain;

/* Ciclo de vida de un viaje*/
public enum EstadoViaje {
    ASIGNADO("Asignado"),
    EN_CURSO("En curso"),
    FINALIZADO("Finalizado");

    private final String descripcion;

    EstadoViaje(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }
}