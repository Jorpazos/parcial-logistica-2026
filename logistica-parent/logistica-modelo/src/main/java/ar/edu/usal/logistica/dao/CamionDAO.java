package ar.edu.usal.logistica.dao;

import ar.edu.usal.logistica.domain.Camion;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.exception.ValidacionException;

import java.util.List;

public interface CamionDAO {

    void insertar(Camion camion) throws DAOException, ValidacionException;

    void actualizar(Camion camion) throws DAOException, ValidacionException;

    void eliminar(long id) throws DAOException, ValidacionException;

    Camion buscarPorId(long id) throws DAOException;

    List<Camion> listarTodos() throws DAOException;

    /* Camiones que el chofer puede manejar y que no están en viaje */
    List<Camion> listarDisponiblesParaChofer(long choferId) throws DAOException;
}