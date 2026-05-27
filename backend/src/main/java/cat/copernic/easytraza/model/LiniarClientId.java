package cat.copernic.easytraza.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class LiniarClientId implements Serializable {
    private String nifClient;
    private LocalDate dataProduccio;
    private Long idProducte;

    public LiniarClientId() {}

    public LiniarClientId(String nifClient, LocalDate dataProduccio, Long idProducte) {
        this.nifClient = nifClient;
        this.dataProduccio = dataProduccio;
        this.idProducte = idProducte;
    }

    public String getNifClient() { return nifClient; }
    public void setNifClient(String nifClient) { this.nifClient = nifClient; }
    
    public LocalDate getDataProduccio() { return dataProduccio; }
    public void setDataProduccio(LocalDate dataProduccio) { this.dataProduccio = dataProduccio; }
    
    public Long getIdProducte() { return idProducte; }
    public void setIdProducte(Long idProducte) { this.idProducte = idProducte; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LiniarClientId)) return false;
        LiniarClientId that = (LiniarClientId) o;
        return Objects.equals(nifClient, that.nifClient) &&
               Objects.equals(dataProduccio, that.dataProduccio) &&
               Objects.equals(idProducte, that.idProducte);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nifClient, dataProduccio, idProducte);
    }
}