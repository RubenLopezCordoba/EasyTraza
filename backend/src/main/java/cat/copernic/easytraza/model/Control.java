package cat.copernic.easytraza.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "controls")
/**
 * Registre de control de pH de l'aigua.
 */
public class Control {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false)
    private Double ph;

    @ManyToOne
    @JoinColumn(name = "usuari_id", nullable = false)
    private Usuari usuari;

    @Column(length = 500)
    private String observacions;

    public Control() {}

    public Control(LocalDate data, Double ph, Usuari usuari) {
        this.data = data;
        this.ph = ph;
        this.usuari = usuari;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public Double getPh() { return ph; }
    public void setPh(Double ph) { this.ph = ph; }

    public Usuari getUsuari() { return usuari; }
    public void setUsuari(Usuari usuari) { this.usuari = usuari; }

    public String getObservacions() { return observacions; }
    public void setObservacions(String observacions) { this.observacions = observacions; }
}