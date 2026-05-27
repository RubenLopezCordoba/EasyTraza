package cat.copernic.easytraza.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@IdClass(AlbarraProveidorId.class)
@Table(name = "albars_proveidor")
/**
 * Albarà de proveïdor amb les seves línies de lots i matèries primeres.
 */
public class AlbarraProveidor {

    @Id
    @Column(name = "proveedor_nif", length = 20)
    private String nifProveidor;

    @Id
    @Column(name = "id_albara", length = 50)
    private String idAlbarra;

    @ManyToOne
    @MapsId("nifProveidor")
    @JoinColumn(name = "proveedor_nif", referencedColumnName = "nif", nullable = false)
    private Provedor provedor;

    @ManyToOne
    @JoinColumn(name = "usuari_id", nullable = false)
    private Usuari usuari;

    @Column(nullable = false)
    private LocalDateTime dataRecepcio;

    @Column(nullable = false)
    private String provedorNombre;

    @Column(nullable = false)
    private String operarioNombre;

    private String operarioEmail;

    @OneToMany(mappedBy = "albara", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<LiniarProveidor> linies = new ArrayList<>();

    @Column(length = 500)
    private String observacions;

    private String imagenUrl;

    @Column(length = 2000)
    private String textoOcr;

    @Column(length = 5000)
    private String jsonParsejat;

    public AlbarraProveidor() {}

    public AlbarraProveidor(String idAlbarra, LocalDateTime dataRecepcio, Provedor provedor, Usuari usuari) {
        this.idAlbarra = idAlbarra;
        this.dataRecepcio = dataRecepcio;
        this.provedor = provedor;
        this.usuari = usuari;
        if (provedor != null) {
            this.nifProveidor = provedor.getNif();
            this.provedorNombre = provedor.getNombre();
        }
        if (usuari != null) {
            this.operarioNombre = usuari.getNombre();
            this.operarioEmail = usuari.getEmail();
        }
    }

    @JsonProperty("lots")
    public List<Lot> getLots() {
        if (linies == null) return new ArrayList<>();
        return linies.stream().map(LiniarProveidor::getLot).collect(Collectors.toList());
    }

    public String getNifProveidor() { return nifProveidor; }
    public void setNifProveidor(String nifProveidor) { this.nifProveidor = nifProveidor; }

    @JsonProperty("numAlbara")
    public String getIdAlbarra() { return idAlbarra; }
    public void setIdAlbarra(String idAlbarra) { this.idAlbarra = idAlbarra; }

    @JsonProperty("proveedor")
    public Provedor getProvedor() { return provedor; }
    public void setProvedor(Provedor provedor) {
        this.provedor = provedor;
        if (provedor != null) {
            this.nifProveidor = provedor.getNif();
            this.provedorNombre = provedor.getNombre();
        }
    }
    public Provedor getProveedor() { return provedor; }

    @JsonProperty("usuario")
    public Usuari getUsuari() { return usuari; }
    public void setUsuari(Usuari usuari) {
        this.usuari = usuari;
        if (usuari != null) {
            this.operarioNombre = usuari.getNombre();
            this.operarioEmail = usuari.getEmail();
        }
    }
    public Usuari getUsuario() { return usuari; }

    public LocalDateTime getDataRecepcio() { return dataRecepcio; }
    public void setDataRecepcio(LocalDateTime dataRecepcio) { this.dataRecepcio = dataRecepcio; }

    public String getProvedorNombre() { return provedorNombre; }
    public void setProvedorNombre(String provedorNombre) { this.provedorNombre = provedorNombre; }

    public String getOperarioNombre() { return operarioNombre; }
    public void setOperarioNombre(String operarioNombre) { this.operarioNombre = operarioNombre; }

    public String getOperarioEmail() { return operarioEmail; }
    public void setOperarioEmail(String operarioEmail) { this.operarioEmail = operarioEmail; }

    public List<LiniarProveidor> getLinies() { return linies; }
    public void setLinies(List<LiniarProveidor> linies) { this.linies = linies; }

    public String getObservacions() { return observacions; }
    public void setObservacions(String observacions) { this.observacions = observacions; }

    public String getImagenUrl() { return imagenUrl; }
    public void setImagenUrl(String imagenUrl) { this.imagenUrl = imagenUrl; }

    public String getTextoOcr() { return textoOcr; }
    public void setTextoOcr(String textoOcr) { this.textoOcr = textoOcr; }

    public String getJsonParsejat() { return jsonParsejat; }
    public void setJsonParsejat(String jsonParsejat) { this.jsonParsejat = jsonParsejat; }
}
