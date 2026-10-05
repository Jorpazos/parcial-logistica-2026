package ar.edu.usal.logistica.domain;

/** Cálculo de tiempo y tanques de un viaje, según el enunciado. */
public final class CalculadoraViaje {

    /** Kilómetros que recorre el camión por día (dato del enunciado). */
    public static final int KM_POR_DIA = 200;

    private CalculadoraViaje() {
    }

    public static EstimacionViaje calcular(Camion camion, int distanciaKm) {
        if (distanciaKm <= 0) {
            throw new IllegalArgumentException("La distancia debe ser mayor a cero.");
        }
        int dias = (int) Math.ceil((double) distanciaKm / KM_POR_DIA);
        double litros = camion.litrosNecesarios(distanciaKm);
        int tanques = (int) Math.ceil(litros / camion.getCapacidadTanqueLitros());
        return new EstimacionViaje(distanciaKm, dias, litros, tanques);
    }
}