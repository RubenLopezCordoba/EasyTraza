package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.model.*;
import cat.copernic.easytraza.repository.*;
import cat.copernic.easytraza.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST per gestionar operacions relacionades amb LotController.
 */
@RestController
@RequestMapping("/api/lots")
/**
 * Controlador REST per gestionar operacions relacionades amb LotController.
 */
public class LotController {

    private static final Logger log = LoggerFactory.getLogger(LotController.class);

    @Autowired private LotRepository lotRepository;
    @Autowired private AlbaraRepository albaraRepository;
    @Autowired private LiniarProveidorRepository liniarProveidorRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private LiniarClientLotRepository liniarClientLotRepository;
    @Autowired private cat.copernic.easytraza.repository.AlbarraClientRepository albaraClientRepository;

    @GetMapping
    public List<Map<String, Object>> getAll() {
        List<Lot> lots = lotRepository.findAll();
        return lots.stream().map(lot -> {
            Map<String, Object> map = new HashMap<>();
            map.put("nifProveidor", lot.getNifProveidor());
            map.put("idLot", lot.getIdLot());
            map.put("numLot", lot.getNumLot());
            map.put("estat", lot.getEstat());
            map.put("catalogo", lot.getMateriaPrimera() != null ? Map.of("id", lot.getMateriaPrimera().getId(), "nombre", lot.getMateriaPrimera().getNombre()) : null);
            map.put("quantitat", lot.getQuantitat());
            map.put("unitat", lot.getUnitat());
            map.put("dataCaducitat", lot.getDataCaducitat());
            map.put("dataRecepcio", lot.getDataRecepcio());
            map.put("dataObertura", lot.getDataObertura());
            map.put("dataAcabament", lot.getDataAcabament());
            // Get first associated albaran
            List<LiniarProveidor> linies = liniarProveidorRepository.findByNifProveidorAndIdLot(lot.getNifProveidor(), lot.getIdLot());
            if (!linies.isEmpty()) {
                AlbarraProveidor a = linies.get(0).getAlbara();
                map.put("albara", Map.of("numAlbara", a.getIdAlbarra(), "nifProveidor", a.getNifProveidor(), "data", a.getDataRecepcio() != null ? a.getDataRecepcio().toString() : ""));
            } else {
                map.put("albara", null);
            }
            // Get associated client albaran
            List<LiniarClientLot> clientLinies = liniarClientLotRepository.findByLotNifProveidorAndLotIdLot(lot.getNifProveidor(), lot.getIdLot());
            map.put("teProduccio", !clientLinies.isEmpty());
            return map;
        }).toList();
    }

    @GetMapping("/{nifProveidor}")
    /**
     * Obté dades relacionades amb ById.
     */
    public ResponseEntity<Lot> getById(@PathVariable String nifProveidor, @RequestParam String idLot) {
        return lotRepository.findById(new LotId(nifProveidor, idLot))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/albaran/{nifProveidor}")
    /**
     * Obté dades relacionades amb ByAlbara.
     */
    public List<Lot> getByAlbara(@PathVariable String nifProveidor, @RequestParam String idAlbarra) {
        return liniarProveidorRepository.findByNifProveidorAndIdAlbarra(nifProveidor, idAlbarra).stream()
                .map(LiniarProveidor::getLot).toList();
    }

    @GetMapping("/estat/{estat}")
    /**
     * Obté dades relacionades amb ByEstat.
     */
    public List<Lot> getByEstat(@PathVariable String estat) { 
        return lotRepository.findByEstat(estat); 
    }

    @GetMapping("/oberts")
    /**
     * Obté dades relacionades amb Oberts.
     */
    public ResponseEntity<List<Lot>> getOberts() { 
        return ResponseEntity.ok(lotRepository.findLotsOberts()); 
    }

    @PutMapping("/{nifProveidor}/iniciar")
    public ResponseEntity<Map<String, Object>> iniciarLot(@PathVariable String nifProveidor, @RequestParam String idLot) {
        Map<String, Object> r = new HashMap<>();
        Lot lot = lotRepository.findById(new LotId(nifProveidor, idLot)).orElse(null);
        if (lot == null) { 
            r.put("success", false); 
            r.put("message", "Lote no encontrado"); 
            return ResponseEntity.badRequest().body(r); 
        }
        if ("OBERT".equals(lot.getEstat())) { 
            r.put("success", false); 
            r.put("message", "El lote ya está abierto"); 
            return ResponseEntity.badRequest().body(r); 
        }
        lotRepository.findByMateriaPrimeraIdAndEstat(lot.getMateriaPrimera().getId(), "OBERT").forEach(obert -> {
            obert.setEstat("ACABAT"); 
            obert.setDataAcabament(java.time.LocalDateTime.now()); 
            lotRepository.save(obert);
        });
        lot.setEstat("OBERT"); 
        lot.setDataObertura(java.time.LocalDateTime.now()); 
        lotRepository.save(lot);
        r.put("success", true); 
        r.put("message", "Lote iniciado correctamente"); 
        return ResponseEntity.ok(r);
    }

    @PutMapping("/{nifProveidor}/finalitzar")
    public ResponseEntity<Map<String, Object>> finalitzarLot(@PathVariable String nifProveidor, @RequestParam String idLot) {
        Map<String, Object> r = new HashMap<>();
        Lot lot = lotRepository.findById(new LotId(nifProveidor, idLot)).orElse(null);
        if (lot == null) { 
            r.put("success", false); 
            r.put("message", "Lote no encontrado"); 
            return ResponseEntity.badRequest().body(r); 
        }
        if (!"OBERT".equals(lot.getEstat())) { 
            r.put("success", false); 
            r.put("message", "El lote no está abierto"); 
            return ResponseEntity.badRequest().body(r); 
        }
        lot.setEstat("ACABAT"); 
        lot.setDataAcabament(java.time.LocalDateTime.now()); 
        lotRepository.save(lot);
        r.put("success", true); 
        r.put("message", "Lote finalizado correctamente"); 
        return ResponseEntity.ok(r);
    }

    @DeleteMapping("/{nifProveidor}")
    /**
     * Executa l'operació deleteLot.
     */
    public ResponseEntity<?> deleteLot(@PathVariable String nifProveidor, @RequestParam String idLot) {
        try {
            log.info("=== ELIMINANDO LOTE ===");
            log.info("NIF: {}", nifProveidor);
            log.info("ID Lot: {}", idLot);
            
            // 1. Verificar estado del lote
            String checkSql = "SELECT estat FROM lots_proveidor WHERE proveedor_nif = ? AND id_lot = ?";
            String estat;
            try {
                estat = jdbcTemplate.queryForObject(checkSql, String.class, nifProveidor, idLot);
            } catch (Exception e) {
                return ResponseUtil.error("Lot no trobat");
            }
            
            if (!"EN_ESTOC".equals(estat)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Només es poden eliminar lots en estoc (EN_ESTOC)",
                    "estatActual", estat
                ));
            }
            
            // 2. Obtener información del albarán antes de eliminar
            String getAlbaraSql = "SELECT proveedor_nif, id_albara FROM linies_proveidor WHERE proveedor_nif = ? AND id_lot = ? LIMIT 1";
            String nifAlbara = null;
            String idAlbara = null;
            
            try {
                Map<String, Object> result = jdbcTemplate.queryForMap(getAlbaraSql, nifProveidor, idLot);
                nifAlbara = (String) result.get("proveedor_nif");
                idAlbara = (String) result.get("id_albara");
                log.info("Albaran asociado: {} - {}", nifAlbara, idAlbara);
            } catch (Exception e) {
                log.info("Lote sin albaran asociado");
            }
            
            // 3. Eliminar las líneas directamente con SQL
            String deleteLiniesSql = "DELETE FROM linies_proveidor WHERE proveedor_nif = ? AND id_lot = ?";
            int liniesEliminades = jdbcTemplate.update(deleteLiniesSql, nifProveidor, idLot);
            log.info("Lineas eliminadas: {}", liniesEliminades);
            
            // 4. Eliminar albarán si se quedó vacío
            boolean albaranEliminado = false;
            if (nifAlbara != null && idAlbara != null) {
                String countSql = "SELECT COUNT(*) FROM linies_proveidor WHERE proveedor_nif = ? AND id_albara = ?";
                Integer count = jdbcTemplate.queryForObject(countSql, Integer.class, nifAlbara, idAlbara);
                
                log.info("Lineas restantes en albarán: {}", count);
                
                if (count != null && count == 0) {
                    String deleteAlbaraSql = "DELETE FROM albars_proveidor WHERE proveedor_nif = ? AND id_albara = ?";
                    int deleted = jdbcTemplate.update(deleteAlbaraSql, nifAlbara, idAlbara);
                    albaranEliminado = deleted > 0;
                    if (albaranEliminado) {
                        log.info("Albaran eliminado");
                    }
                }
            }
            
            // 5. Eliminar el lote
            String deleteLotSql = "DELETE FROM lots_proveidor WHERE proveedor_nif = ? AND id_lot = ?";
            int lotsEliminats = jdbcTemplate.update(deleteLotSql, nifProveidor, idLot);
            
            if (lotsEliminats == 0) {
                return ResponseUtil.error("No s'ha pogut eliminar el lot");
            }
            
            log.info("Lote eliminado correctamente");
            return ResponseEntity.ok(Map.of(
                "message", "Lot eliminat correctament",
                "liniesEliminades", liniesEliminades,
                "albaranEliminado", albaranEliminado
            ));
            
        } catch (Exception e) {
            log.error("ERROR: {}", e.getMessage(), e);
log.error("Error", e);
return ResponseUtil.error(500, "S'ha produït un error inesperat");
        }
    }

    @PutMapping("/{nifProveidor}/estat")
    /**
     * Executa l'operació updateEstat.
     */
    public ResponseEntity<?> updateEstat(@PathVariable String nifProveidor, @RequestParam String idLot, @RequestBody Map<String, String> body) {
        return lotRepository.findById(new LotId(nifProveidor, idLot)).map(lot -> {
            lot.setEstat(body.get("estat")); 
            lotRepository.save(lot);
            return ResponseEntity.ok(Map.of("message", "Estat actualitzat"));
        }).orElse(ResponseEntity.notFound().build());
    }

    private void recalcularLotsClientActius() {
        try {
            List<AlbarraClient> actius = albaraClientRepository.findByEstat("NO_LLIURAT");
            for (AlbarraClient alb : actius) {
                List<Lot> lotsActuals = lotRepository.findLotsObertsPerTraçabilitat(
                        alb.getDataProduccio().atStartOfDay(),
                        alb.getDataLliurament() != null ? alb.getDataLliurament() : java.time.LocalDateTime.now()
                );
                liniarClientLotRepository.deleteByAlbaraNifClientAndAlbaraDataProduccio(alb.getNifClient(), alb.getDataProduccio());
                for (Lot lot : lotsActuals) {
                    liniarClientLotRepository.save(new LiniarClientLot(alb, lot));
                }
            }
        } catch (Exception e) {
            log.warn("Error recalculant lots d'albarans actius: {}", e.getMessage());
        }
    }

    @GetMapping("/{nifProveidor}/{idLot}/produccio")
    /**
     * Obté dades relacionades amb Produccio.
     */
    public ResponseEntity<?> getProduccio(@PathVariable String nifProveidor, @PathVariable String idLot) {
        List<LiniarClientLot> assoc = liniarClientLotRepository.findByLotNifProveidorAndLotIdLot(nifProveidor, idLot);
        if (assoc.isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (LiniarClientLot lcl : assoc) {
            AlbarraClient alb = lcl.getAlbara();
            if (alb == null || alb.getLinies() == null) continue;
            String clientNom = alb.getNomClient();
            if (clientNom == null && alb.getClient() != null) {
                clientNom = alb.getClient().getNom() + " " + alb.getClient().getCognoms();
            }
            String nifClient = alb.getNifClient();
            String data = alb.getDataProduccio() != null ? alb.getDataProduccio().toString() : "";
            for (LiniarClient linia : alb.getLinies()) {
                Map<String, Object> row = new HashMap<>();
                row.put("producte", linia.getProducte() != null ? linia.getProducte().getNom() : "-");
                row.put("quantitat", linia.getQuantitat());
                row.put("client", clientNom);
                row.put("nifClient", nifClient);
                row.put("dataAlbara", data);
                result.add(row);
            }
        }
        return ResponseEntity.ok(result);
    }
}