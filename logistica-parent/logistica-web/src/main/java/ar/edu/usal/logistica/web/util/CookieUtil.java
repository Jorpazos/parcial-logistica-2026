package ar.edu.usal.logistica.web.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public final class CookieUtil {

    public static final String RECORDARME = "recordarme";

    private CookieUtil() {
    }

    // Con el contexto en "/" getContextPath() devuelve "" y la cookie necesita "/"
    private static String ruta(HttpServletRequest req) {
        String ctx = req.getContextPath();
        return ctx.isEmpty() ? "/" : ctx;
    }

    public static String leer(HttpServletRequest req, String nombre) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie c : cookies) {
            if (c.getName().equals(nombre)) {
                return c.getValue();
            }
        }
        return null;
    }

    public static void crear(HttpServletRequest req, HttpServletResponse resp,
                             String nombre, String valor, int segundos) {
        Cookie c = new Cookie(nombre, valor);
        c.setMaxAge(segundos);
        c.setPath(ruta(req));
        c.setHttpOnly(true);
        resp.addCookie(c);
    }

    public static void borrar(HttpServletRequest req, HttpServletResponse resp, String nombre) {
        Cookie c = new Cookie(nombre, "");
        c.setMaxAge(0);
        c.setPath(ruta(req));
        c.setHttpOnly(true);
        resp.addCookie(c);
    }
}