package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.model.Rol;
import cat.copernic.easytraza.model.Usuari;
import cat.copernic.easytraza.service.UsuariService;
import cat.copernic.easytraza.utils.CifValidator;
import cat.copernic.easytraza.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/usuarios")
/**
 * Controlador REST per gestionar operacions relacionades amb UsuariController.
 */
public class UsuariController {
    private static final Logger log = LoggerFactory.getLogger(UsuariController.class);
@Autowired
    private UsuariService usuariService;

    @Autowired
    private cat.copernic.easytraza.repository.UsuariRepository usuariRepository;

    @GetMapping
    /**
     * Executa l'operació listarUsuaris.
     */
    public String listarUsuaris(Model model, HttpSession session) {
        Usuari usuariActual = (Usuari) session.getAttribute("usuario");
        model.addAttribute("usuarioActual", usuariActual);
        return "usuarios";
    }

    @GetMapping("/api/listar")
    @ResponseBody
    /**
     * Executa l'operació listarApi.
     */
    public List<Usuari> listarApi(HttpSession session) {
        Usuari usuariActual = (Usuari) session.getAttribute("usuario");
        List<Usuari> usuaris = usuariService.findAll();
        if (usuariActual != null && Rol.TRABAJADOR.name().equals(usuariActual.getRol())) {
            return usuaris.stream().filter(u -> u.getId().equals(usuariActual.getId())).toList();
        }
        return usuaris;
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    /**
     * Executa l'operació obtenerUsuari.
     */
    public ResponseEntity<?> obtenerUsuari(@PathVariable Long id, HttpSession session) {
        Usuari usuariActual = (Usuari) session.getAttribute("usuario");
        Optional<Usuari> usuari = usuariService.findById(id);
        if (usuari.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (usuariActual != null && Rol.TRABAJADOR.name().equals(usuariActual.getRol())
            && !usuariActual.getId().equals(id)) {
            return ResponseUtil.forbidden("No tienes permiso para ver este usuario");
        }
        return ResponseEntity.ok(usuari.get());
    }

    @GetMapping("/api/actual")
    @ResponseBody
    /**
     * Obté dades relacionades amb UsuariActual.
     */
    public ResponseEntity<?> getUsuariActual(HttpSession session) {
        Usuari usuari = (Usuari) session.getAttribute("usuario");
        if (usuari == null) {
            return ResponseUtil.unauthorized("No autenticado");
        }
        return ResponseEntity.ok(usuari);
    }

    @PostMapping("/api/guardar")
    @ResponseBody
    /**
     * Executa l'operació guardarUsuari.
     */
    public ResponseEntity<?> guardarUsuari(@RequestBody Usuari usuari, HttpSession session) {
        try {
            Usuari usuariActual = (Usuari) session.getAttribute("usuario");
            if (usuariActual == null || Rol.TRABAJADOR.name().equals(usuariActual.getRol())) {
                return ResponseUtil.forbidden("No tienes permiso para crear usuarios");
            }
            if (usuari.getNombre() == null || usuari.getNombre().trim().isEmpty()) {
                return ResponseUtil.error("El nombre es obligatorio");
            }
            if (usuari.getEmail() == null || usuari.getEmail().trim().isEmpty()) {
                return ResponseUtil.error("El email es obligatorio");
            }
            if (usuari.getNif() == null || usuari.getNif().trim().isEmpty()) {
                return ResponseUtil.error("El NIF es obligatorio");
            }
            if (!CifValidator.isValid(usuari.getNif())) {
                return ResponseUtil.error("El NIF no es válido");
            }
            if (usuari.getPassword() == null || usuari.getPassword().length() < 4) {
                return ResponseUtil.error("La contraseña debe tener al menos 4 caracteres");
            }
            if (!usuari.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseUtil.error("El format del email no és vàlid");
            }
            if (usuariService.existsByEmail(usuari.getEmail())) {
                return ResponseUtil.error("Ya existe un usuario con este email");
            }
            if (usuariService.existsByNif(usuari.getNif())) {
                return ResponseUtil.error("Ya existe un usuario con este NIF");
            }
            if (Rol.ADMIN.name().equals(usuariActual.getRol()) && Rol.SUPER_ADMIN.name().equals(usuari.getRol())) {
                return ResponseUtil.error("No puedes crear un Super Administrador");
            }
            usuari.setActivo(true);
            Usuari guardado = usuariService.save(usuari);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("usuario", guardado);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al guardar usuario", e);
            return ResponseUtil.error(e.getMessage());
        }
    }

    @PutMapping("/api/actualizar/{id}")
    @ResponseBody
    /**
     * Executa l'operació actualizarUsuari.
     */
    public ResponseEntity<?> actualizarUsuari(@PathVariable Long id, @RequestBody Usuari usuari, HttpSession session) {
        try {
            Usuari usuariActual = (Usuari) session.getAttribute("usuario");
            Optional<Usuari> existingOpt = usuariService.findById(id);
            if (existingOpt.isEmpty()) {
                return ResponseUtil.error("Usuario no encontrado");
            }
            Usuari usuariExistente = existingOpt.get();
            boolean esPropio = usuariActual.getId().equals(id);

            if (Rol.TRABAJADOR.name().equals(usuariActual.getRol())) {
                if (!esPropio) {
                    return ResponseUtil.forbidden("No tienes permiso para editar otros usuarios");
                }
                usuari.setRol(usuariExistente.getRol());
            }
            if (Rol.ADMIN.name().equals(usuariActual.getRol())) {
                usuari.setRol(usuariExistente.getRol());
                if (Rol.SUPER_ADMIN.name().equals(usuariExistente.getRol())) {
                    return ResponseUtil.forbidden("No puedes editar un Super Administrador");
                }
            }
            if (Rol.SUPER_ADMIN.name().equals(usuariActual.getRol())) {
                if (Rol.SUPER_ADMIN.name().equals(usuariExistente.getRol()) && !esPropio) {
                    usuari.setRol(usuariExistente.getRol());
                }
            }

            if (!usuari.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                return ResponseUtil.error("El format del email no és vàlid");
            }
            Optional<Usuari> emailExistente = usuariService.findByEmail(usuari.getEmail());
            if (emailExistente.isPresent() && !emailExistente.get().getId().equals(id)) {
                return ResponseUtil.error("Ya existe otro usuario con este email");
            }
            if (usuari.getNif() == null || usuari.getNif().trim().isEmpty()) {
                return ResponseUtil.error("El NIF es obligatorio");
            }
            if (!CifValidator.isValid(usuari.getNif())) {
                return ResponseUtil.error("El NIF no es válido");
            }

            if (usuari.getPassword() == null || usuari.getPassword().trim().isEmpty()) {
                usuari.setPassword(usuariExistente.getPassword());
            } else if (usuari.getPassword().length() < 4) {
                return ResponseUtil.error("La contraseña debe tener al menos 4 caracteres");
            }

            // Preserve fotoUrl and nif if not sent
            if (usuari.getFotoUrl() == null || usuari.getFotoUrl().trim().isEmpty()) {
                usuari.setFotoUrl(usuariExistente.getFotoUrl());
            }

            Optional<Usuari> nifExistente = Optional.empty();
            try { nifExistente = usuariService.findByNif(usuari.getNif()); } catch (Exception ignored) {}
            if (nifExistente.isPresent() && !nifExistente.get().getId().equals(id)) {
                return ResponseUtil.error("Ya existe otro usuario con este NIF");
            }

            if (usuari.getNif() == null || usuari.getNif().trim().isEmpty()) {
                usuari.setNif(usuariExistente.getNif());
            }

            usuari.setId(id);
            Usuari actualizado = usuariService.save(usuari);
            if (esPropio) {
                session.setAttribute("usuario", actualizado);
            }
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("usuario", actualizado);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al actualizar usuario", e);
            return ResponseUtil.error(e.getMessage());
        }
    }

    @PostMapping("/api/subir-foto")
    @ResponseBody
    /**
     * Executa l'operació subirFoto.
     */
    public ResponseEntity<?> subirFoto(@RequestParam("foto") MultipartFile foto, HttpSession session) {
        try {
            Usuari usuariActual = (Usuari) session.getAttribute("usuario");
            if (usuariActual == null) {
                return ResponseUtil.unauthorized("No autenticado");
            }
            if (foto.isEmpty()) {
                return ResponseUtil.error("Debes seleccionar una imagen");
            }
            String contentType = foto.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseUtil.error("El archivo debe ser una imagen");
            }
            String uploadDir = "uploads/perfiles";
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            String originalFilename = Objects.requireNonNull(foto.getOriginalFilename());
            String extension = originalFilename.contains(".") ? originalFilename.substring(originalFilename.lastIndexOf('.')) : ".jpg";
            String nombreArchivo = UUID.randomUUID().toString() + extension;
            Path filePath = uploadPath.resolve(nombreArchivo);
            Files.copy(foto.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String fotoUrl = "/uploads/perfiles/" + nombreArchivo;

            if (usuariActual.getFotoUrl() != null) {
                String oldPath = usuariActual.getFotoUrl().replace("/uploads/", "uploads/");
                Path oldFile = Paths.get(oldPath);
                try {
                    Files.deleteIfExists(oldFile);
                } catch (IOException ignore) {
                }
            }

            Usuari fresh = usuariRepository.findById(usuariActual.getId()).orElse(usuariActual);
            fresh.setFotoUrl(fotoUrl);
            usuariRepository.save(fresh);
            session.setAttribute("usuario", fresh);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("fotoUrl", fotoUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
log.error("Error al subir la foto", e);
return ResponseUtil.error("Error al subir la foto");
        }
    }

    @DeleteMapping("/api/eliminar/{id}")
    @ResponseBody
    /**
     * Executa l'operació eliminarUsuari.
     */
    public ResponseEntity<?> eliminarUsuari(@PathVariable Long id, HttpSession session) {
        try {
            Usuari usuariActual = (Usuari) session.getAttribute("usuario");
            Optional<Usuari> usuariOpt = usuariService.findById(id);
            if (usuariOpt.isEmpty()) {
                return ResponseUtil.error("Usuario no encontrado");
            }
            if (usuariActual.getId().equals(id)) {
                return ResponseUtil.error("No puedes eliminar tu propio usuario");
            }
            if (Rol.TRABAJADOR.name().equals(usuariActual.getRol())) {
                return ResponseUtil.forbidden("No tienes permiso para eliminar usuarios");
            }
            if (Rol.ADMIN.name().equals(usuariActual.getRol()) && Rol.SUPER_ADMIN.name().equals(usuariOpt.get().getRol())) {
                return ResponseUtil.error("No puedes eliminar un Super Administrador");
            }
            usuariService.deleteById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario eliminado correctamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al eliminar usuario", e);
            String mensaje = e.getMessage();
            if (mensaje != null && mensaje.contains("FKnrte60rgyyf9o78cr43kdk632")) {
                return ResponseUtil.error("No es pot eliminar l'usuari: té albarans associats");
            }
            return ResponseUtil.error(mensaje != null ? mensaje : "Error desconegut");
        }
    }

    @PutMapping("/api/toggle-activo/{id}")
    @ResponseBody
    /**
     * Executa l'operació toggleActivo.
     */
    public ResponseEntity<?> toggleActivo(@PathVariable Long id, HttpSession session) {
        try {
            Usuari usuariActual = (Usuari) session.getAttribute("usuario");
            if (usuariActual == null || Rol.TRABAJADOR.name().equals(usuariActual.getRol())) {
                return ResponseUtil.forbidden("No tienes permiso");
            }
            if (usuariActual.getId().equals(id)) {
                return ResponseUtil.error("No puedes desactivarte a ti mismo");
            }
            Optional<Usuari> opt = usuariService.findById(id);
            if (opt.isEmpty()) {
                return ResponseUtil.error("Usuario no encontrado");
            }
            if (Rol.SUPER_ADMIN.name().equals(opt.get().getRol())) {
                return ResponseUtil.error("No se puede desactivar al Super Administrador");
            }
            Usuari u = opt.get();
            u.setActivo(!Boolean.TRUE.equals(u.getActivo()));
            usuariService.save(u);
            return ResponseEntity.ok(Map.of("success", true, "activo", u.getActivo(), "message",
                u.getActivo() ? "Usuario activado" : "Usuario desactivado"));
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseUtil.error("S'ha produït un error inesperat");
        }
    }
}
