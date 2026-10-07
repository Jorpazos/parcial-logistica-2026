<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Mis viajes</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
</head>
<body>
<jsp:include page="/WEB-INF/jsp/menu.jsp"/>

<div class="container mt-4">
    <h2>Mis viajes</h2>

    <table class="table table-striped">
        <thead>
        <tr>
            <th>Camion</th>
            <th>Origen</th>
            <th>Destino</th>
            <th>Km</th>
            <th>Dias</th>
            <th>Tanques</th>
            <th>Estado</th>
            <th></th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="v" items="${viajes}">
            <tr>
                <td><c:out value="${v.camion.descripcion}"/></td>
                <td>${v.origen.nombre}</td>
                <td>${v.destino.nombre}</td>
                <td>${v.estimacion.distanciaKm}</td>
                <td>${v.estimacion.dias}</td>
                <td>${v.estimacion.tanques}</td>
                <td>${v.estado.descripcion}</td>
                <td>
                    <c:if test="${v.puedeIniciarse()}">
                        <button class="btn btn-primary btn-sm" onclick="cambiar(${v.id}, 'iniciar')">Iniciar</button>
                    </c:if>
                    <c:if test="${v.puedeFinalizarse()}">
                        <button class="btn btn-success btn-sm" onclick="cambiar(${v.id}, 'finalizar')">Finalizar</button>
                    </c:if>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty viajes}">
            <tr><td colspan="8" class="text-center">No tenes viajes asignados</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script src="https://code.jquery.com/jquery-3.7.1.min.js"></script>
<script>
    // Iniciar y finalizar van por PUT con $.ajax
    function cambiar(id, accion) {
        $.ajax({
            url: '${pageContext.request.contextPath}/chofer/viajes?id=' + id + '&accion=' + accion,
            type: 'PUT',
            success: function (mensaje) {
                Swal.fire({icon: 'success', text: mensaje}).then(function () {
                    location.reload();
                });
            },
            error: function (respuesta) {
                Swal.fire({icon: 'warning', text: respuesta.responseText || 'No se pudo completar la accion'});
            }
        });
    }
</script>
</body>
</html>
