package ar.edu.usal.logistica.web.filter;

import ar.edu.usal.logistica.dao.factory.DAOFactory;
import ar.edu.usal.logistica.domain.Usuario;
import ar.edu.usal.logistica.exception.DAOException;
import ar.edu.usal.logistica.web.util.CookieUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        String ruta = req.getRequestURI().substring(req.getContextPath().length());

        // La unica pagina publica es el login
        if (ruta.equals("/login")) {
            chain.doFilter(request, response);
            return;
        }

        Usuario usuario = null;

        // 1) ya hay sesion con usuario
        HttpSession sesion = req.getSession(false);
        if (sesion != null) {
            usuario = (Usuario) sesion.getAttribute("usuario");
        }

        // 2) no hay sesion, pero puede haber cookie "recordarme"
        if (usuario == null) {
            String token = CookieUtil.leer(req, CookieUtil.RECORDARME);
            if (token != null) {
                try {
                    usuario = DAOFactory.getInstance().getUsuarioDAO().buscarPorToken(token);
                } catch (DAOException e) {
                    throw new ServletException(e);
                }
                if (usuario != null) {
                    req.getSession(true).setAttribute("usuario", usuario);
                }
            }
        }

        // 3) nada de lo anterior: al login
        if (usuario == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // 4) las rutas /admin/... son solo para administradores
        if (ruta.startsWith("/admin") && !usuario.esAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Solo para administradores");
            return;
        }

        chain.doFilter(request, response);
    }
}