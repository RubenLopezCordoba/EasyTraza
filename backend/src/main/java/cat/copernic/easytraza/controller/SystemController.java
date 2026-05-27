package cat.copernic.easytraza.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST per gestionar operacions relacionades amb SystemController.
 */
@RestController
@RequestMapping("/system")
/**
 * Controlador REST per gestionar operacions relacionades amb SystemController.
 */
public class SystemController {
    private static final Logger log = LoggerFactory.getLogger(SystemController.class);
@GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        return ResponseEntity.ok(Map.of("status", "ok", "mensaje", "pong"));
    }
}
