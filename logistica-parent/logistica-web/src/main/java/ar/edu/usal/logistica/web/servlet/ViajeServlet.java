package ar.edu.usal.logistica.web.servlet;

import ar.edu.usal.logistica.dao.factory.DAOFactory;
import ar.edu.usal.logistica.domain.Camion;
import ar.edu.usal.logistica.domain.Chofer;
import ar.edu.usal.logistica.domain.Destino;
import ar.edu.usal.logistica.domain.Usuario;
import ar.edu.usal.logistica.domain.Viaje;
import ar.edu.usal.logistica.exception.ChoferNoEncontradoException;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.exception.ValidacionException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/* Pantalla del administrador */
@WebServlet("/admin/viajes")
public class ViajeServlet extends HttpServlet {

    private static final String VISTA_LISTA = "/WEB-INF/jsp/viajes.jsp";
    private static final String VISTA_NUEVO = "/WEB-INF/jsp/viajeNuevo.jsp";

    // GET: lista de viajes, o (si viene el dni) el formulario para armar un viaje
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession sesion = req.getSession();
        if (!esAdmin(req, resp)) {
            return;
        }

        req.setAttribute("aviso", sesion.getAttribute("aviso"));
        req.setAttribute("avisoTipo", sesion.getAttribute("avisoTipo"));
        sesion.removeAttribute("aviso");
        sesion.removeAttribute("avisoTipo");

        try {
            DAOFactory fabrica = DAOFactory.getInstance();
            String dni = req.getParameter("dni");

            if (dni == null || dni.trim().isEmpty()) {
                req.setAttribute("viajes", fabrica.getViajeDAO().listarTodos());
                req.getRequestDispatcher(VISTA_LISTA).forward(req, resp);
                return;
            }

            try {
                Chofer chofer = fabrica.getChoferDAO().buscarPorDni(dni);
                cargarFormulario(req, fabrica, chofer);
                req.getRequestDispatcher(VISTA_NUEVO).forward(req, resp);
            } catch (ChoferNoEncontradoException e) {
                // El enunciado pide avisar en pantalla si el chofer no existe
                avisar(sesion, "warning", "No existe un chofer con DNI " + dni.trim());
                resp.sendRedirect(req.getContextPath() + "/admin/viajes");
            }
        } catch (DAOException e) {
            throw new ServletException(e);
        }
    }

    // POST: accion "calcular" muestra el resultado, accion "guardar" graba el viaje
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession sesion = req.getSession();
        if (!esAdmin(req, resp)) {
            return;
        }

        try {
            DAOFactory fabrica = DAOFactory.getInstance();
            Chofer chofer = fabrica.getChoferDAO().buscarPorId(Long.parseLong(req.getParameter("choferId")));
            Camion camion = fabrica.getCamionDAO().buscarPorId(Long.parseLong(req.getParameter("camionId")));
            Destino origen = Destino.valueOf(req.getParameter("origen"));
            Destino destino = Destino.valueOf(req.getParameter("destino"));

            int km = fabrica.getDistanciaDAO().obtenerKm(origen, destino);
            Viaje viaje = Viaje.nuevo(chofer, camion, origen, destino, km);
            viaje.validar();

            if ("guardar".equals(req.getParameter("accion"))) {
                fabrica.getViajeDAO().insertar(viaje);
                avisar(sesion, "success", "Viaje guardado");
                resp.sendRedirect(req.getContextPath() + "/admin/viajes");
            } else {
                // Se vuelve a mostrar el formulario con el calculo hecho
                cargarFormulario(req, fabrica, chofer);
                req.setAttribute("viaje", viaje);
                req.setAttribute("camionElegido", camion);
                req.setAttribute("origenElegido", origen);
                req.setAttribute("destinoElegido", destino);
                req.getRequestDispatcher(VISTA_NUEVO).forward(req, resp);
            }
        } catch (ValidacionException e) {
            avisar(sesion, "warning", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/viajes");
        } catch (IllegalArgumentException | NullPointerException e) {
            avisar(sesion, "warning", "Hay datos invalidos en el formulario");
            resp.sendRedirect(req.getContextPath() + "/admin/viajes");
        } catch (DAOException e) {
            avisar(sesion, "error", "Error de datos: " + e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/admin/viajes");
        }
    }

    private void cargarFormulario(HttpServletRequest req, DAOFactory fabrica, Chofer chofer) throws DAOException {
        req.setAttribute("chofer", chofer);
        req.setAttribute("camiones", fabrica.getCamionDAO().listarDisponiblesParaChofer(chofer.getId()));
        req.setAttribute("destinos", Destino.values());
    }

    private boolean esAdmin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Usuario usuario = (Usuario) req.getSession().getAttribute("usuario");
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return false;
        }
        if (!usuario.esAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo para administradores");
            return false;
        }
        return true;
    }

    private void avisar(HttpSession sesion, String tipo, String texto) {
        sesion.setAttribute("avisoTipo", tipo);
        sesion.setAttribute("aviso", texto);
    }
}
