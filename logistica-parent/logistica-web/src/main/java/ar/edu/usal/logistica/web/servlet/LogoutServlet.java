package ar.edu.usal.logistica.web.servlet;

import ar.edu.usal.logistica.dao.factory.DAOFactory;
import ar.edu.usal.logistica.exception.DAOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

// Se declara en web.xml
public class LogoutServlet extends HttpServlet {

    // POST: cierra la sesion porque modifica datos
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Si hay cookie "recordarme", se borra el token de la base
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("recordarme")) {
                    try {
                        DAOFactory.getInstance().getUsuarioDAO().eliminarToken(cookie.getValue());
                    } catch (DAOException e) {
                        throw new ServletException(e);
                    }
                }
            }
        }

        // Se limpia la Session
        req.getSession().invalidate();

        // Se limpia la cookie: mismo nombre, vida 0 segundos
        Cookie borrar = new Cookie("recordarme", "");
        borrar.setMaxAge(0);
        resp.addCookie(borrar);

        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
