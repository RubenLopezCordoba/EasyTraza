package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.model.Usuari;
import cat.copernic.easytraza.service.UsuariService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
/**
 * Controlador REST per gestionar operacions relacionades amb LoginController.
 */
public class LoginController {
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
@Autowired
    private UsuariService usuariService;

    @GetMapping("/login")
    /**
     * Executa l'operació showLoginPage.
     */
    public String showLoginPage(Model model) {
        List<Usuari> usuarisActius = usuariService.findUsuariosActivos();
        model.addAttribute("usuarios", usuarisActius);
        return "login";
    }

    @GetMapping("/login/api/usuarios")
    @ResponseBody
    /**
     * Obté dades relacionades amb UsuariosActivos.
     */
    public List<Usuari> getUsuariosActivos() {
        return usuariService.findUsuariosActivos();
    }

    @PostMapping("/login/authenticate")
    @ResponseBody
    /**
     * Executa l'operació authenticate.
     */
    public ResponseEntity<?> authenticate(@RequestBody Map<String, Object> credentials, HttpSession session) {
        try {
            Long id = Long.valueOf(credentials.get("id").toString());
            String password = credentials.get("password").toString();

            Optional<Usuari> usuariOpt = usuariService.findById(id);

            if (usuariOpt.isEmpty() || !Boolean.TRUE.equals(usuariOpt.get().getActivo())) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("error", "Usuari no trobat o desactivat");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
            }

            if (usuariOpt.isPresent()) {
                Usuari usuari = usuariOpt.get();
                Optional<Usuari> auth = usuariService.authenticate(usuari.getEmail(), password);

                if (auth.isPresent()) {
                    session.setAttribute("usuario", usuari);
                    usuariService.updateUltimoAcceso(usuari.getId());

                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("usuario", usuari);

                    boolean necesitaCambiarPassword = usuariService.necesitaCambiarPassword(usuari.getId());
                    response.put("necesitaCambiarPassword", necesitaCambiarPassword);

                    return ResponseEntity.ok(response);
                }
            }

            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Contraseña incorrecta");
            return ResponseEntity.badRequest().body(error);

        } catch (Exception e) {
            log.error("Error al autenticar", e);
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("error", "Error al autenticar");
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/login/api/cambiar-password")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cambiarPasswordDesdeLogin(@RequestBody Map<String, Object> payload, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long usuarioId = Long.valueOf(payload.get("usuarioId").toString());
            String nuevaPassword = payload.get("nuevaPassword").toString();
            String confirmarPassword = payload.get("confirmarPassword").toString();
            
            Usuari usuariSession = (Usuari) session.getAttribute("usuario");
            if (usuariSession == null || !usuariSession.getId().equals(usuarioId)) {
                response.put("success", false);
                response.put("error", "Sesión inválida");
                return ResponseEntity.status(401).body(response);
            }
            
            if (!nuevaPassword.equals(confirmarPassword)) {
                response.put("success", false);
                response.put("error", "Las contraseñas no coinciden");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (nuevaPassword.length() < 6) {
                response.put("success", false);
                response.put("error", "La contraseña debe tener al menos 6 caracteres");
                return ResponseEntity.badRequest().body(response);
            }
            
            usuariService.cambiarPassword(usuarioId, nuevaPassword);
            
            Optional<Usuari> usuariActualitzat = usuariService.findById(usuarioId);
            usuariActualitzat.ifPresent(u -> session.setAttribute("usuario", u));
            
            response.put("success", true);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error al canviar contrasenya", e);
            response.put("success", false);
            response.put("error", "Error al canviar la contrasenya");
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/login/api/necesita-cambiar-password")
    @ResponseBody
    public ResponseEntity<Map<String, Boolean>> necesitaCambiarPassword(@RequestParam Long usuarioId) {
        Map<String, Boolean> response = new HashMap<>();
        response.put("necesitaCambiar", usuariService.necesitaCambiarPassword(usuarioId));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/logout")
    /**
     * Executa l'operació logout.
     */
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    @GetMapping("/panel")
    /**
     * Executa l'operació panel.
     */
    public String panel(HttpSession session) {
        if (session.getAttribute("usuario") == null) {
            return "redirect:/login";
        }
        return "panel";
    }
}