package ar.edu.usal.logistica.util;

import java.sql.Connection;
import java.sql.SQLException;

public final class JdbcUtil {

    /** Código de MySQL: valor duplicado en una clave única. */
    public static final int ERR_DUPLICADO = 1062;
    /** Códigos de MySQL: violación de clave foránea (con filas hijas / padre inexistente). */
    public static final int ERR_FK_PADRE = 1451;
    public static final int ERR_FK_HIJO = 1452;

    private JdbcUtil() {
    }

    public static boolean esDuplicado(SQLException e) {
        return e.getErrorCode() == ERR_DUPLICADO;
    }

    public static boolean esClaveForanea(SQLException e) {
        return e.getErrorCode() == ERR_FK_PADRE || e.getErrorCode() == ERR_FK_HIJO;
    }

    /** Deshace la transacción sin ocultar la excepción original si el rollback también falla. */
    public static void rollbackSilencioso(Connection con, Throwable causa) {
        if (con == null) {
            return;
        }
        try {
            con.rollback();
        } catch (SQLException ex) {
            causa.addSuppressed(ex);
        }
    }
}