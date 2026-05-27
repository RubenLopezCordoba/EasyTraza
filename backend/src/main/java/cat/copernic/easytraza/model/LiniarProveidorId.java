package cat.copernic.easytraza.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clau composta per a LiniarProveidor.
 */
public class LiniarProveidorId implements Serializable {
    private String nifProveidor;
    private String idAlbarra;
    private String idLot;

    public LiniarProveidorId() {}
    public LiniarProveidorId(String nifProveidor, String idAlbarra, String idLot) {
        this.nifProveidor = nifProveidor;
        this.idAlbarra = idAlbarra;
        this.idLot = idLot;
    }

    public String getNifProveidor() { return nifProveidor; }
    public void setNifProveidor(String nifProveidor) { this.nifProveidor = nifProveidor; }
    public String getIdAlbarra() { return idAlbarra; }
    public void setIdAlbarra(String idAlbarra) { this.idAlbarra = idAlbarra; }
    public String getIdLot() { return idLot; }
    public void setIdLot(String idLot) { this.idLot = idLot; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LiniarProveidorId)) return false;
        LiniarProveidorId that = (LiniarProveidorId) o;
        return Objects.equals(nifProveidor, that.nifProveidor) &&
               Objects.equals(idAlbarra, that.idAlbarra) &&
               Objects.equals(idLot, that.idLot);
    }
    @Override
    public int hashCode() { return Objects.hash(nifProveidor, idAlbarra, idLot); }
}
