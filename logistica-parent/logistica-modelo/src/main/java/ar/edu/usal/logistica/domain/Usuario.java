package ar.edu.usal.logistica.domain;


public class Usuario {

    private final Long id;
    private final String username;
    private final String clave;
    private final Rol rol;
    private final Long choferId;     // null si es ADMIN

    public Usuario(Long id, String username, String clave, Rol rol, Long choferId) {
        this.id = id;
        this.username = username;
        this.clave = clave;
        this.rol = rol;
        this.choferId = choferId;
    }

    public boolean esAdmin() {
        return rol == Rol.ADMIN;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getClave() { return clave; }
    public Rol getRol() { return rol; }
    public Long getChoferId() { return choferId; }
}