-- Base de datos del parcial de logística (MySQL 8)
DROP DATABASE IF EXISTS logistica;
CREATE DATABASE logistica CHARACTER SET utf8mb4;
USE logistica;

CREATE TABLE camion (
    id INT AUTO_INCREMENT PRIMARY KEY,
    marca VARCHAR(50) NOT NULL,
    modelo VARCHAR(50) NOT NULL,
    dominio VARCHAR(7) NOT NULL UNIQUE,
    toneladas_max DOUBLE NOT NULL,
    capacidad_tanque_litros DOUBLE NOT NULL,
    consumo_litros_km DOUBLE NOT NULL
);

CREATE TABLE chofer (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    dni VARCHAR(15) NOT NULL UNIQUE,
    fecha_nacimiento DATE NOT NULL,
    categoria VARCHAR(2) NOT NULL,          -- C1, C2 o C3
    telefono VARCHAR(15) NOT NULL
);

-- Camiones que cada chofer está autorizado a manejar (N a N)
CREATE TABLE chofer_camion (
    chofer_id INT NOT NULL,
    camion_id INT NOT NULL,
    PRIMARY KEY (chofer_id, camion_id),
    FOREIGN KEY (chofer_id) REFERENCES chofer(id) ON DELETE CASCADE,
    FOREIGN KEY (camion_id) REFERENCES camion(id)
);

-- La clave se guarda en texto plano (como en clase)
CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    clave VARCHAR(50) NOT NULL,
    rol VARCHAR(10) NOT NULL,               -- ADMIN o CHOFER
    chofer_id INT NULL,
    FOREIGN KEY (chofer_id) REFERENCES chofer(id) ON DELETE CASCADE
);

CREATE TABLE sesion_recordada (
    token VARCHAR(36) PRIMARY KEY,
    usuario_id INT NOT NULL,
    expira DATETIME NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id) ON DELETE CASCADE
);

CREATE TABLE distancia (
    origen VARCHAR(20) NOT NULL,
    destino VARCHAR(20) NOT NULL,
    km INT NOT NULL,
    PRIMARY KEY (origen, destino)
);

CREATE TABLE viaje (
    id INT AUTO_INCREMENT PRIMARY KEY,
    chofer_id INT NOT NULL,
    camion_id INT NOT NULL,
    origen VARCHAR(20) NOT NULL,
    destino VARCHAR(20) NOT NULL,
    distancia_km INT NOT NULL,
    dias_estimados INT NOT NULL,
    litros_estimados DOUBLE NOT NULL,
    tanques INT NOT NULL,
    estado VARCHAR(12) NOT NULL DEFAULT 'ASIGNADO',   -- ASIGNADO, EN_CURSO, FINALIZADO
    fecha_carga DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_inicio DATETIME NULL,
    fecha_fin DATETIME NULL,
    FOREIGN KEY (chofer_id) REFERENCES chofer(id),
    FOREIGN KEY (camion_id) REFERENCES camion(id)
);

DELIMITER //

-- Camiones que el chofer puede manejar (autorizados, alcanza su categoría y sin viaje pendiente)
CREATE PROCEDURE sp_camiones_disponibles(IN p_chofer_id INT)
BEGIN
    SELECT c.id, c.marca, c.modelo, c.dominio, c.toneladas_max, c.capacidad_tanque_litros, c.consumo_litros_km
    FROM camion c
    JOIN chofer_camion cc ON cc.camion_id = c.id
    JOIN chofer ch ON ch.id = cc.chofer_id
    WHERE cc.chofer_id = p_chofer_id
      AND c.toneladas_max <= CASE ch.categoria WHEN 'C1' THEN 12 WHEN 'C2' THEN 24 ELSE 999999 END
      AND NOT EXISTS (SELECT 1 FROM viaje v
                      WHERE v.camion_id = c.id AND v.estado IN ('ASIGNADO', 'EN_CURSO'))
    ORDER BY c.marca, c.modelo, c.dominio;
END //

-- Finaliza un viaje EN_CURSO del chofer; p_filas devuelve 1 si se pudo y 0 si no
CREATE PROCEDURE sp_finalizar_viaje(IN p_viaje_id INT, IN p_chofer_id INT, OUT p_filas INT)
BEGIN
    UPDATE viaje SET estado = 'FINALIZADO', fecha_fin = NOW()
    WHERE id = p_viaje_id AND chofer_id = p_chofer_id AND estado = 'EN_CURSO';
    SET p_filas = ROW_COUNT();
END //

DELIMITER ;

-- Distancias aproximadas en km (se cargan en un sentido y después se copian al revés)
INSERT INTO distancia (origen, destino, km) VALUES
('CABA','CORDOBA',700), ('CABA','CORRIENTES',1000), ('CABA','FORMOSA',1190), ('CABA','LA_PLATA',60),
('CABA','LA_RIOJA',1170), ('CABA','MENDOZA',1050), ('CABA','NEUQUEN',1150),
('CORDOBA','CORRIENTES',900), ('CORDOBA','FORMOSA',1100), ('CORDOBA','LA_PLATA',760),
('CORDOBA','LA_RIOJA',470), ('CORDOBA','MENDOZA',680), ('CORDOBA','NEUQUEN',1020),
('CORRIENTES','FORMOSA',230), ('CORRIENTES','LA_PLATA',1060), ('CORRIENTES','LA_RIOJA',1250),
('CORRIENTES','MENDOZA',1700), ('CORRIENTES','NEUQUEN',1900),
('FORMOSA','LA_PLATA',1250), ('FORMOSA','LA_RIOJA',1180), ('FORMOSA','MENDOZA',1800), ('FORMOSA','NEUQUEN',2100),
('LA_PLATA','LA_RIOJA',1230), ('LA_PLATA','MENDOZA',1110), ('LA_PLATA','NEUQUEN',1200),
('LA_RIOJA','MENDOZA',580), ('LA_RIOJA','NEUQUEN',1230),
('MENDOZA','NEUQUEN',780);
INSERT INTO distancia (origen, destino, km) SELECT destino, origen, km FROM distancia;

-- Usuario administrador: admin / admin
INSERT INTO usuario (username, clave, rol, chofer_id) VALUES ('admin', 'admin', 'ADMIN', NULL);

-- Datos de prueba
INSERT INTO camion (marca, modelo, dominio, toneladas_max, capacidad_tanque_litros, consumo_litros_km) VALUES
('Scania', 'R450', 'AB123CD', 10, 400, 0.35),
('Mercedes-Benz', 'Actros', 'AC456DE', 20, 600, 0.40),
('Volvo', 'FH16', 'AD789FG', 30, 800, 0.45);
