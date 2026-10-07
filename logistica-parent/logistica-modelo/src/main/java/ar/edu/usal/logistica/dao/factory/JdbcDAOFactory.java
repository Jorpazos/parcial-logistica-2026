package ar.edu.usal.logistica.dao.factory;

import ar.edu.usal.logistica.dao.CamionDAO;
import ar.edu.usal.logistica.dao.ChoferDAO;
import ar.edu.usal.logistica.dao.DistanciaDAO;
import ar.edu.usal.logistica.dao.UsuarioDAO;
import ar.edu.usal.logistica.dao.ViajeDAO;
import ar.edu.usal.logistica.dao.jdbc.CamionDAOJdbc;
import ar.edu.usal.logistica.dao.jdbc.ChoferDAOJdbc;
import ar.edu.usal.logistica.dao.jdbc.DistanciaDAOJdbc;
import ar.edu.usal.logistica.dao.jdbc.UsuarioDAOJdbc;
import ar.edu.usal.logistica.dao.jdbc.ViajeDAOJdbc;

public final class JdbcDAOFactory extends DAOFactory {

    private static final JdbcDAOFactory INSTANCIA = new JdbcDAOFactory();

    private JdbcDAOFactory() {
    }

    public static JdbcDAOFactory getInstance() {
        return INSTANCIA;
    }

    @Override
    public ChoferDAO getChoferDAO() {
        return new ChoferDAOJdbc();
    }

    @Override
    public CamionDAO getCamionDAO() {
        return new CamionDAOJdbc();
    }

    @Override
    public UsuarioDAO getUsuarioDAO() {
        return new UsuarioDAOJdbc();
    }

    @Override
    public ViajeDAO getViajeDAO() {
        return new ViajeDAOJdbc();
    }

    @Override
    public DistanciaDAO getDistanciaDAO() {
        return new DistanciaDAOJdbc();
    }
}