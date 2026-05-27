package cat.copernic.easytraza.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@IdClass(LotId.class)
@Table(name = "lots_proveidor", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"proveedor_nif", "id_lot"})
})
/**
 * Lot de producció amb estat, quantitat i matèria primera associada.
 */
public class Lot {

    @Id
    @Column(name = "proveedor_nif", length = 20)
    private String nifProveidor;

    @Id
    @Column(name = "id_lot", length = 50)
    private String idLot;

    @ManyToOne
    @MapsId("nifProveidor")
    @JoinColumn(name = "proveedor_nif", referencedColumnName = "nif", nullable = false)
    private Provedor provedor;

    @ManyToOne
    @JoinColumn(name = "materia_primera_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private MateriaPrimera materiaPrimera;

    @Column(nullable = false)
    private String estat;

    @ManyToOne
    @JoinColumn(name = "usuari_inici_id")
    private Usuari usuariInici;

    private LocalDate dataProduccio;
    private LocalDateTime dataCaducitat;
    private LocalDateTime dataObertura;
    private LocalDateTime dataAcabament;
    private LocalDateTime dataRecepcio;

    private String ubicacio;
    private String lotProveidor;

    @Transient
    private String descripcio;

    @Column(nullable = false)
    private Integer quantitat;

    @Column(nullable = false)
    private String unitat;

    public Lot() {}

    public Lot(String idLot, Provedor provedor, MateriaPrimera materiaPrimera) {
        this.idLot = idLot;
        this.provedor = provedor;
        this.nifProveidor = provedor.getNif();
        this.materiaPrimera = materiaPrimera;
        this.estat = "EN_ESTOC";
        this.dataRecepcio = LocalDateTime.now();
    }

    public String getNifProveidor() { return nifProveidor; }
    public void setNifProveidor(String nifProveidor) { this.nifProveidor = nifProveidor; }

    public String getIdLot() { return idLot; }
    public void setIdLot(String idLot) { this.idLot = idLot; }

    public Provedor getProvedor() { return provedor; }
    public void setProvedor(Provedor provedor) {
        this.provedor = provedor;
        if (provedor != null) this.nifProveidor = provedor.getNif();
    }

    @JsonProperty("catalogo")
    public MateriaPrimera getMateriaPrimera() { return materiaPrimera; }
    public void setMateriaPrimera(MateriaPrimera materiaPrimera) { this.materiaPrimera = materiaPrimera; }

    public String getEstat() { return estat; }
    public void setEstat(String estat) { this.estat = estat; }

    public Usuari getUsuariInici() { return usuariInici; }
    public void setUsuariInici(Usuari usuariInici) { this.usuariInici = usuariInici; }

    public LocalDate getDataProduccio() { return dataProduccio; }
    public void setDataProduccio(LocalDate dataProduccio) { this.dataProduccio = dataProduccio; }

    public LocalDateTime getDataCaducitat() { return dataCaducitat; }
    public void setDataCaducitat(LocalDateTime dataCaducitat) { this.dataCaducitat = dataCaducitat; }

    public LocalDateTime getDataObertura() { return dataObertura; }
    public void setDataObertura(LocalDateTime dataObertura) { this.dataObertura = dataObertura; }

    public LocalDateTime getDataAcabament() { return dataAcabament; }
    public void setDataAcabament(LocalDateTime dataAcabament) { this.dataAcabament = dataAcabament; }

    public LocalDateTime getDataRecepcio() { return dataRecepcio; }
    public void setDataRecepcio(LocalDateTime dataRecepcio) { this.dataRecepcio = dataRecepcio; }

    public String getUbicacio() { return ubicacio; }
    public void setUbicacio(String ubicacio) { this.ubicacio = ubicacio; }

    public String getLotProveidor() { return lotProveidor; }
    public void setLotProveidor(String lotProveidor) { this.lotProveidor = lotProveidor; }

    @JsonProperty("descripcion")
    public String getDescripcio() { return descripcio; }
    @JsonProperty("descripcion")
    public void setDescripcio(String descripcio) { this.descripcio = descripcio; }

    @JsonProperty("quantitat")
    public Integer getQuantitat() { return quantitat; }
    @JsonProperty("quantitat")
    public void setQuantitat(Integer quantitat) { this.quantitat = quantitat; }

    @JsonProperty("unitat")
    public String getUnitat() { return unitat; }
    @JsonProperty("unitat")
    public void setUnitat(String unitat) { this.unitat = unitat; }

    @JsonProperty("numLot")
    public String getNumLot() { return idLot; }

    @JsonProperty("id")
    public String getLotId() { return idLot; }
}
