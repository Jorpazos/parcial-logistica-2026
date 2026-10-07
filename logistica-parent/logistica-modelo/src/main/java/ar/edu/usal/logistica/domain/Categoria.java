package ar.edu.usal.logistica.domain;

/** Categoría de licencia: define cuántas toneladas puede transportar el chofer. */
public enum Categoria {
    C1(12), C2(24), C3(Integer.MAX_VALUE);   // C3: más de 24 t, sin tope

    private final int toneladasMaximas;

    Categoria(int toneladasMaximas) {
        this.toneladasMaximas = toneladasMaximas;
    }

    /** Texto para mostrar en pantalla. */
    public String getDescripcion() {
        return this == C3 ? "más de 24 t" : "hasta " + toneladasMaximas + " t";
    }

    public int getToneladasMaximas() {
        return toneladasMaximas;
    }

    /** Indica si un camión de esas toneladas lo puede manejar esta categoría. */
    public boolean admite(double toneladas) {
        return toneladas <= toneladasMaximas;
    }
}