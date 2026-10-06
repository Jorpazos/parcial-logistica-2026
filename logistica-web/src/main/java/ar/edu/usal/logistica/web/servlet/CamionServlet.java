package ar.edu.usal.logistica.web.servlet;

import ar.edu.usal.logistica.dao.CamionDAO;
import ar.edu.usal.logistica.dao.factory.DAOFactory;
import ar.edu.usal.logistica.domain.Camion;
import ar.edu.usal.logistica.domain.Usuario;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.exception.ValidacionException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/admin/camiones")
public class CamionServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/jsp/camiones.jsp";

    // GET: muestra la lista y, si piden editar, carga el camion en el formulario
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        HttpSession sesion = req.getSession();
        Usuario usuario = (Usuario) sesion.getAttribute("usuario");
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!usuario.esAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo para administradores");
            return;
        }

        // Si el POST anterior dejo un aviso en la Session, se pasa al request y se borra (se muestra una sola vez)
        req.setAttribute("aviso", sesion.getAttribute("aviso"));
        req.setAttribute("avisoTipo", sesion.getAttribute("avisoTipo"));
        sesion.removeAttribute("aviso");
        sesion.removeAttribute("avisoTipo");

        try {
            CamionDAO dao = DAOFactory.getInstance().getCamionDAO();

            String id = req.getParameter("id");
            if ("editar".equals(req.getParameter("accion")) && id != null) {
                req.setAttribute("camionEditar", dao.buscarPorId(Long.parseLong(id)));
            }
            req.setAttribute("camiones", dao.listarTodos());
            req.getRequestDispatcher(VISTA).forward(req, resp);

        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Id invalido");
        } catch (DAOException e) {
            throw new ServletException(e);
        }
    }

    // POST: guardar (alta o modificacion) o eliminar
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        HttpSession sesion = req.getSession();
        Usuario usuario = (Usuario) sesion.getAttribute("usuario");
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        if (!usuario.esAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo para administradores");
            return;
        }

        try {
            CamionDAO dao = DAOFactory.getInstance().getCamionDAO();

            if ("eliminar".equals(req.getParameter("accion"))) {
                dao.eliminar(Long.parseLong(req.getParameter("id")));
                avisar(sesion, "success", "Camion eliminado");
            } else {
                String idTexto = req.getParameter("id");
                Long id = (idTexto == null || idTexto.isEmpty()) ? null : Long.valueOf(idTexto);

                Camion camion = new Camion(id,
                        req.getParameter("marca"),
                        req.getParameter("modelo"),
                        req.getParameter("dominio"),
                        numero(req, "toneladas"),
                        numero(req, "tanque"),
                        numero(req, "consumo"));

                if (id == null) {
                    dao.insertar(camion);
                    avisar(sesion, "success", "Camion guardado");
                } else {
                    dao.actualizar(camion);
                    avisar(sesion, "success", "Camion modificado");
                }
            }
        } catch (ValidacionException e) {
            avisar(sesion, "warning", e.getMessage());
        } catch (NumberFormatException e) {
            avisar(sesion, "warning", "Hay un numero invalido en el formulario");
        } catch (DAOException e) {
            avisar(sesion, "error", "Error de datos: " + e.getMessage());
        }

        // Post-Redirect-Get: asi F5 no repite el POST
        resp.sendRedirect(req.getContextPath() + "/admin/camiones");
    }

    // Lee un numero decimal aceptando coma o punto
    private double numero(HttpServletRequest req, String nombre) {
        String texto = req.getParameter(nombre);
        if (texto == null) {
            throw new NumberFormatException(nombre);
        }
        return Double.parseDouble(texto.trim().replace(',', '.'));
    }

    // Deja un mensaje en la Session para mostrarlo una sola vez con SweetAlert
    private void avisar(HttpSession sesion, String tipo, String texto) {
        sesion.setAttribute("avisoTipo", tipo);
        sesion.setAttribute("aviso", texto);
    }
}