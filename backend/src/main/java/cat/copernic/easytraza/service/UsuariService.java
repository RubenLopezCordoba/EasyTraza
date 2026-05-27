package cat.copernic.easytraza.service;

import cat.copernic.easytraza.model.PasswordResetToken;
import cat.copernic.easytraza.model.Usuari;
import cat.copernic.easytraza.repository.PasswordResetTokenRepository;
import cat.copernic.easytraza.repository.UsuariRepository;
import cat.copernic.easytraza.utils.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
/**
 * Servei per gestionar la logica de negoci de UsuariService.
 */
public class UsuariService {
    private static final Logger log = LoggerFactory.getLogger(UsuariService.class);
@Autowired
    private UsuariRepository usuariRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public List<Usuari> findAll() {
        return usuariRepository.findAll();
    }

    public Optional<Usuari> findById(Long id) {
        return usuariRepository.findById(id);
    }

    public Optional<Usuari> findByEmail(String email) {
        return usuariRepository.findByEmail(email);
    }

    public List<Usuari> findByRol(String rol) {
        return usuariRepository.findByRol(rol);
    }

    public List<Usuari> findUsuariosActivos() {
        return usuariRepository.findByActivoTrue();
    }

    public Usuari save(Usuari usuari) throws Exception {
        if (usuari.getNombre() == null || usuari.getNombre().trim().isEmpty()) {
            throw new Exception("El nombre es obligatorio");
        }
        if (usuari.getEmail() == null || usuari.getEmail().trim().isEmpty()) {
            throw new Exception("El email es obligatorio");
        }
        if (!usuari.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new Exception("El formato del email no es válido");
        }
        if (usuari.getId() == null) {
            if ("SUPER_ADMIN".equals(usuari.getRol())) {
                throw new Exception("No se puede crear un usuario Super Administrador");
            }
            if (usuari.getPassword() == null || usuari.getPassword().length() < 4) {
                throw new Exception("La contraseña debe tener al menos 4 caracteres");
            }
            if (usuariRepository.existsByEmail(usuari.getEmail())) {
                throw new Exception("Ya existe un usuario con este email");
            }
            usuari.setPassword(passwordEncoder.encode(usuari.getPassword()));
            usuari.setFechaCreacion(LocalDateTime.now());
            usuari.setActivo(true);
            usuari.setPasswordCambiada(false);
        } else {
            Optional<Usuari> existing = usuariRepository.findById(usuari.getId());
            if (existing.isEmpty()) {
                throw new Exception("Usuario no encontrado");
            }
            Usuari usuariExistente = existing.get();
            if ("SUPER_ADMIN".equals(usuariExistente.getRol())) {
                throw new Exception("No se puede modificar el usuario Super Administrador");
            }
            Optional<Usuari> emailExistente = usuariRepository.findByEmail(usuari.getEmail());
            if (emailExistente.isPresent() && !emailExistente.get().getId().equals(usuari.getId())) {
                throw new Exception("Ya existe otro usuario con este email");
            }
            if ("SUPER_ADMIN".equals(usuari.getRol())) {
                throw new Exception("No se puede asignar el rol de Super Administrador");
            }
            String passwordNueva = usuari.getPassword();
            String passwordExistente = usuariExistente.getPassword();
            if (passwordNueva == null || passwordNueva.trim().isEmpty()) {
                usuari.setPassword(passwordExistente);
            } else if (passwordNueva.length() < 4) {
                throw new Exception("La contraseña debe tener al menos 4 caracteres");
            } else if (passwordExistente.equals(passwordNueva)) {
                // Es el mismo password, no recodificar
                usuari.setPassword(passwordExistente);
            } else {
                usuari.setPassword(passwordEncoder.encode(passwordNueva));
            }
            usuari.setPasswordCambiada(usuariExistente.isPasswordCambiada());
            usuari.setFechaCreacion(usuariExistente.getFechaCreacion());
        }
        return usuariRepository.save(usuari);
    }

    public void deleteById(Long id) throws Exception {
        Optional<Usuari> usuari = usuariRepository.findById(id);
        if (usuari.isEmpty()) {
            throw new Exception("Usuario no encontrado");
        }
        if ("SUPER_ADMIN".equals(usuari.get().getRol())) {
            throw new Exception("No se puede eliminar al Super Administrador");
        }
        usuariRepository.deleteById(id);
    }

    public boolean existsByEmail(String email) {
        return usuariRepository.existsByEmail(email);
    }

    public boolean existsByNif(String nif) {
        return usuariRepository.existsByNif(nif);
    }

    public Optional<Usuari> findByNif(String nif) {
        return usuariRepository.findByNif(nif);
    }

    public void crearSuperAdminSiNoExiste() {
        List<Usuari> superAdmins = usuariRepository.findByRol("SUPER_ADMIN");
        if (superAdmins.isEmpty()) {
            Usuari superAdminUser = new Usuari();
            superAdminUser.setNombre("Super Admin");
            superAdminUser.setEmail("superadmin@easytraza.com");
            superAdminUser.setPassword(passwordEncoder.encode("admin123"));
            superAdminUser.setRol("SUPER_ADMIN");
            superAdminUser.setEsAdmin(true);
            superAdminUser.setTelefono("000000000");
            superAdminUser.setActivo(true);
            superAdminUser.setFechaCreacion(LocalDateTime.now());
            superAdminUser.setPasswordCambiada(false);
            usuariRepository.save(superAdminUser);
            log.info("=====================================");
            log.info("SUPER_ADMIN CREADO:");
            log.info("Email: superadmin@easytraza.com");
            log.info("Contraseña: admin123");
            log.info("=====================================");
        }
    }

    public Optional<Usuari> authenticate(String email, String password) {
        Optional<Usuari> usuari = usuariRepository.findByEmail(email);
        if (usuari.isPresent() && passwordEncoder.matches(password, usuari.get().getPassword())) {
            return usuari;
        }
        return Optional.empty();
    }

    public void updateFotoUrl(Long id, String fotoUrl) {
        usuariRepository.updateFotoUrl(id, fotoUrl);
    }

    public void updateUltimoAcceso(Long id) {
        usuariRepository.findById(id).ifPresent(usuari -> {
            usuari.setUltimoAcceso(LocalDateTime.now());
            usuariRepository.save(usuari);
        });
    }

    public boolean necesitaCambiarPassword(Long id) {
        Optional<Usuari> usuari = usuariRepository.findById(id);
        if (usuari.isEmpty()) return false;
        Usuari u = usuari.get();
        return "SUPER_ADMIN".equals(u.getRol()) && !u.isPasswordCambiada();
    }

    public void cambiarPassword(Long id, String nuevaPassword) throws Exception {
        Optional<Usuari> usuariOpt = usuariRepository.findById(id);
        if (usuariOpt.isEmpty()) {
            throw new Exception("Usuario no encontrado");
        }
        if (nuevaPassword == null || nuevaPassword.length() < 6) {
            throw new Exception("La contraseña debe tener al menos 6 caracteres");
        }
        Usuari usuari = usuariOpt.get();
        usuari.setPassword(passwordEncoder.encode(nuevaPassword));
        usuari.setPasswordCambiada(true);
        usuariRepository.save(usuari);
    }

    @Transactional
    public String createPasswordResetToken(String email) throws Exception {
        Optional<Usuari> usuariOpt = usuariRepository.findByEmail(email);
        if (usuariOpt.isEmpty()) {
            throw new Exception("No existe ningún usuario con este email");
        }
        Usuari usuari = usuariOpt.get();
        if (!usuari.getActivo()) {
            throw new Exception("El usuario está desactivado");
        }
        tokenRepository.deleteByUsuari(usuari);
        String code = String.format("%06d", (int)(Math.random() * 1000000));
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = new PasswordResetToken(code, token, usuari);
        tokenRepository.save(resetToken);
        return code;
    }

    public boolean sendPasswordResetEmail(String email, String code) {
        if (mailSender == null) {
            log.warn("JavaMailSender no configurado. Código: {}", code);
            return false;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("EasyTraza - Código de Recuperación de Contraseña");
            message.setText("""
                Has solicitado recuperar tu contraseña de EasyTraza.
                Tu código de verificación es: %s
                Este código expirará en 10 minutos.
                Introdúcelo en la pantalla de recuperación para restablecer tu contraseña.
                Si no has solicitado este cambio, ignora este mensaje.
                EasyTraza - Sistema de Trazabilidad
                """.formatted(code));
            mailSender.send(message);
            log.info("EMAIL enviado correctamente a: {}", email);
            return true;
        } catch (Exception e) {
            log.error("ERROR al enviar email a {}: {}", email, e.getMessage());
            log.warn("Código (fallback): {}", code);
            return false;
        }
    }

    public Optional<PasswordResetToken> validatePasswordResetCode(String code) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByCode(code);
        if (resetToken.isEmpty()) return Optional.empty();
        PasswordResetToken t = resetToken.get();
        if (t.isExpired() || t.getUsed()) return Optional.empty();
        return resetToken;
    }

    @Transactional
    public void resetPassword(String token, String newPassword) throws Exception {
        Optional<PasswordResetToken> resetTokenOpt = tokenRepository.findByToken(token);
        if (resetTokenOpt.isEmpty()) {
            throw new Exception("El enlace de recuperación no es válido o ha expirado");
        }
        PasswordResetToken resetToken = resetTokenOpt.get();
        if (resetToken.isExpired() || resetToken.getUsed()) {
            throw new Exception("El enlace de recuperación no es válido o ha expirado");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new Exception("La contraseña debe tener al menos 6 caracteres");
        }
        Usuari usuari = resetToken.getUsuari();
        usuari.setPassword(passwordEncoder.encode(newPassword));
        usuari.setPasswordCambiada(true);
        usuariRepository.save(usuari);
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }

    public Usuari obtenerUsuarioActual() {
        return null;
    }
}
