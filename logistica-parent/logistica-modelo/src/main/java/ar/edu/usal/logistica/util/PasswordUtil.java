package ar.edu.usal.logistica.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    /** Convierte la clave en un texto de 64 caracteres. La misma clave da siempre el mismo hash. */
    public static String hashear(String clave) {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] hash = sha.digest(clave.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 no está disponible en esta JVM", e);
        }
    }

    /** Compara la clave ingresada con el hash guardado en la base. */
    public static boolean verificar(String clave, String hashGuardado) {
        return clave != null && hashGuardado != null && hashear(clave).equals(hashGuardado);
    }

    /** Token aleatorio de 64 caracteres para la cookie "recordarme". */
    public static String generarToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}