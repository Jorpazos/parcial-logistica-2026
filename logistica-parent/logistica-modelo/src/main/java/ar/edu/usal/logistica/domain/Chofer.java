package ar.edu.usal.logistica.domain;

import ar.edu.usal.logistica.exception.ValidacionException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Chofer: hereda los datos de Persona y agrega categoría y camiones autorizados. */
public class Chofer extends Persona {

    private Categoria categoria;
    // La lista es privada: solo se modifica con autorizar() y se expone de solo lectura
    private final List<Camion> camionesAutorizados = new ArrayList<>();

    public Chofer(Long id, String nombre, String apellido, String dni,
                  LocalDate fechaNacimiento, Categoria categoria, String telefono) {
        super(id, nombre, apellido, dni, fechaNacimiento, telefono);
        this.categoria = categoria;
    }

    @Override
    public void validar() throws ValidacionException {
        super.validar();   // primero las reglas de Persona
        if (categoria == null) {
            throw new ValidacionException("La categoría es obligatoria.");
        }
        for (Camion camion : camionesAutorizados) {
            if (!categoria.admite(camion.getToneladasMaximas())) {
                throw new ValidacionException("La categoría " + categoria + " no permite manejar el camión "
                        + camion.getDescripcion() + ".");
            }
        }
    }

    /** Agrega un camión a los autorizados, sin repetirlo. */
    public void autorizar(Camion camion) {
        boolean yaEsta = camionesAutorizados.stream()
                .anyMatch(c -> c.getId() != null && c.getId().equals(camion.getId()));
        if (!yaEsta) {
            camionesAutorizados.add(camion);
        }
    }

    /** Puede manejarlo si está autorizado y su categoría alcanza para las toneladas. */
    public boolean puedeManejar(Camion camion) {
        boolean autorizado = camionesAutorizados.stream()
                .anyMatch(c -> c.getId() != null && c.getId().equals(camion.getId()));
        return autorizado && categoria.admite(camion.getToneladasMaximas());
    }

    public Categoria getCategoria() { return categoria; }

    public List<Camion> getCamionesAutorizados() {
        return Collections.unmodifiableList(camionesAutorizados);
    }
}