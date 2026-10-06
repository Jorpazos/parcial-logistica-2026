<%--
  Created by IntelliJ IDEA.
  User: jorge
  Date: 5/10/2026
  Time: 21:57
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <title>Ingresar - Logística</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
<div class="container" style="max-width: 420px; margin-top: 12vh;">
    <div class="card shadow-sm">
        <div class="card-body p-4">
            <h4 class="mb-4 text-center">Logística</h4>
            <form method="post" action="${pageContext.request.contextPath}/login">
                <div class="mb-3">
                    <label class="form-label" for="username">Usuario</label>
                    <input class="form-control" id="username" name="username" required autofocus>
                </div>
                <div class="mb-3">
                    <label class="form-label" for="clave">Contraseña</label>
                    <input class="form-control" id="clave" name="clave" type="password" required>
                </div>
                <div class="form-check mb-3">
                    <input class="form-check-input" type="checkbox" id="recordarme" name="recordarme">
                    <label class="form-check-label" for="recordarme">Recordarme</label>
                </div>
                <button class="btn btn-primary w-100" type="submit">Ingresar</button>
            </form>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<c:if test="${not empty error}">
    <script>
        Swal.fire({ icon: 'error', title: 'No se pudo ingresar', text: '<c:out value="${error}"/>' });
    </script>
</c:if>
</body>
</html>
