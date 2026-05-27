package cat.copernic.easytraza.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "materies_primeres")
/**
 * Matèria primera del catàleg d'ingredients.
 */
public class MateriaPrimera {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 500)
    private String descripcio;

    public MateriaPrimera() {}

    public MateriaPrimera(String nom, String descripcio) {
        this.nom = nom;
        this.descripcio = descripcio;
    }

    @JsonProperty("id")
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    @JsonProperty("nombre")
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getNombre() { return nom; }

    @JsonProperty("descripcion")
    public String getDescripcio() { return descripcio; }
    public void setDescripcio(String descripcio) { this.descripcio = descripcio; }
    public String getDescripcion() { return descripcio; }
}
