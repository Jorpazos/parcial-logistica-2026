package ar.edu.usal.logistica.web.servlet;

import ar.edu.usal.logistica.dao.factory.DAOFactory;
import ar.edu.usal.logistica.exception.DAOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/ping")
public class PingServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/plain;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        try {
            int cantidad = DAOFactory.getInstance().getChoferDAO().listarTodos().size();
            out.println("OK, choferes en la base: " + cantidad);
        } catch (DAOException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Error de datos: " + e.getMessage());
        }
    }
}