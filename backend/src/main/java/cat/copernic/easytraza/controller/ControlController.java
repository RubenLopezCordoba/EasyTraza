package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.model.Control;
import cat.copernic.easytraza.model.Usuari;
import cat.copernic.easytraza.service.ControlService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST per gestionar operacions relacionades amb ControlController.
 */
@RestController
@RequestMapping("/api/controls")
/**
 * Controlador REST per gestionar operacions relacionades amb ControlController.
 */
public class ControlController {
    private static final Logger log = LoggerFactory.getLogger(ControlController.class);
@Autowired
    private ControlService controlService;

    @GetMapping
    /**
     * Obté dades relacionades amb AllControls.
     */
    public ResponseEntity<List<Control>> getAllControls() {
        return ResponseEntity.ok(controlService.getAllControls());
    }

    @GetMapping("/weekly-status")
    public ResponseEntity<Map<String, Object>> getWeeklyStatus() {
        return ResponseEntity.ok(controlService.getWeeklyStatus());
    }

    @PostMapping
    /**
     * Executa l'operació createControl.
     */
    public ResponseEntity<?> createControl(HttpSession session, @RequestBody Map<String, Object> body) {
        try {
            Usuari user = (Usuari) session.getAttribute("usuario");
            if (user == null) {
                return bad("Usuari no autenticat");
            }
            Double ph = Double.valueOf(body.get("ph").toString());
            String observacions = (String) body.getOrDefault("observacions", "");
            Control created = controlService.createControl(ph, user.getId(), observacions);
            Map<String, Object> r = new HashMap<>();
            r.put("success", true);
            r.put("mensaje", "Control de pH registrat correctament");
            r.put("control", created);
            return ResponseEntity.ok(r);
        } catch (Exception e) {
            return bad(e.getMessage());
        }
    }

    private ResponseEntity<Map<String, Object>> bad(String msg) {
        Map<String, Object> r = new HashMap<>();
        r.put("success", false);
        r.put("mensaje", msg);
        return ResponseEntity.badRequest().body(r);
    }
}
