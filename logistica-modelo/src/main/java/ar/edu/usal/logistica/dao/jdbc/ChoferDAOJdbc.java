package ar.edu.usal.logistica.dao.jdbc;

import ar.edu.usal.logistica.dao.ChoferDAO;
import ar.edu.usal.logistica.domain.Camion;
import ar.edu.usal.logistica.domain.Chofer;
import ar.edu.usal.logistica.exception.ChoferNoEncontradoException;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.exception.ValidacionException;
import ar.edu.usal.logistica.util.ConnectionManager;
import ar.edu.usal.logistica.util.PasswordUtil;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ChoferDAOJdbc implements ChoferDAO {

    private static final String SELECT_CHOFER =
            "SELECT id, nombre, apellido, dni, fecha_nacimiento, categoria, telefono FROM chofer";
    private static final String SELECT_CAMIONES_DEL_CHOFER =
            "SELECT c.id, c.marca, c.modelo, c.dominio, c.toneladas_max, c.capacidad_tanque_litros, "
                    + "c.consumo_litros_km FROM camion c "
                    + "JOIN chofer_camion cc ON cc.camion_id = c.id WHERE cc.chofer_id = ?";
    private static final String INSERT_CHOFER =
            "INSERT INTO chofer (nombre, apellido, dni, fecha_nacimiento, categoria, telefono) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String UPDATE_CHOFER =
            "UPDATE chofer SET nombre = ?, apellido = ?, dni = ?, fecha_nacimiento = ?, categoria = ?, "
                    + "telefono = ? WHERE id = ?";
    private static final String INSERT_AUTORIZACION =
            "INSERT INTO chofer_camion (chofer_id, camion_id) VALUES (?, ?)";
    private static final String INSERT_USUARIO =
            "INSERT INTO usuario (username, clave, rol, chofer_id) VALUES (?, ?, 'CHOFER', ?)";

    @Override
    public void insertar(Chofer chofer, String claveInicial) throws DAOException, ValidacionException {
        chofer.validar();
        if (claveInicial == null || claveInicial.length() < 6) {
            throw new ValidacionException("La contraseña inicial debe tener al menos 6 caracteres.");
        }
        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            con.setAutoCommit(false);   // INICIO de la transacción: o se guarda todo o nada
            try {
                // 1) El chofer
                try (PreparedStatement ps = con.prepareStatement(INSERT_CHOFER, Statement.RETURN_GENERATED_KEYS)) {
                    cargarDatos(ps, chofer);
                    ps.executeUpdate();
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        claves.next();
                        chofer.setId(claves.getLong(1));
                    }
                }
                // 2) Los camiones que está autorizado a manejar
                guardarAutorizaciones(con, chofer);
                // 3) Su usuario para entrar al sistema (el usuario es su DNI)
                try (PreparedStatement ps = con.prepareStatement(INSERT_USUARIO)) {
                    ps.setString(1, chofer.getDni());
                    ps.setString(2, PasswordUtil.hashear(claveInicial));
                    ps.setLong(3, chofer.getId());
                    ps.executeUpdate();
                }
                con.commit();                          // las 3 salieron bien: se confirma
            } catch (SQLException e) {
                JdbcUtil.rollbackSilencioso(con, e);   // alguna falló: se deshace todo
                chofer.setId(null);
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e, chofer, "No se pudo guardar el chofer.");
        }
    }

    @Override
    public void actualizar(Chofer chofer) throws DAOException, ValidacionException {
        chofer.validar();
        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(UPDATE_CHOFER)) {
                    cargarDatos(ps, chofer);
                    ps.setLong(7, chofer.getId());
                    if (ps.executeUpdate() == 0) {
                        throw new ValidacionException("El chofer que intenta modificar no existe.");
                    }
                }
                // Se reemplazan las autorizaciones: se borran las viejas y se cargan las nuevas
                try (PreparedStatement ps = con.prepareStatement("DELETE FROM chofer_camion WHERE chofer_id = ?")) {
                    ps.setLong(1, chofer.getId());
                    ps.executeUpdate();
                }
                guardarAutorizaciones(con, chofer);
                // El usuario sigue al DNI del chofer si este cambió
                try (PreparedStatement ps = con.prepareStatement("UPDATE usuario SET username = ? WHERE chofer_id = ?")) {
                    ps.setString(1, chofer.getDni());
                    ps.setLong(2, chofer.getId());
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException | ValidacionException e) {
                JdbcUtil.rollbackSilencioso(con, e);
                throw e;
            }
        } catch (SQLException e) {
            throw traducir(e, chofer, "No se pudo modificar el chofer.");
        }
    }

    @Override
    public void eliminar(long id) throws DAOException, ValidacionException {
        // Las FK con ON DELETE CASCADE borran usuario y autorizaciones; si tiene viajes, la base lo impide
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM chofer WHERE id = ?")) {
            ps.setLong(1, id);
            if (ps.executeUpdate() == 0) {
                throw new ValidacionException("El chofer que intenta eliminar no existe.");
            }
        } catch (SQLException e) {
            if (JdbcUtil.esClaveForanea(e)) {
                throw new ValidacionException("No se puede eliminar el chofer porque tiene viajes registrados.");
            }
            throw new DAOException("No se pudo eliminar el chofer.", e);
        }
    }

    @Override
    public Chofer buscarPorId(long id) throws DAOException {
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_CHOFER + " WHERE id = ?")) {
            ps.setLong(1, id);
            Chofer chofer = null;
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    chofer = Mapeadores.chofer(rs);
                }
            }
            if (chofer != null) {
                cargarCamiones(con, chofer);
            }
            return chofer;
        } catch (SQLException e) {
            throw new DAOException("No se pudo consultar el chofer.", e);
        }
    }

    @Override
    public Chofer buscarPorDni(String dni) throws DAOException, ChoferNoEncontradoException {
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_CHOFER + " WHERE dni = ?")) {
            ps.setString(1, dni == null ? "" : dni.trim());
            Chofer chofer;
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new ChoferNoEncontradoException(dni);   // el enunciado pide avisarlo en pantalla
                }
                chofer = Mapeadores.chofer(rs);
            }
            cargarCamiones(con, chofer);
            return chofer;
        } catch (SQLException e) {
            throw new DAOException("No se pudo consultar el chofer.", e);
        }
    }

    @Override
    public List<Chofer> listarTodos() throws DAOException {
        List<Chofer> choferes = new ArrayList<>();
        // Statement: SQL fijo
        try (Connection con = ConnectionManager.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_CHOFER + " ORDER BY apellido, nombre")) {
            while (rs.next()) {
                choferes.add(Mapeadores.chofer(rs));
            }
            for (Chofer chofer : choferes) {
                cargarCamiones(con, chofer);
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo listar los choferes.", e);
        }
        return choferes;
    }

    /** Carga los camiones que el chofer está autorizado a manejar (relación N a N). */
    private void cargarCamiones(Connection con, Chofer chofer) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(SELECT_CAMIONES_DEL_CHOFER)) {
            ps.setLong(1, chofer.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    chofer.autorizar(Mapeadores.camion(rs));
                }
            }
        }
    }

    private void cargarDatos(PreparedStatement ps, Chofer chofer) throws SQLException {
        ps.setString(1, chofer.getNombre());
        ps.setString(2, chofer.getApellido());
        ps.setString(3, chofer.getDni());
        ps.setDate(4, Date.valueOf(chofer.getFechaNacimiento()));
        ps.setString(5, chofer.getCategoria().name());
        ps.setString(6, chofer.getTelefono());
    }

    private void guardarAutorizaciones(Connection con, Chofer chofer) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(INSERT_AUTORIZACION)) {
            for (Camion camion : chofer.getCamionesAutorizados()) {
                ps.setLong(1, chofer.getId());
                ps.setLong(2, camion.getId());
                ps.executeUpdate();
            }
        }
    }

    private DAOException traducir(SQLException e, Chofer chofer, String mensajeGenerico) throws ValidacionException {
        if (JdbcUtil.esDuplicado(e)) {
            throw new ValidacionException("Ya existe un chofer con DNI " + chofer.getDni() + ".");
        }
        if (JdbcUtil.esClaveForanea(e)) {
            throw new ValidacionException("Alguno de los camiones seleccionados ya no existe.");
        }
        return new DAOException(mensajeGenerico, e);
    }
}