package ar.edu.usal.logistica.dao.factory;

import ar.edu.usal.logistica.dao.CamionDAO;
import ar.edu.usal.logistica.dao.ChoferDAO;
import ar.edu.usal.logistica.dao.DistanciaDAO;
import ar.edu.usal.logistica.dao.UsuarioDAO;
import ar.edu.usal.logistica.dao.ViajeDAO;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.util.ConnectionManager;

public abstract class DAOFactory {

    public abstract ChoferDAO getChoferDAO();

    public abstract CamionDAO getCamionDAO();

    public abstract UsuarioDAO getUsuarioDAO();

    public abstract ViajeDAO getViajeDAO();

    public abstract DistanciaDAO getDistanciaDAO();

    public static DAOFactory getInstance() throws DAOException {
        String origen = ConnectionManager.getInstance().getProperty("dao.origen");
        if ("JDBC".equalsIgnoreCase(origen)) {
            return JdbcDAOFactory.getInstance();
        }
        throw new DAOException("Origen de datos no soportado: " + origen);
    }

}