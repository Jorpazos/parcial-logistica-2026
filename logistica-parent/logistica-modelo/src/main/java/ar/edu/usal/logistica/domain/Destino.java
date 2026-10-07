package ar.edu.usal.logistica.domain;

/* Ciudades a las que transporta la empresa  */
public enum Destino {
    CABA("CABA"),
    CORDOBA("Córdoba"),
    CORRIENTES("Corrientes"),
    FORMOSA("Formosa"),
    LA_PLATA("La Plata"),
    LA_RIOJA("La Rioja"),
    MENDOZA("Mendoza"),
    NEUQUEN("Neuquén");

    private final String nombre;

    Destino(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}