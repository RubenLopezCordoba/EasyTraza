package cat.copernic.easytraza.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clau composta per a AlbarraProveidor (nifProveidor + idAlbarra).
 */
public class AlbarraProveidorId implements Serializable {
    private String nifProveidor;
    private String idAlbarra;

    public AlbarraProveidorId() {}
    public AlbarraProveidorId(String nifProveidor, String idAlbarra) {
        this.nifProveidor = nifProveidor;
        this.idAlbarra = idAlbarra;
    }

    public String getNifProveidor() { return nifProveidor; }
    public void setNifProveidor(String nifProveidor) { this.nifProveidor = nifProveidor; }
    public String getIdAlbarra() { return idAlbarra; }
    public void setIdAlbarra(String idAlbarra) { this.idAlbarra = idAlbarra; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlbarraProveidorId)) return false;
        AlbarraProveidorId that = (AlbarraProveidorId) o;
        return Objects.equals(nifProveidor, that.nifProveidor) &&
               Objects.equals(idAlbarra, that.idAlbarra);
    }
    @Override
    public int hashCode() { return Objects.hash(nifProveidor, idAlbarra); }
}
