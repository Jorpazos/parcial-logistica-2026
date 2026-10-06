package ar.edu.usal.logistica.web.servlet;

import ar.edu.usal.logistica.dao.factory.DAOFactory;
import ar.edu.usal.logistica.domain.Usuario;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.util.PasswordUtil;
import ar.edu.usal.logistica.web.util.CookieUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String VISTA_LOGIN = "/WEB-INF/jsp/login.jsp";
    private static final int DIAS_RECORDAR = 7;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher(VISTA_LOGIN).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String username = req.getParameter("username");
        String clave = req.getParameter("clave");
        boolean recordar = req.getParameter("recordarme") != null;

        try {
            DAOFactory fabrica = DAOFactory.getInstance();
            Usuario usuario = fabrica.getUsuarioDAO().buscarPorUsername(username);

            if (usuario == null || clave == null
                    || !PasswordUtil.verificar(clave, usuario.getClave())) {
                req.setAttribute("error", "Usuario o contraseña incorrectos");
                req.getRequestDispatcher(VISTA_LOGIN).forward(req, resp);
                return;
            }

            HttpSession vieja = req.getSession(false);
            if (vieja != null) {
                vieja.invalidate();
            }
            req.getSession(true).setAttribute("usuario", usuario);

            if (recordar) {
                String token = PasswordUtil.generarToken();
                LocalDateTime vence = LocalDateTime.now().plusDays(DIAS_RECORDAR);
                fabrica.getUsuarioDAO().guardarToken(token, usuario.getId(), vence);
                CookieUtil.crear(req, resp, CookieUtil.RECORDARME, token,
                        DIAS_RECORDAR * 24 * 60 * 60);
            }

            resp.sendRedirect(req.getContextPath() + "/inicio");

        } catch (DAOException e) {
            req.setAttribute("error", "No se pudo acceder a los datos: " + e.getMessage());
            req.getRequestDispatcher(VISTA_LOGIN).forward(req, resp);
        }
    }
}