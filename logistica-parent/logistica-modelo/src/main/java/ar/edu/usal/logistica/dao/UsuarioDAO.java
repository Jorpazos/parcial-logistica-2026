package ar.edu.usal.logistica.dao;

import ar.edu.usal.logistica.domain.Usuario;
import ar.edu.usal.logistica.exception.DAOException;

import java.time.LocalDateTime;

public interface UsuarioDAO {

    /** @return el usuario, o null si no existe */
    Usuario buscarPorUsername(String username) throws DAOException;

    void guardarToken(String token, long usuarioId, LocalDateTime expira) throws DAOException;

    /** @return el usuario dueño de un token vigente, o null si no existe o venció */
    Usuario buscarPorToken(String token) throws DAOException;

    void eliminarToken(String token) throws DAOException;
}