package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST per gestionar operacions relacionades amb PanelController.
 */
@RestController
@RequestMapping("/api/panel")
/**
 * Controlador REST per gestionar operacions relacionades amb PanelController.
 */
public class PanelController {
    private static final Logger log = LoggerFactory.getLogger(PanelController.class);
@Autowired private ProvedorRepository provedorRepository;
    @Autowired private ProducteRepository producteRepository;
    @Autowired private MateriaPrimeraRepository materiaPrimeraRepository;
    @Autowired private AlbaraRepository albaraRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private AlbarraClientRepository albarraClientRepository;
    @Autowired private LotRepository lotRepository;
    @Autowired private UsuariRepository usuariRepository;
    @Autowired private ControlRepository controlRepository;

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        var clients = clientRepository.findAll();
        stats.put("activeClients", clients.stream().filter(c -> Boolean.TRUE.equals(c.getActivo())).count());
        stats.put("inactiveClients", clients.stream().filter(c -> !Boolean.TRUE.equals(c.getActivo())).count());
        stats.put("totalSuppliers", provedorRepository.count());
        stats.put("totalProducts", producteRepository.count());
        stats.put("totalIngredients", materiaPrimeraRepository.count());
        stats.put("totalAlbaransProveidor", albaraRepository.count());
        stats.put("totalAlbaransClient", albarraClientRepository.count());
        stats.put("totalLots", lotRepository.count());
        stats.put("lotsEnEstoc", lotRepository.findByEstat("EN_ESTOC").size());
        var latestControls = controlRepository.findAllByOrderByDataDesc();
        stats.put("latestPh", latestControls.isEmpty() ? null : latestControls.get(0).getPh());
        stats.put("totalUsers", (long) usuariRepository.findByActivoTrue().size());
        stats.put("phWeekCompleted", !controlRepository.findAllByOrderByDataDesc().isEmpty());
        return ResponseEntity.ok(stats);
    }
}
