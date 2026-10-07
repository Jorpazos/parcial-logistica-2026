<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Nuevo viaje</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css">
</head>
<body>
<jsp:include page="/WEB-INF/jsp/menu.jsp"/>

<div class="container mt-4">
    <h2>Nuevo viaje</h2>
    <p>Chofer: <strong><c:out value="${chofer.nombreCompleto}"/></strong>
        (DNI <c:out value="${chofer.dni}"/>, categoria ${chofer.categoria})</p>

    <c:choose>
        <c:when test="${empty camiones}">
            <div class="alert alert-warning">El chofer no tiene camiones disponibles.</div>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/viajes">Volver</a>
        </c:when>
        <c:otherwise>
            <form method="post" action="${pageContext.request.contextPath}/admin/viajes">
                <input type="hidden" name="choferId" value="${chofer.id}">

                <div class="row">
                    <div class="col-md-4 mb-3">
                        <label class="form-label" for="camionId">Camion</label>
                        <select class="form-select" id="camionId" name="camionId" required>
                            <c:forEach var="cam" items="${camiones}">
                                <option value="${cam.id}" ${camionElegido.id == cam.id ? 'selected' : ''}>
                                    <c:out value="${cam.descripcion}"/>
                                </option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-4 mb-3">
                        <label class="form-label" for="origen">Origen</label>
                        <select class="form-select" id="origen" name="origen" required>
                            <c:forEach var="d" items="${destinos}">
                                <option value="${d}" ${origenElegido == d ? 'selected' : ''}>${d.nombre}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-4 mb-3">
                        <label class="form-label" for="destino">Destino</label>
                        <select class="form-select" id="destino" name="destino" required>
                            <c:forEach var="d" items="${destinos}">
                                <option value="${d}" ${destinoElegido == d ? 'selected' : ''}>${d.nombre}</option>
                            </c:forEach>
                        </select>
                    </div>
                </div>

                <button type="submit" name="accion" value="calcular" class="btn btn-secondary">Calcular</button>
                <c:if test="${not empty viaje}">
                    <button type="submit" name="accion" value="guardar" class="btn btn-success">Guardar viaje</button>
                </c:if>
                <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/admin/viajes">Volver</a>
            </form>
        </c:otherwise>
    </c:choose>

    <c:if test="${not empty viaje}">
        <div class="card mt-4">
            <div class="card-body">
                <h5 class="card-title">Calculo del viaje</h5>
                <p class="mb-1">Distancia: ${viaje.estimacion.distanciaKm} km</p>
                <p class="mb-1">Tiempo estimado: ${viaje.estimacion.dias} dia(s)</p>
                <p class="mb-1">Combustible: ${viaje.estimacion.litrosTotales} litros</p>
                <p class="mb-0">Tanques necesarios: ${viaje.estimacion.tanques}</p>
            </div>
        </div>
    </c:if>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
