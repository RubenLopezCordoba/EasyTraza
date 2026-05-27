    package cat.copernic.easytraza.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "usuarios")
/**
 * Usuari del sistema amb credencials, rol i dades personals.
 */
public class Usuari {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "rol", nullable = false, length = 20)
    private String rol;

    @Column(name = "es_admin")
    private Boolean esAdmin = false;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "nif", length = 15)
    private String nif;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "ultimo_acceso")
    private LocalDateTime ultimoAcceso;

    @Column(name = "password_cambiada")
    private Boolean passwordCambiada = false;

    @Column(length = 500)
    private String fotoUrl;

    @JsonIgnore
    @OneToMany(mappedBy = "usuari")
    private List<AlbarraProveidor> albarans;

    @JsonIgnore
    @OneToMany(mappedBy = "usuariInici")
    private List<Lot> lotsIniciats;

    public Usuari() {}

    public Usuari(String nombre, String email, String password, String rol, String telefono, String nif) {
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.telefono = telefono;
        this.nif = nif;
        this.activo = true;
        this.fechaCreacion = LocalDateTime.now();
    }

    public String getIniciales() {
        if (nombre == null || nombre.isEmpty()) {
            return "??";
        }
        String[] partes = nombre.split(" ");
        if (partes.length == 1) {
            return partes[0].substring(0, Math.min(2, partes[0].length())).toUpperCase();
        }
        return (partes[0].charAt(0) + "" + partes[partes.length - 1].charAt(0)).toUpperCase();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }

    public Rol getRolEnum() { return Rol.fromString(rol); }
    public void setRolEnum(Rol rol) { this.rol = rol != null ? rol.name() : null; }

    public Boolean getEsAdmin() { return esAdmin; }
    public void setEsAdmin(Boolean esAdmin) { this.esAdmin = esAdmin; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public LocalDateTime getUltimoAcceso() { return ultimoAcceso; }
    public void setUltimoAcceso(LocalDateTime ultimoAcceso) { this.ultimoAcceso = ultimoAcceso; }

    public Boolean isPasswordCambiada() { return passwordCambiada; }
    public void setPasswordCambiada(Boolean passwordCambiada) { this.passwordCambiada = passwordCambiada; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public List<AlbarraProveidor> getAlbarans() { return albarans; }
    public void setAlbarans(List<AlbarraProveidor> albarans) { this.albarans = albarans; }

    public List<Lot> getLotsIniciats() { return lotsIniciats; }
    public void setLotsIniciats(List<Lot> lotsIniciats) { this.lotsIniciats = lotsIniciats; }
}
