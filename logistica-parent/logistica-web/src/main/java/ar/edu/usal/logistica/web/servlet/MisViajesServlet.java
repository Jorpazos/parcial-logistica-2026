package ar.edu.usal.logistica.web.servlet;

import ar.edu.usal.logistica.dao.ViajeDAO;
import ar.edu.usal.logistica.dao.factory.DAOFactory;
import ar.edu.usal.logistica.domain.Usuario;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.exception.ValidacionException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/* Pantalla del chofer*/
@WebServlet("/chofer/viajes")
public class MisViajesServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/jsp/misViajes.jsp";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Usuario usuario = chofer(req, resp);
        if (usuario == null) {
            return;
        }
        try {
            req.setAttribute("viajes", DAOFactory.getInstance().getViajeDAO().listarPorChofer(usuario.getChoferId()));
            req.getRequestDispatcher(VISTA).forward(req, resp);
        } catch (DAOException e) {
            throw new ServletException(e);
        }
    }

    // PUT /chofer/viajes?id=5&accion=iniciar   (o accion=finalizar)
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        Usuario usuario = chofer(req, resp);
        if (usuario == null) {
            return;
        }
        resp.setContentType("text/plain;charset=UTF-8");
        try {
            ViajeDAO dao = DAOFactory.getInstance().getViajeDAO();
            long id = Long.parseLong(req.getParameter("id"));
            if ("iniciar".equals(req.getParameter("accion"))) {
                dao.iniciar(id, usuario.getChoferId());
                resp.getWriter().write("Viaje iniciado");
            } else if ("finalizar".equals(req.getParameter("accion"))) {
                dao.finalizar(id, usuario.getChoferId());
                resp.getWriter().write("Viaje finalizado");
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Accion invalida");
            }
        } catch (ValidacionException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
            resp.getWriter().write(e.getMessage());
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id invalido");
        } catch (DAOException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Error de datos: " + e.getMessage());
        }
    }

    // Devuelve el usuario si es un chofer logueado
    private Usuario chofer(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Usuario usuario = (Usuario) req.getSession().getAttribute("usuario");
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        if (usuario.esAdmin() || usuario.getChoferId() == null) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo para choferes");
            return null;
        }
        return usuario;
    }
}
