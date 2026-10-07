<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Viajes</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
</head>
<body>
<jsp:include page="/WEB-INF/jsp/menu.jsp"/>

<div class="container mt-4">
    <h2>Viajes</h2>

    <h5 class="mt-4">Nuevo viaje</h5>
    <form method="get" action="${pageContext.request.contextPath}/admin/viajes" class="row g-2 mb-4">
        <div class="col-md-4">
            <input type="text" class="form-control" name="dni" placeholder="DNI del chofer" required>
        </div>
        <div class="col-md-2">
            <button type="submit" class="btn btn-primary">Buscar chofer</button>
        </div>
    </form>

    <table class="table table-striped">
        <thead>
        <tr>
            <th>Chofer</th>
            <th>Camion</th>
            <th>Origen</th>
            <th>Destino</th>
            <th>Km</th>
            <th>Dias</th>
            <th>Tanques</th>
            <th>Estado</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="v" items="${viajes}">
            <tr>
                <td><c:out value="${v.chofer.nombreCompleto}"/></td>
                <td><c:out value="${v.camion.descripcion}"/></td>
                <td>${v.origen.nombre}</td>
                <td>${v.destino.nombre}</td>
                <td>${v.estimacion.distanciaKm}</td>
                <td>${v.estimacion.dias}</td>
                <td>${v.estimacion.tanques}</td>
                <td>${v.estado.descripcion}</td>
            </tr>
        </c:forEach>
        <c:if test="${empty viajes}">
            <tr><td colspan="8" class="text-center">No hay viajes cargados</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<c:if test="${not empty aviso}">
    <script>
        Swal.fire({
            icon: '<c:out value="${avisoTipo}"/>',
            text: '<c:out value="${aviso}"/>'
        });
    </script>
</c:if>
</body>
</html>
