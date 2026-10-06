package ar.edu.usal.logistica.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

public final class PasswordUtil {

    private PasswordUtil() {
    }

    /** Convierte la clave en un texto de 64 caracteres (SHA-256). La misma clave da siempre el mismo resultado. */
    public static String hashear(String clave) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha.digest(clave.getBytes(StandardCharsets.UTF_8));
            return String.format("%064x", new BigInteger(1, hash));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible en esta JVM", e);
        }
    }

    /** Compara la clave ingresada con el hash guardado en la base. */
    public static boolean verificar(String clave, String hashGuardado) {
        return clave != null && hashGuardado != null && hashear(clave).equals(hashGuardado);
    }

    /** Texto al azar para la cookie "recordarme" (no tiene nada que ver con la clave). */
    public static String generarToken() {
        return UUID.randomUUID().toString();
    }
}
