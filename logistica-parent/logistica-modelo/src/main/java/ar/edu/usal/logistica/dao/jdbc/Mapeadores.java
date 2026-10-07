package ar.edu.usal.logistica.dao.jdbc;

import ar.edu.usal.logistica.domain.Camion;
import ar.edu.usal.logistica.domain.Categoria;
import ar.edu.usal.logistica.domain.Chofer;

import java.sql.ResultSet;
import java.sql.SQLException;


final class Mapeadores {

    private Mapeadores() {
    }

    static Camion camion(ResultSet rs) throws SQLException {
        return camion(rs, "");
    }

    static Camion camion(ResultSet rs, String prefijo) throws SQLException {
        return new Camion(
                rs.getLong(prefijo + "id"),
                rs.getString(prefijo + "marca"),
                rs.getString(prefijo + "modelo"),
                rs.getString(prefijo + "dominio"),
                rs.getDouble(prefijo + "toneladas_max"),
                rs.getDouble(prefijo + "capacidad_tanque_litros"),
                rs.getDouble(prefijo + "consumo_litros_km"));
    }

    static Chofer chofer(ResultSet rs) throws SQLException {
        return chofer(rs, "");
    }

    static Chofer chofer(ResultSet rs, String prefijo) throws SQLException {
        return new Chofer(
                rs.getLong(prefijo + "id"),
                rs.getString(prefijo + "nombre"),
                rs.getString(prefijo + "apellido"),
                rs.getString(prefijo + "dni"),
                rs.getDate(prefijo + "fecha_nacimiento").toLocalDate(),
                Categoria.valueOf(rs.getString(prefijo + "categoria")),
                rs.getString(prefijo + "telefono"));
    }
}