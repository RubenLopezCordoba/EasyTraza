package cat.copernic.easytraza.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Clau composta per a AlbarraClient (nifClient + dataProduccio).
 */
public class AlbarraClientId implements Serializable {
    private String nifClient;
    private LocalDate dataProduccio;

    public AlbarraClientId() {}
    public AlbarraClientId(String nifClient, LocalDate dataProduccio) {
        this.nifClient = nifClient;
        this.dataProduccio = dataProduccio;
    }

    public String getNifClient() { return nifClient; }
    public void setNifClient(String nifClient) { this.nifClient = nifClient; }
    public LocalDate getDataProduccio() { return dataProduccio; }
    public void setDataProduccio(LocalDate dataProduccio) { this.dataProduccio = dataProduccio; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlbarraClientId)) return false;
        AlbarraClientId that = (AlbarraClientId) o;
        return Objects.equals(nifClient, that.nifClient) &&
               Objects.equals(dataProduccio, that.dataProduccio);
    }
    @Override
    public int hashCode() { return Objects.hash(nifClient, dataProduccio); }
}
