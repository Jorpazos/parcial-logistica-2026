package ar.edu.usal.logistica.exception;

/** Se lanza cuando se busca un chofer por DNI y no existe. */
public class ChoferNoEncontradoException extends LogisticaException {
    private static final long serialVersionUID = 1L;

    public ChoferNoEncontradoException(String dni) {
        super("No existe ningún chofer con DNI " + dni + ".");
    }
}