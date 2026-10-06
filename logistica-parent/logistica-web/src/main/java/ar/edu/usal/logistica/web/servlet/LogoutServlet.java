package ar.edu.usal.logistica.web.servlet;

import ar.edu.usal.logistica.dao.factory.DAOFactory;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.web.util.CookieUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

// Se declara en web.xml (no lleva @WebServlet)
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String token = CookieUtil.leer(req, CookieUtil.RECORDARME);
        if (token != null) {
            try {
                DAOFactory.getInstance().getUsuarioDAO().eliminarToken(token);
            } catch (DAOException e) {
                throw new ServletException(e);
            }
        }

        HttpSession sesion = req.getSession(false);
        if (sesion != null) {
            sesion.invalidate();
        }

        CookieUtil.borrar(req, resp, CookieUtil.RECORDARME);
        CookieUtil.borrar(req, resp, "JSESSIONID");

        resp.sendRedirect(req.getContextPath() + "/login");
    }
}