package ar.edu.usal.logistica.util;

import ar.edu.usal.logistica.exception.DAOException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class ConnectionManager {

    private static final String ARCHIVO = "db.properties";

    // La única instancia de la clase (se crea la primera vez que se la pide)
    private static ConnectionManager instancia;

    private final Properties propiedades = new Properties();

    // Constructor privado: nadie de afuera puede hacer "new ConnectionManager()"
    private ConnectionManager() throws DAOException {
        try (InputStream in = ConnectionManager.class.getClassLoader().getResourceAsStream(ARCHIVO)) {
            if (in == null) {
                throw new DAOException("No se encontró el archivo " + ARCHIVO + " en el classpath.");
            }
            propiedades.load(in);
            Class.forName(propiedades.getProperty("db.driver"));
        } catch (IOException e) {
            throw new DAOException("No se pudo leer " + ARCHIVO, e);
        } catch (ClassNotFoundException e) {
            throw new DAOException("No se encontró el driver JDBC de MySQL.", e);
        }
    }

    /* Punto de acceso global. synchronized evita que dos hilos creen dos instancias. */
    public static synchronized ConnectionManager getInstance() throws DAOException {
        if (instancia == null) {
            instancia = new ConnectionManager();
        }
        return instancia;
    }

    /* Devuelve una conexión nueva. Quien la pide es responsable de cerrarla */
    public Connection getConnection() throws DAOException {
        try {
            return DriverManager.getConnection(
                    propiedades.getProperty("db.url"),
                    propiedades.getProperty("db.user"),
                    propiedades.getProperty("db.password"));
        } catch (SQLException e) {
            throw new DAOException("No se pudo conectar a la base de datos.", e);
        }
    }

    /* Lee cualquier valor del archivo  */
    public String getProperty(String clave) {
        return propiedades.getProperty(clave);
    }
}