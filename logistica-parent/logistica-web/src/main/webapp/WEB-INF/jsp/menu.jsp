<%--
  Created by IntelliJ IDEA.
  User: jorge
  Date: 5/10/2026
  Time: 22:50
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<nav class="navbar navbar-expand navbar-dark bg-dark">
    <div class="container">
        <span class="navbar-brand">Logistica</span>
        <ul class="navbar-nav me-auto">
            <li class="nav-item">
                <a class="nav-link" href="${pageContext.request.contextPath}/inicio">Inicio</a>
            </li>
            <c:if test="${sessionScope.usuario.rol == 'ADMIN'}">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/admin/camiones">Camiones</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/admin/choferes">Choferes</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/admin/viajes">Viajes</a>
                </li>
            </c:if>
            <c:if test="${sessionScope.usuario.rol == 'CHOFER'}">
                <li class="nav-item">
                    <a class="nav-link" href="${pageContext.request.contextPath}/chofer/viajes">Mis viajes</a>
                </li>
            </c:if>
        </ul>
        <form method="post" action="${pageContext.request.contextPath}/logout">
            <button type="submit" class="btn btn-outline-light btn-sm">Salir</button>
        </form>
    </div>
</nav>