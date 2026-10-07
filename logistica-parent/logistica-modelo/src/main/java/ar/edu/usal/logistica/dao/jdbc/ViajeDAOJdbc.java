package ar.edu.usal.logistica.dao.jdbc;

import ar.edu.usal.logistica.dao.ViajeDAO;
import ar.edu.usal.logistica.domain.Destino;
import ar.edu.usal.logistica.domain.EstadoViaje;
import ar.edu.usal.logistica.domain.EstimacionViaje;
import ar.edu.usal.logistica.domain.Viaje;
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
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ViajeDAOJdbc implements ViajeDAO {

    // Un viaje junto con su chofer y su camión en una sola consulta (JOIN). Los alias evitan columnas repetidas.
    private static final String SELECT_BASE =
            "SELECT v.id, v.origen, v.destino, v.distancia_km, v.dias_estimados, v.litros_estimados, v.tanques, "
                    + "v.estado, v.fecha_carga, v.fecha_inicio, v.fecha_fin, "
                    + "ch.id AS ch_id, ch.nombre AS ch_nombre, ch.apellido AS ch_apellido, ch.dni AS ch_dni, "
                    + "ch.fecha_nacimiento AS ch_fecha_nacimiento, ch.categoria AS ch_categoria, "
                    + "ch.telefono AS ch_telefono, "
                    + "c.id AS c_id, c.marca AS c_marca, c.modelo AS c_modelo, c.dominio AS c_dominio, "
                    + "c.toneladas_max AS c_toneladas_max, c.capacidad_tanque_litros AS c_capacidad_tanque_litros, "
                    + "c.consumo_litros_km AS c_consumo_litros_km "
                    + "FROM viaje v JOIN chofer ch ON ch.id = v.chofer_id JOIN camion c ON c.id = v.camion_id ";
    private static final String ORDEN = " ORDER BY v.fecha_carga DESC, v.id DESC";
    private static final String INSERT =
            "INSERT INTO viaje (chofer_id, camion_id, origen, destino, distancia_km, dias_estimados, "
                    + "litros_estimados, tanques, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'ASIGNADO')";

    @Override
    public void insertar(Viaje viaje) throws DAOException, ValidacionException {
        viaje.validar();
        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
                // Se verifica que el camión siga libre (sin viaje ASIGNADO ni EN_CURSO)
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) FROM viaje WHERE camion_id = ? AND estado IN ('ASIGNADO', 'EN_CURSO')")) {
                    ps.setLong(1, viaje.getCamion().getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            throw new ValidacionException("El camión ya está asignado a otro viaje.");
                        }
                    }
                }
                // Recién ahí se inserta el viaje
                try (PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
                    EstimacionViaje est = viaje.getEstimacion();
                    ps.setLong(1, viaje.getChofer().getId());
                    ps.setLong(2, viaje.getCamion().getId());
                    ps.setString(3, viaje.getOrigen().name());
                    ps.setString(4, viaje.getDestino().name());
                    ps.setInt(5, est.getDistanciaKm());
                    ps.setInt(6, est.getDias());
                    ps.setDouble(7, est.getLitrosTotales());
                    ps.setInt(8, est.getTanques());
                    ps.executeUpdate();
                    try (ResultSet claves = ps.getGeneratedKeys()) {
                        claves.next();
                        viaje.setId(claves.getLong(1));
                    }
                }
                con.commit();
            } catch (SQLException | ValidacionException e) {
                JdbcUtil.rollbackSilencioso(con, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo guardar el viaje.", e);
        }
    }

    @Override
    public List<Viaje> listarTodos() throws DAOException {
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + ORDEN);
             ResultSet rs = ps.executeQuery()) {
            return leer(rs);
        } catch (SQLException e) {
            throw new DAOException("No se pudo listar los viajes.", e);
        }
    }

    @Override
    public List<Viaje> listarPorChofer(long choferId) throws DAOException {
        try (Connection con = ConnectionManager.getInstance().getConnection();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + "WHERE v.chofer_id = ?" + ORDEN)) {
            ps.setLong(1, choferId);
            try (ResultSet rs = ps.executeQuery()) {
                return leer(rs);
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo listar los viajes del chofer.", e);
        }
    }

    @Override
    public void iniciar(long viajeId, long choferId) throws DAOException, ValidacionException {
        try (Connection con = ConnectionManager.getInstance().getConnection()) {
            con.setAutoCommit(false);
            try {
                // Un chofer no puede tener dos viajes en curso a la vez
                try (PreparedStatement ps = con.prepareStatement(
                        "SELECT COUNT(*) FROM viaje WHERE chofer_id = ? AND estado = 'EN_CURSO'")) {
                    ps.setLong(1, choferId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        if (rs.getInt(1) > 0) {
                            throw new ValidacionException("Ya tenés un viaje en curso. Finalizalo antes de iniciar otro.");
                        }
                    }
                }
                // Solo se inicia un viaje propio y todavía ASIGNADO
                try (PreparedStatement ps = con.prepareStatement(
                        "UPDATE viaje SET estado = 'EN_CURSO', fecha_inicio = NOW() "
                                + "WHERE id = ? AND chofer_id = ? AND estado = 'ASIGNADO'")) {
                    ps.setLong(1, viajeId);
                    ps.setLong(2, choferId);
                    if (ps.executeUpdate() == 0) {
                        throw new ValidacionException("El viaje no existe, no es tuyo o ya fue iniciado.");
                    }
                }
                con.commit();
            } catch (SQLException | ValidacionException e) {
                JdbcUtil.rollbackSilencioso(con, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo iniciar el viaje.", e);
        }
    }

    @Override
    public void finalizar(long viajeId, long choferId) throws DAOException, ValidacionException {
        // CallableStatement: stored procedure con 2 parámetros IN (viaje, chofer) y 1 OUT (filas modificadas)
        try (Connection con = ConnectionManager.getInstance().getConnection();
             CallableStatement cs = con.prepareCall("{call sp_finalizar_viaje(?, ?, ?)}")) {
            cs.setLong(1, viajeId);
            cs.setLong(2, choferId);
            cs.registerOutParameter(3, Types.INTEGER);   // se declara que el 3er parámetro es de salida
            cs.execute();
            if (cs.getInt(3) == 0) {                     // se lee el valor que devolvió el procedure
                throw new ValidacionException("El viaje no existe, no es tuyo o todavía no fue iniciado.");
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo finalizar el viaje.", e);
        }
    }

    private List<Viaje> leer(ResultSet rs) throws SQLException {
        List<Viaje> viajes = new ArrayList<>();
        while (rs.next()) {
            EstimacionViaje estimacion = new EstimacionViaje(rs.getInt("distancia_km"), rs.getInt("dias_estimados"),
                    rs.getDouble("litros_estimados"), rs.getInt("tanques"));
            viajes.add(new Viaje(
                    rs.getLong("id"),
                    Mapeadores.chofer(rs, "ch_"),
                    Mapeadores.camion(rs, "c_"),
                    Destino.valueOf(rs.getString("origen")),
                    Destino.valueOf(rs.getString("destino")),
                    estimacion,
                    EstadoViaje.valueOf(rs.getString("estado")),
                    aFecha(rs.getTimestamp("fecha_carga")),
                    aFecha(rs.getTimestamp("fecha_inicio")),
                    aFecha(rs.getTimestamp("fecha_fin"))));
        }
        return viajes;
    }

    private LocalDateTime aFecha(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }
}