package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.model.Provedor;
import cat.copernic.easytraza.model.Usuari;
import cat.copernic.easytraza.service.ProvedorService;
import cat.copernic.easytraza.utils.CifValidator;
import cat.copernic.easytraza.utils.ResponseUtil;
import cat.copernic.easytraza.model.Rol;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST per gestionar operacions relacionades amb ProvedorController.
 */
@RestController
@RequestMapping("/proveedores")
/**
 * Controlador REST per gestionar operacions relacionades amb ProvedorController.
 */
public class ProvedorController {
    private static final Logger log = LoggerFactory.getLogger(ProvedorController.class);
@Autowired
    private ProvedorService provedorService;

    @GetMapping("/api/listar")
    /**
     * Executa l'operació listar.
     */
    public java.util.List<Provedor> listar() {
        return provedorService.findAll();
    }

    @GetMapping("/api/{nif}")
    /**
     * Executa l'operació obtener.
     */
    public ResponseEntity<Provedor> obtener(@PathVariable String nif) {
        return provedorService.findById(nif)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/guardar")
    /**
     * Executa l'operació guardar.
     */
    public ResponseEntity<?> guardar(@RequestBody Provedor provedor, HttpSession session) {
        try {
            Usuari u = (Usuari) session.getAttribute("usuario");
            if (u == null || Rol.TRABAJADOR.name().equals(u.getRol()))
                return ResponseUtil.forbidden("No tens permís");
            if (!CifValidator.isValid(provedor.getNif())) {
                return ResponseUtil.error(CifValidator.getErrorMessage());
            }
            Provedor guardado = provedorService.save(provedor);
            return ResponseEntity.ok(Map.of("success", true, "proveedor", guardado));
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseUtil.error("S'ha produït un error inesperat");
        }
    }

    @PutMapping("/api/actualizar/{nif}")
    /**
     * Executa l'operació actualizar.
     */
    public ResponseEntity<?> actualizar(@PathVariable String nif, @RequestBody Provedor provedor, HttpSession session) {
        try {
            Usuari u = (Usuari) session.getAttribute("usuario");
            if (u == null || Rol.TRABAJADOR.name().equals(u.getRol()))
                return ResponseUtil.forbidden("No tens permís");
            Optional<Provedor> existente = provedorService.findById(nif);
            if (!existente.isPresent()) {
                return ResponseUtil.error("Proveedor no encontrado");
            }
            provedor.setNif(nif);
            Provedor actualizado = provedorService.save(provedor);
            return ResponseEntity.ok(Map.of("success", true, "proveedor", actualizado));
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseUtil.error("S'ha produït un error inesperat");
        }
    }

    @DeleteMapping("/api/eliminar/{nif}")
    /**
     * Executa l'operació eliminar.
     */
    public ResponseEntity<?> eliminar(@PathVariable String nif, HttpSession session) {
        try {
            Usuari u = (Usuari) session.getAttribute("usuario");
            if (u == null || Rol.TRABAJADOR.name().equals(u.getRol()))
                return ResponseUtil.forbidden("No tens permís");
            provedorService.deleteById(nif);
            return ResponseEntity.ok(Map.of("success", true));
        } catch (RuntimeException e) {
            log.error("Error", e);
            return ResponseUtil.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseUtil.error("S'ha produït un error inesperat");
        }
    }
}
