package ar.edu.usal.logistica.web.servlet;

import ar.edu.usal.logistica.dao.CamionDAO;
import ar.edu.usal.logistica.dao.ChoferDAO;
import ar.edu.usal.logistica.dao.factory.DAOFactory;
import ar.edu.usal.logistica.domain.Camion;
import ar.edu.usal.logistica.domain.Categoria;
import ar.edu.usal.logistica.domain.Chofer;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.exception.ValidacionException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.Set;

@WebServlet("/admin/choferes")
public class ChoferServlet extends HttpServlet {

    private static final String VISTA = "/WEB-INF/jsp/choferes.jsp";

    // GET: lista de choferes, y si piden editar carga uno en el formulario
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        try {
            DAOFactory fabrica = DAOFactory.getInstance();
            ChoferDAO choferDAO = fabrica.getChoferDAO();

            Set<Long> idsAutorizados = new HashSet<>();
            String id = req.getParameter("id");
            if ("editar".equals(req.getParameter("accion")) && id != null) {
                Chofer chofer = choferDAO.buscarPorId(Long.parseLong(id));
                req.setAttribute("choferEditar", chofer);
                if (chofer != null) {
                    for (Camion c : chofer.getCamionesAutorizados()) {
                        idsAutorizados.add(c.getId());
                    }
                }
            }
            req.setAttribute("idsAutorizados", idsAutorizados);
            req.setAttribute("choferes", choferDAO.listarTodos());
            req.setAttribute("camiones", fabrica.getCamionDAO().listarTodos());
            req.setAttribute("categorias", Categoria.values());
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

        try {
            DAOFactory fabrica = DAOFactory.getInstance();
            ChoferDAO choferDAO = fabrica.getChoferDAO();

            if ("eliminar".equals(req.getParameter("accion"))) {
                choferDAO.eliminar(Long.parseLong(req.getParameter("id")));
                avisar(sesion, "success", "Chofer eliminado");
            } else {
                String idTexto = req.getParameter("id");
                Long id = (idTexto == null || idTexto.isEmpty()) ? null : Long.valueOf(idTexto);

                Chofer chofer = new Chofer(id,
                        req.getParameter("nombre"),
                        req.getParameter("apellido"),
                        req.getParameter("dni"),
                        LocalDate.parse(req.getParameter("fechaNacimiento")),
                        Categoria.valueOf(req.getParameter("categoria")),
                        req.getParameter("telefono"));

                // Camiones tildados en el formulario
                String[] camionIds = req.getParameterValues("camionId");
                if (camionIds != null) {
                    CamionDAO camionDAO = fabrica.getCamionDAO();
                    for (String camionId : camionIds) {
                        Camion camion = camionDAO.buscarPorId(Long.parseLong(camionId));
                        if (camion != null) {
                            chofer.autorizar(camion);
                        }
                    }
                }

                if (id == null) {
                    String clave = req.getParameter("clave");
                    if (clave == null || clave.trim().length() < 4) {
                        throw new ValidacionException("La clave inicial debe tener al menos 4 caracteres.");
                    }
                    choferDAO.insertar(chofer, clave.trim());
                    avisar(sesion, "success", "Chofer guardado. Usuario: su DNI.");
                } else {
                    choferDAO.actualizar(chofer);
                    avisar(sesion, "success", "Chofer modificado");
                }
            }
        } catch (ValidacionException e) {
            avisar(sesion, "warning", e.getMessage());
        } catch (IllegalArgumentException | DateTimeParseException e) {
            avisar(sesion, "warning", "Hay un dato invalido en el formulario (fecha, categoria o numero)");
        } catch (DAOException e) {
            avisar(sesion, "error", "Error de datos: " + e.getMessage());
        }

        resp.sendRedirect(req.getContextPath() + "/admin/choferes");
    }

    private void avisar(HttpSession sesion, String tipo, String texto) {
        sesion.setAttribute("avisoTipo", tipo);
        sesion.setAttribute("aviso", texto);
    }
}