package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.model.Rol;
import cat.copernic.easytraza.model.Usuari;
import cat.copernic.easytraza.service.UsuariService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador REST per gestionar operacions relacionades amb MobileAuthController.
 */
@RestController
@RequestMapping("/api/mobile")
/**
 * Controlador REST per gestionar operacions relacionades amb MobileAuthController.
 */
public class MobileAuthController {
    private static final Logger log = LoggerFactory.getLogger(MobileAuthController.class);
@Autowired
    private UsuariService usuariService;

    @PostMapping("/login")
    /**
     * Executa l'operació login.
     */
    public ResponseEntity<?> login(@RequestBody Map<String, Long> body, HttpSession session) {
        Long usuariId = body.get("usuarioId");
        Optional<Usuari> usuariOpt = usuariService.findById(usuariId);
        if (usuariOpt.isEmpty() || !Boolean.TRUE.equals(usuariOpt.get().getActivo())) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Usuario no válido");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        Usuari usuari = usuariOpt.get();
        session.setAttribute("usuario", usuari);
        usuariService.updateUltimoAcceso(usuari.getId());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("usuari", Map.of(
            "id", usuari.getId(),
            "nombre", usuari.getNombre(),
            "email", usuari.getEmail(),
            "rol", usuari.getRol()
        ));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    /**
     * Executa l'operació logout.
     */
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/usuarios")
    /**
     * Executa l'operació listarUsuarios.
     */
    public ResponseEntity<?> listarUsuarios() {
        List<Usuari> usuaris = usuariService.findUsuariosActivos();
        List<Map<String, Object>> lista = new ArrayList<>();
        for (Usuari u : usuaris) {
            if (Rol.SUPER_ADMIN.name().equals(u.getRol())) continue;
            Map<String, Object> item = new HashMap<>();
            item.put("id", u.getId());
            item.put("nombre", u.getNombre());
            lista.add(item);
        }
        return ResponseEntity.ok(lista);
    }
}
