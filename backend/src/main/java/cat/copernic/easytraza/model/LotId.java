package cat.copernic.easytraza.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clau composta per a Lot (nifProveidor + idLot).
 */
public class LotId implements Serializable {
    private String nifProveidor;
    private String idLot;

    public LotId() {}
    public LotId(String nifProveidor, String idLot) {
        this.nifProveidor = nifProveidor;
        this.idLot = idLot;
    }

    public String getNifProveidor() { return nifProveidor; }
    public void setNifProveidor(String nifProveidor) { this.nifProveidor = nifProveidor; }
    public String getIdLot() { return idLot; }
    public void setIdLot(String idLot) { this.idLot = idLot; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LotId)) return false;
        LotId lotId = (LotId) o;
        return Objects.equals(nifProveidor, lotId.nifProveidor) &&
               Objects.equals(idLot, lotId.idLot);
    }
    @Override
    public int hashCode() { return Objects.hash(nifProveidor, idLot); }
}
