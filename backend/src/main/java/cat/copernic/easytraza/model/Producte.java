package cat.copernic.easytraza.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "productes")
/**
 * Producte fabricat del catàleg.
 */
public class Producte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producte")
    private Long idProducte;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(length = 500)
    private String descripcio;

    public Producte() {}

    public Producte(String nom, String descripcio) {
        this.nom = nom;
        this.descripcio = descripcio;
    }

    @JsonProperty("id")
    public Long getIdProducte() { return idProducte; }
    public void setIdProducte(Long idProducte) { this.idProducte = idProducte; }
    public Long getId() { return idProducte; }
    @com.fasterxml.jackson.annotation.JsonIgnore
    public void setId(Long id) { this.idProducte = id; }

    @JsonProperty("nombre")
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getNombre() { return nom; }

    @JsonProperty("descripcion")
    public String getDescripcio() { return descripcio; }
    public void setDescripcio(String descripcio) { this.descripcio = descripcio; }
    public String getDescripcion() { return descripcio; }
}
