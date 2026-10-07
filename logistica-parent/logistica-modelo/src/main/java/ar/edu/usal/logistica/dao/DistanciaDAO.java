package ar.edu.usal.logistica.dao;

import ar.edu.usal.logistica.domain.Destino;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.exception.ValidacionException;

public interface DistanciaDAO {

    /*kilómetros entre ambas ciudades */
    int obtenerKm(Destino origen, Destino destino) throws DAOException, ValidacionException;
}