<%--
  Created by IntelliJ IDEA.
  User: jorge
  Date: 5/10/2026
  Time: 22:55
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Camiones</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
</head>
<body>
<jsp:include page="/WEB-INF/jsp/menu.jsp"/>

<div class="container mt-4">
    <h2>Camiones</h2>

    <h5 class="mt-4">
        <c:choose>
            <c:when test="${not empty camionEditar}">Modificar camion</c:when>
            <c:otherwise>Nuevo camion</c:otherwise>
        </c:choose>
    </h5>

    <form method="post" action="${pageContext.request.contextPath}/admin/camiones">
        <input type="hidden" name="accion" value="guardar">
        <input type="hidden" name="id" value="${camionEditar.id}">

        <div class="row">
            <div class="col-md-4 mb-3">
                <label class="form-label" for="marca">Marca</label>
                <input type="text" class="form-control" id="marca" name="marca"
                       value="<c:out value='${camionEditar.marca}'/>" required>
            </div>
            <div class="col-md-4 mb-3">
                <label class="form-label" for="modelo">Modelo</label>
                <input type="text" class="form-control" id="modelo" name="modelo"
                       value="<c:out value='${camionEditar.modelo}'/>" required>
            </div>
            <div class="col-md-4 mb-3">
                <label class="form-label" for="dominio">Dominio (patente)</label>
                <input type="text" class="form-control" id="dominio" name="dominio"
                       value="<c:out value='${camionEditar.dominio}'/>" required>
            </div>
        </div>
        <div class="row">
            <div class="col-md-4 mb-3">
                <label class="form-label" for="toneladas">Toneladas maximas</label>
                <input type="number" step="0.01" class="form-control" id="toneladas" name="toneladas"
                       value="${camionEditar.toneladasMaximas}" required>
            </div>
            <div class="col-md-4 mb-3">
                <label class="form-label" for="tanque">Capacidad del tanque (litros)</label>
                <input type="number" step="0.01" class="form-control" id="tanque" name="tanque"
                       value="${camionEditar.capacidadTanqueLitros}" required>
            </div>
            <div class="col-md-4 mb-3">
                <label class="form-label" for="consumo">Consumo (litros por km)</label>
                <input type="number" step="0.01" class="form-control" id="consumo" name="consumo"
                       value="${camionEditar.consumoLitrosPorKm}" required>
            </div>
        </div>

        <button type="submit" class="btn btn-primary">Guardar</button>
        <c:if test="${not empty camionEditar}">
            <a href="${pageContext.request.contextPath}/admin/camiones" class="btn btn-secondary">Cancelar</a>
        </c:if>
    </form>

    <h5 class="mt-5">Lista de camiones</h5>
    <table class="table table-bordered table-striped">
        <thead>
        <tr>
            <th>Marca</th>
            <th>Modelo</th>
            <th>Dominio</th>
            <th>Toneladas</th>
            <th>Tanque (l)</th>
            <th>Consumo (l/km)</th>
            <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="c" items="${camiones}">
            <tr>
                <td><c:out value="${c.marca}"/></td>
                <td><c:out value="${c.modelo}"/></td>
                <td><c:out value="${c.dominio}"/></td>
                <td>${c.toneladasMaximas}</td>
                <td>${c.capacidadTanqueLitros}</td>
                <td>${c.consumoLitrosPorKm}</td>
                <td>
                    <a class="btn btn-warning btn-sm"
                       href="${pageContext.request.contextPath}/admin/camiones?accion=editar&id=${c.id}">Editar</a>
                    <button type="button" class="btn btn-danger btn-sm"
                            onclick="confirmarEliminar(${c.id})">Eliminar</button>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty camiones}">
            <tr><td colspan="7" class="text-center">No hay camiones cargados</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<%-- Formulario oculto que usa el boton Eliminar (va por POST) --%>
<form id="formEliminar" method="post" action="${pageContext.request.contextPath}/admin/camiones">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" name="id" id="idEliminar">
</form>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
    function confirmarEliminar(id) {
        Swal.fire({
            title: 'Eliminar camion',
            text: 'Esta accion no se puede deshacer',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonText: 'Si, eliminar',
            cancelButtonText: 'Cancelar'
        }).then(function (resultado) {
            if (resultado.isConfirmed) {
                document.getElementById('idEliminar').value = id;
                document.getElementById('formEliminar').submit();
            }
        });
    }
</script>

<%-- Mensaje que dejo el servlet en la Session: se muestra una vez y se borra --%>
<c:if test="${not empty sessionScope.aviso}">
    <script>
        Swal.fire({
            icon: '<c:out value="${sessionScope.avisoTipo}"/>',
            text: '<c:out value="${sessionScope.aviso}"/>'
        });
    </script>
    <c:remove var="aviso" scope="session"/>
    <c:remove var="avisoTipo" scope="session"/>
</c:if>
</body>
</html>