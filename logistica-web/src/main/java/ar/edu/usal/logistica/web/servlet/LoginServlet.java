package ar.edu.usal.logistica.web.servlet;

import ar.edu.usal.logistica.dao.factory.DAOFactory;
import ar.edu.usal.logistica.domain.Usuario;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.util.PasswordUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final String VISTA_LOGIN = "/WEB-INF/jsp/login.jsp";
    private static final int DIAS_RECORDAR = 7;

    // GET: muestra el formulario. Si ya hay sesion, o una cookie "recordarme" valida, entra directo
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        if (req.getSession().getAttribute("usuario") != null) {
            resp.sendRedirect(req.getContextPath() + "/inicio");
            return;
        }

        // Se busca la cookie "recordarme" (getCookies() puede ser null)
        String token = null;
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("recordarme")) {
                    token = cookie.getValue();
                }
            }
        }

        if (token != null) {
            try {
                Usuario usuario = DAOFactory.getInstance().getUsuarioDAO().buscarPorToken(token);
                if (usuario != null) {
                    req.getSession().setAttribute("usuario", usuario);
                    resp.sendRedirect(req.getContextPath() + "/inicio");
                    return;
                }
            } catch (DAOException e) {
                throw new ServletException(e);
            }
        }

        req.getRequestDispatcher(VISTA_LOGIN).forward(req, resp);
    }

    // POST: valida usuario y clave
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

            req.getSession().setAttribute("usuario", usuario);

            if (recordar) {
                // La cookie guarda un token al azar (nunca la clave); la base sabe de quien es
                String token = PasswordUtil.generarToken();
                LocalDateTime vence = LocalDateTime.now().plusDays(DIAS_RECORDAR);
                fabrica.getUsuarioDAO().guardarToken(token, usuario.getId(), vence);

                Cookie cookie = new Cookie("recordarme", token);
                cookie.setMaxAge(DIAS_RECORDAR * 24 * 60 * 60);   // en segundos
                cookie.setHttpOnly(true);
                resp.addCookie(cookie);
            }

            resp.sendRedirect(req.getContextPath() + "/inicio");

        } catch (DAOException e) {
            req.setAttribute("error", "No se pudo acceder a los datos: " + e.getMessage());
            req.getRequestDispatcher(VISTA_LOGIN).forward(req, resp);
        }
    }
}
