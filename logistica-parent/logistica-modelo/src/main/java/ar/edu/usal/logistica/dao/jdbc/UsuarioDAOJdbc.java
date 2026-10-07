package ar.edu.usal.logistica.dao.jdbc;

import ar.edu.usal.logistica.dao.UsuarioDAO;
import ar.edu.usal.logistica.domain.Rol;
import ar.edu.usal.logistica.domain.Usuario;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.util.ConnectionManager;
import ar.edu.usal.logistica.util.JdbcUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class UsuarioDAOJdbc implements UsuarioDAO {

    private static final String SELECT_POR_USERNAME =
            "SELECT id, username, clave, rol, chofer_id FROM usuario WHERE username = ?";
    private static final String SELECT_POR_TOKEN =
            "SELECT u.id, u.username, u.clave, u.rol, u.chofer_id FROM usuario u "
                    + "JOIN sesion_recordada s ON s.usuario_id = u.id WHERE s.token = ? AND s.expira > ?";

    @Override
    public Usuario buscarPorUsername(String username) throws DAOException {
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_POR_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo consultar el usuario.", e);
        }
    }

    @Override
    public void guardarToken(String token, long usuarioId, LocalDateTime expira) throws DAOException {
        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            // TRANSACCIÓN: limpieza de tokens vencidos + alta del nuevo, todo o nada
            con.setAutoCommit(false);
            try (PreparedStatement limpiar = con.prepareStatement("DELETE FROM sesion_recordada WHERE expira < ?");
                 PreparedStatement alta = con.prepareStatement(
                         "INSERT INTO sesion_recordada (token, usuario_id, expira) VALUES (?, ?, ?)")) {
                limpiar.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                limpiar.executeUpdate();
                alta.setString(1, token);
                alta.setLong(2, usuarioId);
                alta.setTimestamp(3, Timestamp.valueOf(expira));
                alta.executeUpdate();
                con.commit();                          // todo salió bien: se confirma
            } catch (SQLException e) {
                JdbcUtil.rollbackSilencioso(con, e);   // algo falló: se deshace
                throw e;
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo recordar la sesión.", e);
        }
    }

    @Override
    public Usuario buscarPorToken(String token) throws DAOException {
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_POR_TOKEN)) {
            ps.setString(1, token);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo validar la sesión recordada.", e);
        }
    }

    @Override
    public void eliminarToken(String token) throws DAOException {
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM sesion_recordada WHERE token = ?")) {
            ps.setString(1, token);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("No se pudo cerrar la sesión recordada.", e);
        }
    }

    /* Convierte la fila en un Usuario. chofer_id puede ser NULL (el ADMIN no tiene chofer) */
    private Usuario mapear(ResultSet rs) throws SQLException {
        long choferId = rs.getLong("chofer_id");
        Long choferIdONulo = (choferId == 0) ? null : choferId;   // si es NULL en la base, getLong devuelve 0
        return new Usuario(rs.getLong("id"), rs.getString("username"), rs.getString("clave"),
                Rol.valueOf(rs.getString("rol")), choferIdONulo);
    }
}