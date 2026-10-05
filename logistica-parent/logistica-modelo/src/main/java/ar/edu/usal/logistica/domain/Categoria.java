package ar.edu.usal.logistica.domain;

/** Categoría de licencia: define cuántas toneladas puede transportar el chofer. */
public enum Categoria {
    A(10), B(20), C(30);

    private final int toneladasMaximas;

    Categoria(int toneladasMaximas) {
        this.toneladasMaximas = toneladasMaximas;
    }

    public int getToneladasMaximas() {
        return toneladasMaximas;
    }

    /** Indica si un camión de esas toneladas lo puede manejar esta categoría. */
    public boolean admite(double toneladas) {
        return toneladas <= toneladasMaximas;
    }
}