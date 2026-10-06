<%
    // La aplicacion arranca en la raiz: se manda a /inicio (si no hay sesion, de ahi va al login)
    response.sendRedirect(request.getContextPath() + "/inicio");
%>
