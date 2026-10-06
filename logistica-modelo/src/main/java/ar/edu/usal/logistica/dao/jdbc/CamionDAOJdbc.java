package ar.edu.usal.logistica.dao.jdbc;

import ar.edu.usal.logistica.dao.CamionDAO;
import ar.edu.usal.logistica.domain.Camion;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.exception.ValidacionException;
import ar.edu.usal.logistica.util.ConnectionManager;
import ar.edu.usal.logistica.util.JdbcUtil;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Implementación JDBC de CamionDAO. */
public class CamionDAOJdbc implements CamionDAO {

    private static final String INSERT =
            "INSERT INTO camion (marca, modelo, dominio, toneladas_max, capacidad_tanque_litros, consumo_litros_km) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE camion SET marca = ?, modelo = ?, dominio = ?, toneladas_max = ?, "
                    + "capacidad_tanque_litros = ?, consumo_litros_km = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM camion WHERE id = ?";
    private static final String SELECT_POR_ID =
            "SELECT id, marca, modelo, dominio, toneladas_max, capacidad_tanque_litros, consumo_litros_km "
                    + "FROM camion WHERE id = ?";
    // Consulta fija, sin parámetros: es el caso de uso de Statement
    private static final String SELECT_TODOS =
            "SELECT id, marca, modelo, dominio, toneladas_max, capacidad_tanque_litros, consumo_litros_km "
                    + "FROM camion ORDER BY marca, modelo, dominio";

    @Override
    public void insertar(Camion camion) throws DAOException, ValidacionException {
        camion.validar();   // primero las reglas del dominio
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, camion.getMarca());
            ps.setString(2, camion.getModelo());
            ps.setString(3, camion.getDominio());
            ps.setDouble(4, camion.getToneladasMaximas());
            ps.setDouble(5, camion.getCapacidadTanqueLitros());
            ps.setDouble(6, camion.getConsumoLitrosPorKm());
            ps.executeUpdate();
            // Recupera el id que MySQL le asignó (AUTO_INCREMENT) y lo guarda en el objeto
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    camion.setId(claves.getLong(1));
                }
            }
        } catch (SQLException e) {
            throw traducir(e, camion, "No se pudo guardar el camión.");
        }
    }

    @Override
    public void actualizar(Camion camion) throws DAOException, ValidacionException {
        camion.validar();
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {
            ps.setString(1, camion.getMarca());
            ps.setString(2, camion.getModelo());
            ps.setString(3, camion.getDominio());
            ps.setDouble(4, camion.getToneladasMaximas());
            ps.setDouble(5, camion.getCapacidadTanqueLitros());
            ps.setDouble(6, camion.getConsumoLitrosPorKm());
            ps.setLong(7, camion.getId());
            if (ps.executeUpdate() == 0) {   // executeUpdate devuelve cuántas filas cambió
                throw new ValidacionException("El camión que intenta modificar no existe.");
            }
        } catch (SQLException e) {
            throw traducir(e, camion, "No se pudo modificar el camión.");
        }
    }

    @Override
    public void eliminar(long id) throws DAOException, ValidacionException {
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setLong(1, id);
            if (ps.executeUpdate() == 0) {
                throw new ValidacionException("El camión que intenta eliminar no existe.");
            }
        } catch (SQLException e) {
            if (JdbcUtil.esClaveForanea(e)) {
                throw new ValidacionException("No se puede eliminar el camión porque tiene viajes registrados.");
            }
            throw new DAOException("No se pudo eliminar el camión.", e);
        }
    }

    @Override
    public Camion buscarPorId(long id) throws DAOException {
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_POR_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Mapeadores.camion(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo consultar el camión.", e);
        }
    }

    @Override
    public List<Camion> listarTodos() throws DAOException {
        List<Camion> camiones = new ArrayList<>();
        // Statement: SQL fijo, sin datos del usuario
        try (Connection con = ConnectionManager.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_TODOS)) {
            while (rs.next()) {
                camiones.add(Mapeadores.camion(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo listar los camiones.", e);
        }
        return camiones;
    }

    @Override
    public List<Camion> listarDisponiblesParaChofer(long choferId) throws DAOException {
        List<Camion> camiones = new ArrayList<>();
        // CallableStatement: la regla de disponibilidad vive en el stored procedure
        try (Connection con = ConnectionManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_camiones_disponibles(?)}")) {
            cs.setLong(1, choferId);
            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    camiones.add(Mapeadores.camion(rs));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudieron consultar los camiones disponibles.", e);
        }
        return camiones;
    }

    /** Traduce el error técnico a un mensaje de negocio cuando se puede (dominio duplicado). */
    private DAOException traducir(SQLException e, Camion camion, String mensajeGenerico) throws ValidacionException {
        if (JdbcUtil.esDuplicado(e)) {
            throw new ValidacionException("Ya existe un camión con el dominio " + camion.getDominio() + ".");
        }
        return new DAOException(mensajeGenerico, e);
    }
}