package cat.copernic.easytraza.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@IdClass(LiniarClientId.class)
@Table(name = "clients_albarans_linies")
public class LiniarClient {

    @Id
    @Column(name = "client_nif", length = 20)
    private String nifClient;

    @Id
    @Column(name = "data_produccio")
    private LocalDate dataProduccio;

    @Id
    @Column(name = "producte_id", nullable = false)
    private Long idProducte;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "client_nif", referencedColumnName = "client_nif", insertable = false, updatable = false),
        @JoinColumn(name = "data_produccio", referencedColumnName = "data_produccio", insertable = false, updatable = false)
    })
    @JsonIgnoreProperties({"linies", "lotsAssociats"})
    private AlbarraClient albarraClient;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "producte_id", referencedColumnName = "id_producte", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Producte producte;

    @Column(nullable = false)
    private Double quantitat;

    public LiniarClient() {}

    public LiniarClient(AlbarraClient albarraClient, Producte producte, Double quantitat) {
        if (albarraClient != null) {
            this.nifClient = albarraClient.getNifClient();
            this.dataProduccio = albarraClient.getDataProduccio();
            this.albarraClient = albarraClient;
        }
        if (producte != null) {
            this.producte = producte;
            this.idProducte = producte.getIdProducte();
        }
        this.quantitat = quantitat;
    }

    public String getNifClient() { return nifClient; }
    public void setNifClient(String nifClient) { this.nifClient = nifClient; }

    public LocalDate getDataProduccio() { return dataProduccio; }
    public void setDataProduccio(LocalDate dataProduccio) { this.dataProduccio = dataProduccio; }

    @com.fasterxml.jackson.annotation.JsonProperty("id")
    public Long getIdProducte() { return idProducte; }
    public void setIdProducte(Long idProducte) { this.idProducte = idProducte; }

    public AlbarraClient getAlbarraClient() { return albarraClient; }
    public void setAlbarraClient(AlbarraClient albarraClient) {
        this.albarraClient = albarraClient;
        if (albarraClient != null) {
            this.nifClient = albarraClient.getNifClient();
            this.dataProduccio = albarraClient.getDataProduccio();
        }
    }

    public Producte getProducte() { return producte; }
    public void setProducte(Producte producte) {
        this.producte = producte;
        if (producte != null) this.idProducte = producte.getIdProducte();
    }

    public Double getQuantitat() { return quantitat; }
    public void setQuantitat(Double quantitat) { this.quantitat = quantitat; }
}
