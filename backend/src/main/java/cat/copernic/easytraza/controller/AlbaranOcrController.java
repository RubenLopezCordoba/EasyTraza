package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.model.AlbarraProveidor;
import cat.copernic.easytraza.service.AlbaranOcrService;
import cat.copernic.easytraza.service.AlbaranParsedDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST per gestionar operacions relacionades amb
 * AlbaranOcrController.
 */
@RestController
@RequestMapping("/api/ocr")
/**
 * Controlador REST per gestionar operacions relacionades amb
 * AlbaranOcrController.
 */
public class AlbaranOcrController {
    private static final Logger log = LoggerFactory.getLogger(AlbaranOcrController.class);
@Autowired
    private AlbaranOcrService albaranOcrService;

    @PostMapping("/analizar")
    /**
     * Executa l'operació analizarAlbaran.
     */
    public ResponseEntity<?> analizarAlbaran(
            @RequestParam("imagen") MultipartFile imagen) {
        if (imagen.isEmpty()) {
            return ResponseEntity.badRequest().body(crearError("La imagen es obligatoria"));
        }

        try {
            AlbaranParsedDTO parsed = albaranOcrService.analizarSinGuardar(imagen);

            Map<String, Object> respuesta = new HashMap<>();
            respuesta.put("success", true);
            respuesta.put("mensaje", "Análisis completado. Revisa los datos.");
            respuesta.put("datos", parsed);

            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("mensaje", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/confirmar")
    /**
     * Executa l'operació confirmarAlbaran.
     */
    public ResponseEntity<?> confirmarAlbaran(
            @RequestBody AlbaranParsedDTO datosConfirmados,
            @RequestParam("usuarioId") Long usuarioId) {
        try {
            AlbarraProveidor albara = albaranOcrService.guardarDesdeConfirmacion(datosConfirmados, usuarioId);

            Map<String, Object> respuesta = new HashMap<>();
            if (albara == null) {
                respuesta.put("success", true);
                respuesta.put("mensaje", "Albarán eliminado automáticamente por no tener lotes");
            } else {
                respuesta.put("success", true);
                respuesta.put("mensaje", "Albarán guardado correctamente");
                respuesta.put("albaran", albara);
            }
            return ResponseEntity.ok(respuesta);

        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("mensaje", "Error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @GetMapping("/test")
    /**
     * Executa l'operació test.
     */
    public ResponseEntity<?> test() {
        Map<String, String> respuesta = new HashMap<>();
        respuesta.put("estado", "OK");
        respuesta.put("mensaje", "Servicio OCR funcionando correctamente");
        return ResponseEntity.ok(respuesta);
    }

    private Map<String, String> crearError(String mensaje) {
        Map<String, String> error = new HashMap<>();
        error.put("error", mensaje);
        return error;
    }
}
