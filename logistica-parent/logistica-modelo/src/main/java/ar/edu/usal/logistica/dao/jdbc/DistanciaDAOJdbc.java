package ar.edu.usal.logistica.dao.jdbc;

import ar.edu.usal.logistica.dao.DistanciaDAO;
import ar.edu.usal.logistica.domain.Destino;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.exception.ValidacionException;
import ar.edu.usal.logistica.util.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DistanciaDAOJdbc implements DistanciaDAO {

    private static final String SELECT_KM =
            "SELECT km FROM distancia WHERE origen = ? AND destino = ?";

    @Override
    public int obtenerKm(Destino origen, Destino destino) throws DAOException, ValidacionException {
        if (origen == null || destino == null || origen == destino) {
            throw new ValidacionException("Debe elegir un origen y un destino distintos.");
        }
        // try-with-resources: la conexión y el statement se cierran solos
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_KM)) {
            ps.setString(1, origen.name());    // el 1er ? es el origen
            ps.setString(2, destino.name());   // el 2do ? es el destino
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("km");
                }
            }
        } catch (SQLException e) {
            // Se envuelve la excepción técnica para que la web no dependa de JDBC
            throw new DAOException("No se pudo consultar la distancia.", e);
        }
        throw new ValidacionException("No hay distancia cargada entre " + origen.getNombre()
                + " y " + destino.getNombre() + ".");
    }
}