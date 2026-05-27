package cat.copernic.easytraza.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

@Entity
@Table(name = "clients")
/**
 * Client del sistema amb dades personals i de contacte.
 */
public class Client {

    @Id
    @Column(nullable = false, unique = true, length = 20)
    private String nif;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String cognoms;

    @Column(length = 200)
    private String adreca;

    @Column(nullable = false, length = 20)
    private String telefon;

    @Column(length = 100)
    private String email;

    @Column(length = 500)
    private String observacions;

    @Column(nullable = false)
    private Boolean activo = true;

    public Client() {}

    public Client(String nif, String nom, String cognoms) {
        this.nif = nif;
        this.nom = nom;
        this.cognoms = cognoms;
    }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getCognoms() { return cognoms; }
    public void setCognoms(String cognoms) { this.cognoms = cognoms; }

    public String getAdreca() { return adreca; }
    public void setAdreca(String adreca) { this.adreca = adreca; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getObservacions() { return observacions; }
    public void setObservacions(String observacions) { this.observacions = observacions; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }
}
