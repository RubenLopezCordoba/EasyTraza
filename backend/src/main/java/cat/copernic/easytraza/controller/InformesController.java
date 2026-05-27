package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.model.*;
import cat.copernic.easytraza.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/informes")
public class InformesController {
    private static final Logger log = LoggerFactory.getLogger(InformesController.class);
    
    @Autowired
    private LiniarClientRepository liniarClientRepository;

    @Autowired
    private ProducteRepository producteRepository;

    @GetMapping("/venuts")
    public ResponseEntity<?> getVendesMensuals(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Long producteId) {

        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<LiniarClient> linies;
        if (producteId != null && producteId > 0) {
            // CORREGIDO: Usar el nuevo nombre del método
        linies = liniarClientRepository.findByDataProduccioBetweenAndIdProducte(start, end, producteId);
            } else {
            linies = liniarClientRepository.findByDataProduccioBetween(start, end);
        }

        // Group by day and sum quantities
        Map<Integer, Double> diaMap = new LinkedHashMap<>();
        Set<Long> productesTrobats = new HashSet<>();
        double totalGeneral = 0;

        for (LiniarClient l : linies) {
            int dia = l.getDataProduccio().getDayOfMonth();
            double q = l.getQuantitat() != null ? l.getQuantitat() : 0;
            diaMap.merge(dia, q, Double::sum);
            totalGeneral += q;
            if (l.getProducte() != null) {
                productesTrobats.add(l.getProducte().getIdProducte());
            }
        }

        // Build response
        List<Map<String, Object>> dies = new ArrayList<>();
        for (int d = 1; d <= end.getDayOfMonth(); d++) {
            Map<String, Object> dia = new HashMap<>();
            dia.put("dia", d);
            dia.put("total", diaMap.getOrDefault(d, 0.0));
            dies.add(dia);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("mes", month);
        result.put("any", year);
        result.put("dies", dies);
        result.put("totalMes", totalGeneral);
        result.put("productesTrobats", productesTrobats.size());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/productes")
    public ResponseEntity<List<Producte>> getProductes() {
        return ResponseEntity.ok(producteRepository.findAll());
    }
}