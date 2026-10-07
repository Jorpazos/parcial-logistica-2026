-- Base de datos del parcial de logistica (MySQL 8)
-- Se ejecuta entero: borra la base si existe y la vuelve a crear con datos de prueba.
DROP DATABASE IF EXISTS logistica;
CREATE DATABASE logistica CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE logistica;

-- Categorias de la licencia nacional de conducir (C1 hasta 12 t, C2 hasta 24 t, C3 mas de 24 t)
CREATE TABLE categoria (
    codigo VARCHAR(2) NOT NULL PRIMARY KEY,
    toneladas_max INT NOT NULL,
    CONSTRAINT ck_categoria_ton CHECK (toneladas_max > 0)
);

-- Ciudades validas como origen y destino
CREATE TABLE destino (
    codigo VARCHAR(20) NOT NULL PRIMARY KEY,
    nombre VARCHAR(40) NOT NULL
);

CREATE TABLE camion (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    marca VARCHAR(40) NOT NULL,
    modelo VARCHAR(40) NOT NULL,
    dominio VARCHAR(10) NOT NULL,
    toneladas_max DECIMAL(6,2) NOT NULL,
    capacidad_tanque_litros DECIMAL(8,2) NOT NULL,
    consumo_litros_km DECIMAL(6,3) NOT NULL,
    CONSTRAINT uq_camion_dominio UNIQUE (dominio),
    CONSTRAINT ck_camion_ton CHECK (toneladas_max > 0),
    CONSTRAINT ck_camion_tanque CHECK (capacidad_tanque_litros > 0),
    CONSTRAINT ck_camion_consumo CHECK (consumo_litros_km > 0)
);

CREATE TABLE chofer (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    apellido VARCHAR(50) NOT NULL,
    dni VARCHAR(8) NOT NULL,
    fecha_nacimiento DATE NOT NULL,
    categoria VARCHAR(2) NOT NULL,
    telefono VARCHAR(15) NOT NULL,
    CONSTRAINT uq_chofer_dni UNIQUE (dni),
    CONSTRAINT fk_chofer_categoria FOREIGN KEY (categoria) REFERENCES categoria (codigo)
);

-- Camiones que cada chofer esta autorizado a manejar (relacion N a N)
CREATE TABLE chofer_camion (
    chofer_id BIGINT NOT NULL,
    camion_id BIGINT NOT NULL,
    PRIMARY KEY (chofer_id, camion_id),
    CONSTRAINT fk_cc_chofer FOREIGN KEY (chofer_id) REFERENCES chofer (id) ON DELETE CASCADE,
    CONSTRAINT fk_cc_camion FOREIGN KEY (camion_id) REFERENCES camion (id) ON DELETE CASCADE
);

-- La clave se guarda en texto plano (como en clase)
CREATE TABLE usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(30) NOT NULL,
    clave VARCHAR(50) NOT NULL,
    rol VARCHAR(10) NOT NULL,
    chofer_id BIGINT NULL,
    CONSTRAINT uq_usuario_username UNIQUE (username),
    CONSTRAINT uq_usuario_chofer UNIQUE (chofer_id),
    CONSTRAINT fk_usuario_chofer FOREIGN KEY (chofer_id) REFERENCES chofer (id) ON DELETE CASCADE,
    CONSTRAINT ck_usuario_rol CHECK ((rol = 'ADMIN' AND chofer_id IS NULL) OR (rol = 'CHOFER' AND chofer_id IS NOT NULL))
);

-- Tokens de la cookie "recordarme"
CREATE TABLE sesion_recordada (
    token VARCHAR(64) NOT NULL PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    expira DATETIME NOT NULL,
    CONSTRAINT fk_sesion_usuario FOREIGN KEY (usuario_id) REFERENCES usuario (id) ON DELETE CASCADE
);

CREATE TABLE distancia (
    origen VARCHAR(20) NOT NULL,
    destino VARCHAR(20) NOT NULL,
    km INT NOT NULL,
    PRIMARY KEY (origen, destino),
    CONSTRAINT fk_distancia_origen FOREIGN KEY (origen) REFERENCES destino (codigo),
    CONSTRAINT fk_distancia_destino FOREIGN KEY (destino) REFERENCES destino (codigo),
    CONSTRAINT ck_distancia_dist CHECK (origen <> destino),
    CONSTRAINT ck_distancia_km CHECK (km > 0)
);

CREATE TABLE viaje (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chofer_id BIGINT NOT NULL,
    camion_id BIGINT NOT NULL,
    origen VARCHAR(20) NOT NULL,
    destino VARCHAR(20) NOT NULL,
    distancia_km INT NOT NULL,
    dias_estimados INT NOT NULL,
    litros_estimados DECIMAL(10,2) NOT NULL,
    tanques INT NOT NULL,
    estado VARCHAR(12) NOT NULL DEFAULT 'ASIGNADO',
    fecha_carga DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_inicio DATETIME NULL,
    fecha_fin DATETIME NULL,
    CONSTRAINT fk_viaje_chofer FOREIGN KEY (chofer_id) REFERENCES chofer (id),
    CONSTRAINT fk_viaje_camion FOREIGN KEY (camion_id) REFERENCES camion (id),
    CONSTRAINT fk_viaje_origen FOREIGN KEY (origen) REFERENCES destino (codigo),
    CONSTRAINT fk_viaje_destino FOREIGN KEY (destino) REFERENCES destino (codigo),
    CONSTRAINT ck_viaje_estado CHECK (estado IN ('ASIGNADO', 'EN_CURSO', 'FINALIZADO')),
    CONSTRAINT ck_viaje_km CHECK (distancia_km > 0),
    CONSTRAINT ck_viaje_od CHECK (origen <> destino)
);

DELIMITER //

-- Camiones que el chofer puede manejar: autorizados, que su categoria alcance y sin viaje pendiente
CREATE PROCEDURE sp_camiones_disponibles(IN p_chofer_id BIGINT)
BEGIN
    SELECT c.id, c.marca, c.modelo, c.dominio, c.toneladas_max,
           c.capacidad_tanque_litros, c.consumo_litros_km
      FROM camion c
      JOIN chofer_camion cc ON cc.camion_id = c.id AND cc.chofer_id = p_chofer_id
      JOIN chofer ch        ON ch.id = cc.chofer_id
      JOIN categoria cat    ON cat.codigo = ch.categoria
     WHERE c.toneladas_max <= cat.toneladas_max
       AND NOT EXISTS (SELECT 1 FROM viaje v
                        WHERE v.camion_id = c.id
                          AND v.estado IN ('ASIGNADO', 'EN_CURSO'))
     ORDER BY c.marca, c.modelo, c.dominio;
END //

-- Finaliza un viaje EN_CURSO del chofer. p_filas devuelve 1 si se pudo y 0 si no
CREATE PROCEDURE sp_finalizar_viaje(IN p_viaje_id BIGINT, IN p_chofer_id BIGINT, OUT p_filas INT)
BEGIN
    UPDATE viaje
       SET estado = 'FINALIZADO', fecha_fin = NOW()
     WHERE id = p_viaje_id AND chofer_id = p_chofer_id AND estado = 'EN_CURSO';
    SET p_filas = ROW_COUNT();
END //

DELIMITER ;

-- ===================== Datos iniciales =====================

-- C3 no tiene tope: se usa un numero grande
INSERT INTO categoria (codigo, toneladas_max) VALUES ('C1', 12), ('C2', 24), ('C3', 999);

INSERT INTO destino (codigo, nombre) VALUES
('CABA', 'CABA'), ('CORDOBA', 'Córdoba'), ('CORRIENTES', 'Corrientes'), ('FORMOSA', 'Formosa'),
('LA_PLATA', 'La Plata'), ('LA_RIOJA', 'La Rioja'), ('MENDOZA', 'Mendoza'), ('NEUQUEN', 'Neuquén');

-- Distancias en km (se cargan en un sentido y despues se copian al reves)
INSERT INTO distancia (origen, destino, km) VALUES
('CABA', 'CORDOBA', 646), ('CABA', 'CORRIENTES', 792), ('CABA', 'FORMOSA', 933), ('CABA', 'LA_PLATA', 53),
('CABA', 'LA_RIOJA', 986), ('CABA', 'MENDOZA', 985), ('CABA', 'NEUQUEN', 989),
('CORDOBA', 'CORRIENTES', 677), ('CORDOBA', 'FORMOSA', 824), ('CORDOBA', 'LA_PLATA', 698),
('CORDOBA', 'LA_RIOJA', 340), ('CORDOBA', 'MENDOZA', 466), ('CORDOBA', 'NEUQUEN', 907),
('CORRIENTES', 'FORMOSA', 157), ('CORRIENTES', 'LA_PLATA', 830), ('CORRIENTES', 'LA_RIOJA', 814),
('CORRIENTES', 'MENDOZA', 1131), ('CORRIENTES', 'NEUQUEN', 1534),
('FORMOSA', 'LA_PLATA', 968), ('FORMOSA', 'LA_RIOJA', 927), ('FORMOSA', 'MENDOZA', 1269), ('FORMOSA', 'NEUQUEN', 1690),
('LA_PLATA', 'LA_RIOJA', 1038), ('LA_PLATA', 'MENDOZA', 1029), ('LA_PLATA', 'NEUQUEN', 1005),
('LA_RIOJA', 'MENDOZA', 427), ('LA_RIOJA', 'NEUQUEN', 1063),
('MENDOZA', 'NEUQUEN', 676);
INSERT INTO distancia (origen, destino, km) SELECT destino, origen, km FROM distancia;

INSERT INTO camion (marca, modelo, dominio, toneladas_max, capacidad_tanque_litros, consumo_litros_km) VALUES
('Scania', 'R450', 'AB123CD', 28, 600, 0.350),
('Mercedes-Benz', 'Actros', 'AC456EF', 25, 500, 0.320),
('Iveco', 'Stralis', 'AD789GH', 18, 400, 0.280),
('Volvo', 'FH 460', 'AE012IJ', 30, 700, 0.380),
('Ford', 'Cargo', 'AF345KL', 9, 250, 0.200);

INSERT INTO chofer (nombre, apellido, dni, fecha_nacimiento, categoria, telefono) VALUES
('Juan', 'Perez', '30111222', '1985-03-12', 'C3', '1155550001'),
('Maria', 'Gomez', '28999888', '1990-07-25', 'C2', '1155550002'),
('Luis', 'Ramirez', '35222333', '1993-11-02', 'C1', '1155550003');

INSERT INTO chofer_camion (chofer_id, camion_id) VALUES (1, 1), (1, 2), (1, 3), (2, 2), (2, 3), (3, 5);

-- Usuarios: admin / admin y cada chofer entra con su DNI y la clave 123456
INSERT INTO usuario (username, clave, rol, chofer_id) VALUES
('admin', 'admin', 'ADMIN', NULL),
('30111222', '123456', 'CHOFER', 1),
('28999888', '123456', 'CHOFER', 2),
('35222333', '123456', 'CHOFER', 3);
