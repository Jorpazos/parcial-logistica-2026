package ar.edu.usal.logistica.domain;

import ar.edu.usal.logistica.exception.ValidacionException;


public class Camion {

    private Long id;
    private String marca;
    private String modelo;
    private String dominio;
    private double toneladasMaximas;
    private double capacidadTanqueLitros;
    private double consumoLitrosPorKm;

    public Camion(Long id, String marca, String modelo, String dominio,
                  double toneladasMaximas, double capacidadTanqueLitros, double consumoLitrosPorKm) {
        this.id = id;
        this.marca = marca == null ? null : marca.trim();
        this.modelo = modelo == null ? null : modelo.trim();
        this.dominio = dominio == null ? null : dominio.trim().toUpperCase();
        this.toneladasMaximas = toneladasMaximas;
        this.capacidadTanqueLitros = capacidadTanqueLitros;
        this.consumoLitrosPorKm = consumoLitrosPorKm;
    }

    public void validar() throws ValidacionException {
        if (marca == null || marca.isEmpty()) {
            throw new ValidacionException("La marca es obligatoria.");
        }
        if (modelo == null || modelo.isEmpty()) {
            throw new ValidacionException("El modelo es obligatorio.");
        }
        if (dominio == null || !dominio.matches("[A-Z0-9]{6,7}")) {
            throw new ValidacionException("El dominio debe tener 6 o 7 letras y números (ej: AB123CD).");
        }
        if (toneladasMaximas <= 0) {
            throw new ValidacionException("Las toneladas máximas deben ser mayores a cero.");
        }
        if (capacidadTanqueLitros <= 0) {
            throw new ValidacionException("La capacidad del tanque debe ser mayor a cero.");
        }
        if (consumoLitrosPorKm <= 0) {
            throw new ValidacionException("El consumo por km debe ser mayor a cero.");
        }
    }

    /* Litros de combustible necesarios para recorrer una distancia */
    public double litrosNecesarios(int distanciaKm) {
        return distanciaKm * consumoLitrosPorKm;
    }

    public String getDescripcion() {
        return marca + " " + modelo + " (" + dominio + ")";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public String getDominio() { return dominio; }
    public double getToneladasMaximas() { return toneladasMaximas; }
    public double getCapacidadTanqueLitros() { return capacidadTanqueLitros; }
    public double getConsumoLitrosPorKm() { return consumoLitrosPorKm; }
}