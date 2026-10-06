<%--
  Created by IntelliJ IDEA.
  User: jorge
  Date: 5/10/2026
  Time: 23:02
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Choferes</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
</head>
<body>
<jsp:include page="/WEB-INF/jsp/menu.jsp"/>

<div class="container mt-4">
    <h2>Choferes</h2>

    <h5 class="mt-4">
        <c:choose>
            <c:when test="${not empty choferEditar}">Modificar chofer</c:when>
            <c:otherwise>Nuevo chofer</c:otherwise>
        </c:choose>
    </h5>

    <form method="post" action="${pageContext.request.contextPath}/admin/choferes">
        <input type="hidden" name="accion" value="guardar">
        <input type="hidden" name="id" value="${choferEditar.id}">

        <div class="row">
            <div class="col-md-4 mb-3">
                <label class="form-label" for="nombre">Nombre</label>
                <input type="text" class="form-control" id="nombre" name="nombre"
                       value="<c:out value='${choferEditar.nombre}'/>" required>
            </div>
            <div class="col-md-4 mb-3">
                <label class="form-label" for="apellido">Apellido</label>
                <input type="text" class="form-control" id="apellido" name="apellido"
                       value="<c:out value='${choferEditar.apellido}'/>" required>
            </div>
            <div class="col-md-4 mb-3">
                <label class="form-label" for="dni">DNI</label>
                <input type="text" class="form-control" id="dni" name="dni"
                       value="<c:out value='${choferEditar.dni}'/>" required>
            </div>
        </div>
        <div class="row">
            <div class="col-md-4 mb-3">
                <label class="form-label" for="fechaNacimiento">Fecha de nacimiento</label>
                <input type="date" class="form-control" id="fechaNacimiento" name="fechaNacimiento"
                       value="${choferEditar.fechaNacimiento}" required>
            </div>
            <div class="col-md-4 mb-3">
                <label class="form-label" for="telefono">Telefono celular</label>
                <input type="text" class="form-control" id="telefono" name="telefono"
                       value="<c:out value='${choferEditar.telefono}'/>" required>
            </div>
            <div class="col-md-4 mb-3">
                <label class="form-label" for="categoria">Categoria</label>
                <select class="form-select" id="categoria" name="categoria" required>
                    <c:forEach var="cat" items="${categorias}">
                        <option value="${cat}" ${choferEditar.categoria == cat ? 'selected' : ''}>
                                ${cat} (hasta ${cat.toneladasMaximas} t)
                        </option>
                    </c:forEach>
                </select>
            </div>
        </div>

        <%-- La clave inicial solo se pide al crear --%>
        <c:if test="${empty choferEditar}">
            <div class="row">
                <div class="col-md-4 mb-3">
                    <label class="form-label" for="clave">Clave inicial</label>
                    <input type="password" class="form-control" id="clave" name="clave" required>
                    <div class="form-text">El usuario para entrar sera su DNI.</div>
                </div>
            </div>
        </c:if>

        <div class="mb-3">
            <label class="form-label">Camiones autorizados</label>
            <c:forEach var="cam" items="${camiones}">
                <div class="form-check">
                    <input class="form-check-input" type="checkbox" name="camionId"
                           id="cam${cam.id}" value="${cam.id}"
                        ${idsAutorizados.contains(cam.id) ? 'checked' : ''}>
                    <label class="form-check-label" for="cam${cam.id}">
                        <c:out value="${cam.descripcion}"/> - ${cam.toneladasMaximas} t
                    </label>
                </div>
            </c:forEach>
        </div>

        <button type="submit" class="btn btn-primary">Guardar</button>
        <c:if test="${not empty choferEditar}">
            <a href="${pageContext.request.contextPath}/admin/choferes" class="btn btn-secondary">Cancelar</a>
        </c:if>
    </form>

    <h5 class="mt-5">Lista de choferes</h5>
    <table class="table table-bordered table-striped">
        <thead>
        <tr>
            <th>Apellido y nombre</th>
            <th>DNI</th>
            <th>Edad</th>
            <th>Categoria</th>
            <th>Telefono</th>
            <th>Camiones autorizados</th>
            <th>Acciones</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="ch" items="${choferes}">
            <tr>
                <td><c:out value="${ch.nombreCompleto}"/></td>
                <td><c:out value="${ch.dni}"/></td>
                <td>${ch.edad}</td>
                <td>${ch.categoria}</td>
                <td><c:out value="${ch.telefono}"/></td>
                <td>
                    <c:forEach var="cam" items="${ch.camionesAutorizados}">
                        <div><c:out value="${cam.descripcion}"/></div>
                    </c:forEach>
                </td>
                <td>
                    <a class="btn btn-warning btn-sm"
                       href="${pageContext.request.contextPath}/admin/choferes?accion=editar&id=${ch.id}">Editar</a>
                    <button type="button" class="btn btn-danger btn-sm"
                            onclick="confirmarEliminar(${ch.id})">Eliminar</button>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty choferes}">
            <tr><td colspan="7" class="text-center">No hay choferes cargados</td></tr>
        </c:if>
        </tbody>
    </table>
</div>

<%-- Formulario oculto que usa el boton Eliminar (va por POST) --%>
<form id="formEliminar" method="post" action="${pageContext.request.contextPath}/admin/choferes">
    <input type="hidden" name="accion" value="eliminar">
    <input type="hidden" name="id" id="idEliminar">
</form>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
<script>
    function confirmarEliminar(id) {
        Swal.fire({
            title: 'Eliminar chofer',
            text: 'Se borra tambien su usuario. Esta accion no se puede deshacer',
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