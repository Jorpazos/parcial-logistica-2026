package ar.edu.usal.logistica.dao;

import ar.edu.usal.logistica.domain.Chofer;
import ar.edu.usal.logistica.exception.ChoferNoEncontradoException;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.exception.ValidacionException;

import java.util.List;

public interface ChoferDAO {

    /* Da de alta el chofer, sus camiones autorizados y su usuario */
    void insertar(Chofer chofer, String claveInicial) throws DAOException, ValidacionException;

    /* Modifica el chofer y reemplaza sus camiones autorizados */
    void actualizar(Chofer chofer) throws DAOException, ValidacionException;

    /* Elimina el chofer, su usuario y sus autorizaciones (falla si tiene viajes) */
    void eliminar(long id) throws DAOException, ValidacionException;

    Chofer buscarPorId(long id) throws DAOException;

    /* si no existe un chofer con ese DNI */
    Chofer buscarPorDni(String dni) throws DAOException, ChoferNoEncontradoException;

    List<Chofer> listarTodos() throws DAOException;
}