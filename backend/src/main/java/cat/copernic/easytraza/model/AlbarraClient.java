package cat.copernic.easytraza.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@IdClass(AlbarraClientId.class)
@Table(name = "clients_albarans")
/**
 * Albarà de client amb les seves línies de producció i lots associats.
 */
public class AlbarraClient {

    @Id
    @Column(name = "client_nif", length = 20)
    private String nifClient;

    @Id
    @Column(name = "data_produccio")
    private LocalDate dataProduccio;

    @ManyToOne
    @MapsId("nifClient")
    @JoinColumn(name = "client_nif", referencedColumnName = "nif", nullable = false)
    private Client client;

    @ManyToOne
    @JoinColumn(name = "usuari_id", nullable = false)
    private Usuari operari;

    @Column(nullable = false)
    private String estat = "NO_LLIURAT";

    private LocalDateTime dataLliurament;

    private String nomClient;

    @OneToMany(mappedBy = "albarraClient", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<LiniarClient> linies = new ArrayList<>();

    @OneToMany(mappedBy = "albara", cascade = CascadeType.REMOVE, fetch = FetchType.EAGER)
    private List<LiniarClientLot> lotsAssociats = new ArrayList<>();

    public AlbarraClient() {}

    public AlbarraClient(Client client, LocalDate dataProduccio, Usuari operari) {
        this.client = client;
        this.nifClient = client.getNif();
        this.dataProduccio = dataProduccio;
        this.operari = operari;
        this.nomClient = client.getNom() + " " + client.getCognoms();
    }

    public String getNifClient() { return nifClient; }
    public void setNifClient(String nifClient) { this.nifClient = nifClient; }

    @com.fasterxml.jackson.annotation.JsonProperty("dataProduccio")
    public LocalDate getDataProduccio() { return dataProduccio; }
    public void setDataProduccio(LocalDate dataProduccio) { this.dataProduccio = dataProduccio; }

    public Client getClient() { return client; }
    public void setClient(Client client) {
        this.client = client;
        if (client != null) {
            this.nifClient = client.getNif();
            this.nomClient = client.getNom() + " " + client.getCognoms();
        }
    }

    public Usuari getOperari() { return operari; }
    public void setOperari(Usuari operari) { this.operari = operari; }

    public String getEstat() { return estat; }
    public void setEstat(String estat) { this.estat = estat; }

    public LocalDateTime getDataLliurament() { return dataLliurament; }
    public void setDataLliurament(LocalDateTime dataLliurament) { this.dataLliurament = dataLliurament; }

    public String getNomClient() { return nomClient; }
    public void setNomClient(String nomClient) { this.nomClient = nomClient; }

    public List<LiniarClient> getLinies() { return linies; }
    public void setLinies(List<LiniarClient> linies) { this.linies = linies; }

    public List<LiniarClientLot> getLotsAssociats() { return lotsAssociats; }
    public void setLotsAssociats(List<LiniarClientLot> lotsAssociats) { this.lotsAssociats = lotsAssociats; }
}
