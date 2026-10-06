package ar.edu.usal.logistica.exception;


public class LogisticaException extends Exception {
    private static final long serialVersionUID = 1L;

    public LogisticaException(String mensaje) {
        super(mensaje);
    }

    public LogisticaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}