package cat.copernic.easytraza.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import java.util.List;

@Entity
@Table(name = "proveedores")
/**
 * Proveïdor amb dades fiscals i de contacte.
 */
public class Provedor {

    @Id
    @NotBlank(message = "El NIF es obligatorio")
    @Column(name = "nif", length = 20, unique = true, nullable = false)
    @Length(min = 8, max = 10, message = "El NIF debe tener entre 8 y 10 caracteres")
    private String nif;

    @NotBlank(message = "El nombre es obligatorio")
    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "telefono", length = 20)
    private String telefono;

    @Column(name = "direccion", length = 200)
    private String direccion;

    @Column(name = "observaciones", length = 500)
    private String observaciones;

    @JsonIgnore
    @OneToMany(mappedBy = "provedor")
    private List<AlbarraProveidor> albarans;

    public Provedor() {}

    public Provedor(String nif, String nombre, String telefono, String direccion, String observaciones) {
        this.nif = nif;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.observaciones = observaciones;
    }

    @JsonProperty("cif")
    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }
    public String getCif() { return nif; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public List<AlbarraProveidor> getAlbarans() { return albarans; }
    public void setAlbarans(List<AlbarraProveidor> albarans) { this.albarans = albarans; }
}
