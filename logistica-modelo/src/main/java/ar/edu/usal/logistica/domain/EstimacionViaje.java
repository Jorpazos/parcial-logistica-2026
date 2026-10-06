package ar.edu.usal.logistica.domain;

/** Resultado del cálculo de un viaje */
public final class EstimacionViaje {

    private final int distanciaKm;
    private final int dias;
    private final double litrosTotales;
    private final int tanques;

    public EstimacionViaje(int distanciaKm, int dias, double litrosTotales, int tanques) {
        this.distanciaKm = distanciaKm;
        this.dias = dias;
        this.litrosTotales = litrosTotales;
        this.tanques = tanques;
    }

    public int getDistanciaKm() { return distanciaKm; }
    public int getDias() { return dias; }
    public double getLitrosTotales() { return litrosTotales; }
    public int getTanques() { return tanques; }
}