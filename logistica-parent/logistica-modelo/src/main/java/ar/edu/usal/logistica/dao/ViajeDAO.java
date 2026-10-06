package ar.edu.usal.logistica.dao;

import ar.edu.usal.logistica.domain.Viaje;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.exception.ValidacionException;

import java.util.List;

public interface ViajeDAO {

    /** Guarda el viaje verificando, en la misma transacción, que el camión siga libre. */
    void insertar(Viaje viaje) throws DAOException, ValidacionException;

    List<Viaje> listarTodos() throws DAOException;

    List<Viaje> listarPorChofer(long choferId) throws DAOException;

    /** El chofer marca que inicia un viaje ASIGNADO. */
    void iniciar(long viajeId, long choferId) throws DAOException, ValidacionException;

    /** El chofer marca como realizado un viaje EN_CURSO. */
    void finalizar(long viajeId, long choferId) throws DAOException, ValidacionException;
}