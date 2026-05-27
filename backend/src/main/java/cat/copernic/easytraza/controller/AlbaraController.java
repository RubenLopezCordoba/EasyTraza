package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.model.*;
import cat.copernic.easytraza.repository.*;
import cat.copernic.easytraza.utils.CifValidator;
import cat.copernic.easytraza.utils.ResponseUtil;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST per gestionar operacions relacionades amb AlbaraController.
 */
@RestController
@RequestMapping("/api/albarans")
@CrossOrigin(origins = "*")
/**
 * Controlador REST per gestionar operacions relacionades amb AlbaraController.
 */
public class AlbaraController {
    private static final Logger log = LoggerFactory.getLogger(AlbaraController.class);
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private AlbaraRepository albaraRepository;
    @Autowired
    private LotRepository lotRepository;
    @Autowired
    private LiniarProveidorRepository liniarProveidorRepository;
    @Autowired
    private ProvedorRepository provedorRepository;
    @Autowired
    private UsuariRepository usuariRepository;
    @Autowired
    private MateriaPrimeraRepository materiaPrimeraRepository;

    @GetMapping
    /**
     * Obté dades relacionades amb All.
     */
    public ResponseEntity<List<AlbarraProveidor>> getAll() {
        return ResponseEntity.ok(albaraRepository.findAllWithLinies());
    }

    @GetMapping("/{nifProveidor}")
    /**
     * Obté dades relacionades amb ById.
     */
    public ResponseEntity<AlbarraProveidor> getById(@PathVariable String nifProveidor, @RequestParam String idAlbarra) {
        return albaraRepository.findById(new AlbarraProveidorId(nifProveidor, idAlbarra))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/usuari/{usuariId}")
    /**
     * Obté dades relacionades amb ByUsuari.
     */
    public ResponseEntity<List<AlbarraProveidor>> getByUsuari(@PathVariable Long usuariId) {
        return usuariRepository.findById(usuariId)
                .map(u -> ResponseEntity.ok(albaraRepository.findByUsuari(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/with-lots")
    /**
     * Executa l'operació createWithLots.
     */
    public ResponseEntity<?> createWithLots(@RequestBody AlbaraWithLotsRequest request, HttpSession session) {
        try {
            Usuari currentUser = (Usuari) session.getAttribute("usuario");
            if (currentUser == null) {
                return ResponseUtil.unauthorized("No autenticat");
            }
            if (!CifValidator.isValid(request.getProvedorNif())) {
                return ResponseUtil.error(CifValidator.getErrorMessage());
            }
            Provedor provedor = provedorRepository.findByNif(request.getProvedorNif())
                    .orElseGet(() -> {
                        Provedor p = new Provedor();
                        p.setNif(request.getProvedorNif().toUpperCase().trim());
                        p.setNombre(request.getProveedorNombre() != null ? request.getProveedorNombre().trim() : "PROVEEDOR_" + request.getProvedorNif());
                        p.setDireccion("");
                        p.setTelefono(null);
                        return provedorRepository.save(p);
                    });
            if (albaraRepository.existsByIdAlbarra(request.getIdAlbarra())) {
                return ResponseUtil.error("Ya existe un albarán con este ID");
            }

            if (request.getLots() != null && !request.getLots().isEmpty()) {
                for (LotRequest lotReq : request.getLots()) {
                    if (lotReq.getMateriaPrimeraId() == null || lotReq.getMateriaPrimeraId() <= 0) {
                        return ResponseUtil.error("Has de seleccionar una matèria primera del dropdown per cada lot.");
                    }
                    Optional<Lot> existingLot = Optional.empty();
                    try {
                        existingLot = lotRepository.findById(new LotId(provedor.getNif(), lotReq.getIdLot()));
                    } catch (Exception ignored) {
                    }
                    if (existingLot.isPresent()) {
                        Lot existing = existingLot.get();
                        if (!existing.getMateriaPrimera().getId().equals(lotReq.getMateriaPrimeraId())) {
                            return ResponseUtil.error("Aquest lot ja existeix per a aquest proveïdor amb producte diferent: "
                                + existing.getMateriaPrimera().getNombre()
                                + ". No es pot canviar el producte d'un lot existent.");
                        }
                        String reqUnitat = lotReq.getUnitat() != null ? lotReq.getUnitat() : existing.getUnitat();
                        if (!existing.getUnitat().equals(reqUnitat)) {
                            return ResponseUtil.error("El lot " + existing.getIdLot() + " ja existeix amb unitat " 
                                + existing.getUnitat() + ". No es pot canviar a " + reqUnitat);
                        }
                    }
                }
            }

            LocalDateTime dataRecepcio;
            if (request.getDataRecepcio() != null && !request.getDataRecepcio().isEmpty()) {
                dataRecepcio = LocalDateTime.parse(request.getDataRecepcio() + "T00:00:00");
            } else {
                dataRecepcio = LocalDateTime.now();
            }
            AlbarraProveidor albara = new AlbarraProveidor(request.getIdAlbarra(), dataRecepcio, provedor, currentUser);
            albara.setObservacions(request.getObservacions());
            AlbarraProveidor savedAlbara = albaraRepository.save(albara);

            if (request.getLots() != null) {
                for (LotRequest lotReq : request.getLots()) {
                    if (lotReq.getMateriaPrimeraId() == null || lotReq.getMateriaPrimeraId() <= 0) {
                        return ResponseUtil.error("Has de seleccionar una matèria primera del dropdown per cada lot.");
                    }
                    Optional<Lot> existingLot = Optional.empty();
                    try {
                        existingLot = lotRepository.findById(new LotId(provedor.getNif(), lotReq.getIdLot()));
                    } catch (Exception ignored) {
                    }
                    Lot lot;
                    if (existingLot.isPresent()) {
                        Lot existing = existingLot.get();
                        if (!existing.getMateriaPrimera().getId().equals(lotReq.getMateriaPrimeraId())) {
                            return ResponseUtil.error("Aquest lot ja existeix per a aquest proveïdor amb producte diferent: "
                                + existing.getMateriaPrimera().getNombre()
                                + ". No es pot canviar el producte d'un lot existent.");
                        }
                        String reqUnitat = lotReq.getUnitat() != null ? lotReq.getUnitat() : existing.getUnitat();
                        if (!existing.getUnitat().equals(reqUnitat)) {
                            return ResponseUtil.error("El lot " + existing.getIdLot() + " ja existeix amb unitat " 
                                + existing.getUnitat() + ". No es pot canviar a " + reqUnitat);
                        }
                        lot = existing;
                        lot.setQuantitat(existing.getQuantitat() + (lotReq.getQuantitat() != null ? lotReq.getQuantitat() : 0));
                        lot = lotRepository.saveAndFlush(lot);
                    } else {
                        MateriaPrimera mp = materiaPrimeraRepository.findById(lotReq.getMateriaPrimeraId())
                                .orElseThrow(() -> new RuntimeException("Matèria primera no trobada."));
                        lot = new Lot(lotReq.getIdLot(), provedor, mp);
                        lot.setQuantitat(lotReq.getQuantitat() != null ? lotReq.getQuantitat() : 0);
                        lot.setUnitat(lotReq.getUnitat() != null ? lotReq.getUnitat() : "kg");
                        if (lotReq.getDataCaducitat() != null)
                        try {
                            lot.setDataCaducitat(LocalDate.parse(lotReq.getDataCaducitat()).atStartOfDay());
                        } catch (Exception e) {
                        }
                        if (lotReq.getLotProveidor() != null) {
                            lot.setLotProveidor(lotReq.getLotProveidor());
                        }
                        lot = lotRepository.saveAndFlush(lot);
                    }
                    albara.getLinies().add(new LiniarProveidor(albara, lot,
                            lotReq.getQuantitat() != null ? lotReq.getQuantitat() : 0,
                            lotReq.getUnitat() != null ? lotReq.getUnitat() : "kg"));
                }
            }
            albaraRepository.save(albara);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("success", true, "message", "Albarán creado correctamente"));
        } catch (Exception e) {
log.error("Error al crear", e);
return ResponseUtil.error("Error al crear");
        }
    }

    @PutMapping("/{nifProveidor}")
    @Transactional
    public ResponseEntity<?> updateAlbaran(@PathVariable String nifProveidor, @RequestParam String idAlbarra, @RequestBody AlbaraWithLotsRequest request) {
        try {
            AlbarraProveidor albara = albaraRepository.findById(new AlbarraProveidorId(nifProveidor, idAlbarra))
                    .orElseThrow(() -> new RuntimeException("Albarà no trobat"));
            if (albara.getLinies() != null && albara.getLinies().stream().anyMatch(l -> l.getLot() != null && ("OBERT".equals(l.getLot().getEstat()) || "ACABAT".equals(l.getLot().getEstat())))) {
                return ResponseUtil.error("No es pot editar: té lots en estat OBERT o ACABAT");
            }

            Provedor provedor = provedorRepository.findByNif(request.getProvedorNif())
                    .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
            albara.setProvedor(provedor);
            albara.setObservacions(request.getObservacions());
            if (request.getDataRecepcio() != null)
                try {
                albara.setDataRecepcio(LocalDate.parse(request.getDataRecepcio()).atStartOfDay());
            } catch (Exception e) {
            }

            if (request.getLots() == null || request.getLots().isEmpty()) {
                albaraRepository.deleteById(new AlbarraProveidorId(nifProveidor, idAlbarra));
                return ResponseUtil.ok("Albarán eliminado porque no tenía lotes", "deleted", true);
            }

            // Remove existing lines via entity manager
            for (LiniarProveidor line : new java.util.ArrayList<>(albara.getLinies())) {
                entityManager.remove(line);
            }
            albara.getLinies().clear();
            entityManager.flush();

            for (LotRequest lotReq : request.getLots()) {
                if (lotReq.getMateriaPrimeraId() == null || lotReq.getMateriaPrimeraId() <= 0) {
                    return ResponseUtil.error("Has de seleccionar una matèria primera del dropdown per cada lot.");
                }
                Optional<Lot> existingLot = Optional.empty();
                try {
                    existingLot = lotRepository.findById(new LotId(provedor.getNif(), lotReq.getIdLot()));
                } catch (Exception ignored) {
                }
                Lot lot;
                if (existingLot.isPresent()) {
                    Lot existing = existingLot.get();
                    if (!existing.getMateriaPrimera().getId().equals(lotReq.getMateriaPrimeraId())) {
                        return ResponseUtil.error("Aquest lot ja existeix per a aquest proveïdor amb producte diferent: "
                            + existing.getMateriaPrimera().getNombre()
                            + ". No es pot canviar el producte d'un lot existent.");
                    }
                    String reqUnitat = lotReq.getUnitat() != null ? lotReq.getUnitat() : existing.getUnitat();
                    if (!existing.getUnitat().equals(reqUnitat)) {
                        return ResponseUtil.error("El lot " + existing.getIdLot() + " ja existeix amb unitat " 
                            + existing.getUnitat() + ". No es pot canviar a " + reqUnitat);
                    }
                    lot = existing;
                    lot.setQuantitat(existing.getQuantitat() + (lotReq.getQuantitat() != null ? lotReq.getQuantitat() : 0));
                    lot = lotRepository.saveAndFlush(lot);
                } else {
                    MateriaPrimera mp = materiaPrimeraRepository.findById(lotReq.getMateriaPrimeraId())
                            .orElseThrow(() -> new RuntimeException("Matèria primera no trobada."));
                    lot = new Lot(lotReq.getIdLot(), provedor, mp);
                    lot.setQuantitat(lotReq.getQuantitat() != null ? lotReq.getQuantitat() : 0);
                    lot.setUnitat(lotReq.getUnitat() != null ? lotReq.getUnitat() : "kg");
                    if (lotReq.getDataCaducitat() != null)
                        try {
                        lot.setDataCaducitat(LocalDate.parse(lotReq.getDataCaducitat()).atStartOfDay());
                    } catch (Exception e) {
                    }
                    if (lotReq.getLotProveidor() != null) {
                        lot.setLotProveidor(lotReq.getLotProveidor());
                    }
                    lot = lotRepository.saveAndFlush(lot);
                }
                albara.getLinies().add(new LiniarProveidor(albara, lot,
                        lotReq.getQuantitat() != null ? lotReq.getQuantitat() : 0,
                        lotReq.getUnitat() != null ? lotReq.getUnitat() : "kg"));
            }
            albaraRepository.save(albara);
            return ResponseUtil.ok("Albarán actualizado correctamente");
        } catch (Exception e) {
            log.error("Error al actualizar albarà", e);
            return ResponseUtil.error("Error al actualizar");
        }
    }

    @Transactional
    @DeleteMapping("/{nifProveidor}")
    /**
     * Executa l'operació delete.
     */
    public ResponseEntity<?> delete(@PathVariable String nifProveidor, @RequestParam String idAlbarra) {
        try {
            AlbarraProveidor albara = albaraRepository.findById(new AlbarraProveidorId(nifProveidor, idAlbarra))
                    .orElseThrow(() -> new RuntimeException("Albarà no trobat"));
            if (albara.getLinies() != null && albara.getLinies().stream().anyMatch(l -> l.getLot() != null)) {
                return ResponseUtil.error("No es pot eliminar l'albarà perquè té lots associats");
            }
            liniarProveidorRepository.deleteByAlbaraId(nifProveidor, idAlbarra);
            albaraRepository.deleteById(new AlbarraProveidorId(nifProveidor, idAlbarra));
            return ResponseEntity.ok(Map.of("message", "Albarán eliminado correctamente"));
        } catch (Exception e) {
            log.error("Error al eliminar albarà", e);
            return ResponseUtil.error(e.getMessage());
        }
    }

    static class LotRequest {

        private String numLot;
        private Long materiaPrimeraId;
        private Integer quantitat;
        private String unitat;
        private String dataCaducitat;
        private String descripcio;
        private String lotProveidor;
        private String lote;
        private Double cantidad;

        /**
         * Obté dades relacionades amb IdLot.
         */
        public String getIdLot() {
            return numLot;
        }

        /**
         * Obté dades relacionades amb NumLot.
         */
        public String getNumLot() {
            return numLot;
        }

        /**
         * Estableix el valor de NumLot.
         */
        public void setNumLot(String v) {
            numLot = v;
        }

        /**
         * Obté dades relacionades amb MateriaPrimeraId.
         */
        public Long getMateriaPrimeraId() {
            return materiaPrimeraId;
        }

        /**
         * Estableix el valor de MateriaPrimeraId.
         */
        public void setMateriaPrimeraId(Long v) {
            materiaPrimeraId = v;
        }

        /**
         * Obté dades relacionades amb Quantitat.
         */
        public Integer getQuantitat() {
            return quantitat;
        }

        /**
         * Estableix el valor de Quantitat.
         */
        public void setQuantitat(Integer v) {
            quantitat = v;
        }

        /**
         * Obté dades relacionades amb Unitat.
         */
        public String getUnitat() {
            return unitat;
        }

        /**
         * Estableix el valor de Unitat.
         */
        public void setUnitat(String v) {
            unitat = v;
        }

        /**
         * Obté dades relacionades amb DataCaducitat.
         */
        public String getDataCaducitat() {
            return dataCaducitat;
        }

        /**
         * Estableix el valor de DataCaducitat.
         */
        public void setDataCaducitat(String v) {
            dataCaducitat = v;
        }

        @com.fasterxml.jackson.annotation.JsonProperty("descripcion")
        /**
         * Obté dades relacionades amb Descripcio.
         */
        public String getDescripcio() {
            return descripcio;
        }

        @com.fasterxml.jackson.annotation.JsonProperty("descripcion")
        /**
         * Estableix el valor de Descripcio.
         */
        public void setDescripcio(String v) {
            descripcio = v;
        }

        /**
         * Obté dades relacionades amb LotProveidor.
         */
        public String getLotProveidor() {
            return lotProveidor;
        }

        /**
         * Estableix el valor de LotProveidor.
         */
        public void setLotProveidor(String v) {
            lotProveidor = v;
        }

        /**
         * Obté dades relacionades amb Lote.
         */
        public String getLote() {
            return lote;
        }

        /**
         * Estableix el valor de Lote.
         */
        public void setLote(String v) {
            lote = v;
        }

        /**
         * Obté dades relacionades amb Cantidad.
         */
        public Double getCantidad() {
            return cantidad;
        }

        /**
         * Estableix el valor de Cantidad.
         */
        public void setCantidad(Double v) {
            cantidad = v;
        }

        @com.fasterxml.jackson.annotation.JsonIgnore
        /**
         * Executa l'operació hasCatalogo.
         */
        public boolean hasCatalogo() {
            return catalogo != null && catalogo.id != null;
        }
        private CatalogoRef catalogo;

        /**
         * Obté dades relacionades amb Catalogo.
         */
        public CatalogoRef getCatalogo() {
            return catalogo;
        }

        /**
         * Estableix el valor de Catalogo.
         */
        public void setCatalogo(CatalogoRef v) {
            catalogo = v;
            if (v != null && v.id != null) {
                materiaPrimeraId = v.id;
            }
        }

        static class CatalogoRef {

            public Long id;
            public String nombre;
        }
    }

    static class AlbaraWithLotsRequest {

        @com.fasterxml.jackson.annotation.JsonProperty("numAlbara")
        private String idAlbarra;
        @com.fasterxml.jackson.annotation.JsonProperty("proveedorCif")
        private String provedorNif;
        private String proveedorNombre;
        @com.fasterxml.jackson.annotation.JsonProperty("usuarioId")
        private Long usuariId;
        private String observacions;
        private List<LotRequest> lots;
        private String dataRecepcio;

        /**
         * Obté dades relacionades amb IdAlbarra.
         */
        public String getIdAlbarra() {
            return idAlbarra;
        }

        @com.fasterxml.jackson.annotation.JsonProperty("numAlbara")
        /**
         * Estableix el valor de IdAlbarra.
         */
        public void setIdAlbarra(String v) {
            idAlbarra = v;
        }

        /**
         * Obté dades relacionades amb ProvedorNif.
         */
        public String getProvedorNif() {
            return provedorNif;
        }

        @com.fasterxml.jackson.annotation.JsonProperty("proveedorCif")
        /**
         * Estableix el valor de ProvedorNif.
         */
        public void setProvedorNif(String v) {
            provedorNif = v;
        }

        /**
         * Obté dades relacionades amb ProveedorNombre.
         */
        public String getProveedorNombre() {
            return proveedorNombre;
        }

        /**
         * Estableix el valor de ProveedorNombre.
         */
        public void setProveedorNombre(String v) {
            proveedorNombre = v;
        }

        /**
         * Obté dades relacionades amb UsuariId.
         */
        public Long getUsuariId() {
            return usuariId;
        }

        @com.fasterxml.jackson.annotation.JsonProperty("usuarioId")
        /**
         * Estableix el valor de UsuariId.
         */
        public void setUsuariId(Long v) {
            usuariId = v;
        }

        /**
         * Obté dades relacionades amb Observacions.
         */
        public String getObservacions() {
            return observacions;
        }

        /**
         * Estableix el valor de Observacions.
         */
        public void setObservacions(String v) {
            observacions = v;
        }

        /**
         * Obté dades relacionades amb Lots.
         */
        public List<LotRequest> getLots() {
            return lots;
        }

        /**
         * Estableix el valor de Lots.
         */
        public void setLots(List<LotRequest> v) {
            lots = v;
        }

        /**
         * Obté dades relacionades amb DataRecepcio.
         */
        public String getDataRecepcio() {
            return dataRecepcio;
        }

        /**
         * Estableix el valor de DataRecepcio.
         */
        public void setDataRecepcio(String v) {
            dataRecepcio = v;
        }
    }
}
