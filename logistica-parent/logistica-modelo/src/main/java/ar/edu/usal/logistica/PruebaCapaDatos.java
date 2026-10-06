package ar.edu.usal.logistica;

import ar.edu.usal.logistica.dao.factory.DAOFactory;
import ar.edu.usal.logistica.domain.Camion;
import ar.edu.usal.logistica.domain.Chofer;
import ar.edu.usal.logistica.domain.Destino;
import ar.edu.usal.logistica.domain.Usuario;
import ar.edu.usal.logistica.util.PasswordUtil;

/** Prueba manual (no es parte de la entrega): comprueba que la capa de datos habla con MySQL. */
public class PruebaCapaDatos {

    public static void main(String[] args) throws Exception {
        DAOFactory fabrica = DAOFactory.getInstance();

        Chofer juan = fabrica.getChoferDAO().buscarPorDni("30111222");
        System.out.println("Chofer: " + juan.getNombreCompleto() + ", camiones autorizados: "
                + juan.getCamionesAutorizados().size());

        int km = fabrica.getDistanciaDAO().obtenerKm(Destino.CABA, Destino.CORDOBA);
        System.out.println("CABA -> Córdoba: " + km + " km (debería dar 646)");

        for (Camion camion : fabrica.getCamionDAO().listarDisponiblesParaChofer(juan.getId())) {
            System.out.println("Disponible: " + camion.getDescripcion());
        }

        Usuario admin = fabrica.getUsuarioDAO().buscarPorUsername("admin");
        System.out.println("Login admin/admin123 correcto: " + PasswordUtil.verificar("admin123", admin.getClave()));
    }
}