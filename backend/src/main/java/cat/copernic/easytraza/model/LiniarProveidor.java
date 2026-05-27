package cat.copernic.easytraza.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@IdClass(LiniarProveidorId.class)
@Table(name = "linies_proveidor")
/**
 * Línia d'un albarà de proveïdor amb lot i matèria primera.
 */
public class LiniarProveidor {

    @Id
    @Column(name = "proveedor_nif", length = 20)
    private String nifProveidor;

    @Id
    @Column(name = "id_albara", length = 50)
    private String idAlbarra;

    @Id
    @Column(name = "id_lot", length = 50)
    private String idLot;

    @ManyToOne
    @MapsId("nifProveidor")
    @JoinColumn(name = "proveedor_nif", referencedColumnName = "nif", nullable = false)
    private Provedor provedor;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "proveedor_nif", referencedColumnName = "proveedor_nif", insertable = false, updatable = false),
        @JoinColumn(name = "id_albara", referencedColumnName = "id_albara", insertable = false, updatable = false)
    })
    @JsonIgnoreProperties({"linies"})
    private AlbarraProveidor albara;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "proveedor_nif", referencedColumnName = "proveedor_nif", insertable = false, updatable = false),
        @JoinColumn(name = "id_lot", referencedColumnName = "id_lot", insertable = false, updatable = false)
    })
    private Lot lot;

    @Column(nullable = false)
    private Integer quantitat;

    @Column(nullable = false)
    private String unitat;

    public LiniarProveidor() {}

    public LiniarProveidor(AlbarraProveidor albara, Lot lot, Integer quantitat, String unitat) {
        this.albara = albara;
        this.lot = lot;
        this.quantitat = quantitat;
        this.unitat = unitat;
        if (albara != null) {
            this.nifProveidor = albara.getNifProveidor();
            this.idAlbarra = albara.getIdAlbarra();
            this.provedor = albara.getProvedor();
        }
        if (lot != null) {
            this.idLot = lot.getIdLot();
        }
    }

    public String getNifProveidor() { return nifProveidor; }
    public void setNifProveidor(String nifProveidor) { this.nifProveidor = nifProveidor; }

    public String getIdAlbarra() { return idAlbarra; }
    public void setIdAlbarra(String idAlbarra) { this.idAlbarra = idAlbarra; }

    public String getIdLot() { return idLot; }
    public void setIdLot(String idLot) { this.idLot = idLot; }

    public Provedor getProvedor() { return provedor; }
    public void setProvedor(Provedor provedor) { this.provedor = provedor; }

    public AlbarraProveidor getAlbara() { return albara; }
    public void setAlbara(AlbarraProveidor albara) { this.albara = albara; }

    public Lot getLot() { return lot; }
    public void setLot(Lot lot) { this.lot = lot; }

    public Integer getQuantitat() { return quantitat; }
    public void setQuantitat(Integer quantitat) { this.quantitat = quantitat; }

    public String getUnitat() { return unitat; }
    public void setUnitat(String unitat) { this.unitat = unitat; }
}
