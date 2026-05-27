package cat.copernic.easytraza.service;

import cat.copernic.easytraza.service.AlbaranParsedDTO.LoteParsed.CatalogoRef;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonIgnoreProperties(ignoreUnknown = true)
/**
 * Servei per gestionar la logica de negoci de AlbaranParsedDTO.
 */
public class AlbaranParsedDTO {
    private static final Logger log = LoggerFactory.getLogger(AlbaranParsedDTO.class);
private String numAlbara;
    private ProveedorParsed proveedor;
    private String fecha;
    private List<LoteParsed> lots;
    private String imagenTemporal;
    private String observacions;
    private Integer quantitat;
private String numLot;
private CatalogoRef catalogo;

    // Constructor vacío
    public AlbaranParsedDTO() {}

    // Getters y Setters
    public String getNumAlbara() { return numAlbara; }
    public void setNumAlbara(String numAlbara) { this.numAlbara = numAlbara; }

    public ProveedorParsed getProveedor() { return proveedor; }
    public void setProveedor(ProveedorParsed proveedor) { this.proveedor = proveedor; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public List<LoteParsed> getLots() { return lots; }
    public void setLots(List<LoteParsed> lots) { this.lots = lots; }

    public String getImagenTemporal() { return imagenTemporal; }
    public void setImagenTemporal(String imagenTemporal) { this.imagenTemporal = imagenTemporal; }

    public String getObservacions() { return observacions; }
    public void setObservacions(String observacions) { this.observacions = observacions; }

    // ========== ProveedorParsed ==========
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProveedorParsed {
        private String cif;
        private String nombre;
        private String direccion;

        public ProveedorParsed() {}

        public String getCif() { return cif; }
        public void setCif(String cif) { this.cif = cif; }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }

        public String getDireccion() { return direccion; }
        public void setDireccion(String direccion) { this.direccion = direccion; }
    }

    // ========== LoteParsed ==========
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class LoteParsed {
        private String codigoArticulo;
        private String descripcion;
        private Double cantidad;
        private String unidad;
        private String lote;
        private String fechaConsumo;

        public LoteParsed() {}

        public String getCodigoArticulo() { return codigoArticulo; }
        public void setCodigoArticulo(String codigoArticulo) { this.codigoArticulo = codigoArticulo; }

        public String getDescripcion() { return descripcion; }
        public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

        public Double getCantidad() { return cantidad; }
        public void setCantidad(Double cantidad) { this.cantidad = cantidad; }

        public String getUnidad() { return unidad; }
        public void setUnidad(String unidad) { this.unidad = unidad; }

        public String getLote() { return lote; }
        public void setLote(String lote) { this.lote = lote; }

        public String getFechaConsumo() { return fechaConsumo; }
        public void setFechaConsumo(String fechaConsumo) { this.fechaConsumo = fechaConsumo; }
        
        private Integer quantitat;
private String numLot;
private CatalogoRef catalogo;

public Integer getQuantitat() { return quantitat; }
public void setQuantitat(Integer quantitat) { this.quantitat = quantitat; }

public String getNumLot() { return numLot; }
public void setNumLot(String numLot) { this.numLot = numLot; }

public CatalogoRef getCatalogo() { return catalogo; }
public void setCatalogo(CatalogoRef catalogo) { this.catalogo = catalogo; }

public static class CatalogoRef {
    private Long id;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
    }
}