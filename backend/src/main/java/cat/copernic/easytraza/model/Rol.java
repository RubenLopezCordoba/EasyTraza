package cat.copernic.easytraza.model;

/**
 * Enum amb els rols d'usuari del sistema: TRABAJADOR, ADMIN, SUPER_ADMIN.
 */
public enum Rol {
    TRABAJADOR,
    ADMIN,
    SUPER_ADMIN;

    public boolean esAdmin() {
        return this == ADMIN || this == SUPER_ADMIN;
    }

    public boolean potGestionarUsuaris() {
        return this == ADMIN || this == SUPER_ADMIN;
    }

    public static Rol fromString(String rol) {
        if (rol == null) return null;
        try {
            return Rol.valueOf(rol);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
