package cat.copernic.easytraza.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "linies_client_lots")
public class LiniarClientLot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "client_nif", referencedColumnName = "client_nif"),
        @JoinColumn(name = "data_produccio", referencedColumnName = "data_produccio")
    })
    @JsonIgnoreProperties({"lotsAssociats", "linies"})
    private AlbarraClient albara;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "lot_proveidor_nif", referencedColumnName = "proveedor_nif"),
        @JoinColumn(name = "lot_id", referencedColumnName = "id_lot")
    })
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Lot lot;

    public LiniarClientLot() {}

    public LiniarClientLot(AlbarraClient albara, Lot lot) {
        this.albara = albara;
        this.lot = lot;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public AlbarraClient getAlbara() { return albara; }
    public void setAlbara(AlbarraClient albara) { this.albara = albara; }
    public Lot getLot() { return lot; }
    public void setLot(Lot lot) { this.lot = lot; }
}
