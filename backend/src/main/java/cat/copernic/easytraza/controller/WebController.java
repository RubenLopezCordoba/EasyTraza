package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.model.Rol;
import cat.copernic.easytraza.model.Usuari;
import cat.copernic.easytraza.service.UsuariService;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador de pàgines web i recuperació de contrasenya (forgot-password, verify-code, reset-password).
 */
@Controller
public class WebController {
    private static final Logger log = LoggerFactory.getLogger(WebController.class);
@Autowired
    private UsuariService usuariService;

    @Autowired(required = false)
    private JavaMailSender mailSender;

    private final Map<String, ResetData> resetCodes = new HashMap<>();
    private final SecureRandom random = new SecureRandom();

    private static class ResetData {
        String email;
        String code;
        LocalDateTime expires;
        boolean verified;
        ResetData(String email, String code) {
            this.email = email;
            this.code = code;
            this.expires = LocalDateTime.now().plusMinutes(10);
            this.verified = false;
        }
        boolean isExpired() { return LocalDateTime.now().isAfter(expires); }
    }

    @GetMapping("/albarans")
    /**
     * Executa l'operació albarans.
     */
    public String albarans() { return "albarans"; }

    @GetMapping("/proveedores")
    /**
     * Executa l'operació proveedores.
     */
    public String proveedores(HttpSession session) {
        Usuari u = (Usuari) session.getAttribute("usuario");
        if (u != null && Rol.TRABAJADOR.name().equals(u.getRol())) return "redirect:/dashboard";
        return "proveedores";
    }

    @GetMapping("/lotes")
    /**
     * Executa l'operació lotes.
     */
    public String lotes() { return "lotes"; }

    @GetMapping("/client-albarans")
    /**
     * Executa l'operació clientAlbarans.
     */
    public String clientAlbarans() { return "client-albarans"; }

    @GetMapping("/clientes")
    /**
     * Executa l'operació clientes.
     */
    public String clientes(HttpSession session) {
        Usuari u = (Usuari) session.getAttribute("usuario");
        if (u != null && Rol.TRABAJADOR.name().equals(u.getRol())) return "redirect:/dashboard";
        return "clientes";
    }

    @GetMapping("/dashboard")
    /**
     * Executa l'operació dashboard.
     */
    public String dashboard() { return "panel"; }

    @GetMapping("/controls")
    /**
     * Executa l'operació controls.
     */
    public String controls() { return "controls"; }

    @GetMapping("/informes")
    /**
     * Executa l'operació informes.
     */
    public String informes() { return "informes"; }

    @GetMapping("/forgot-password")
    /**
     * Executa l'operació forgotPassword.
     */
    public String forgotPassword() { return "forgot-password"; }

    @PostMapping("/forgot-password")
    /**
     * Executa l'operació forgotPasswordPost.
     */
    public String forgotPasswordPost(@RequestParam String email, Model model) {
        String code = String.format("%06d", random.nextInt(1000000));
        resetCodes.put(code, new ResetData(email, code));
        model.addAttribute("success", true);
        model.addAttribute("email", email);

        try {
            if (mailSender != null) {
                MimeMessage msg = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
                helper.setTo(email);
                helper.setSubject("EasyTraza - Codi de recuperació de contrasenya");
                helper.setText("<h2>Recuperació de contrasenya</h2>"
                    + "<p>Has sol·licitat restablir la teva contrasenya d'EasyTraza.</p>"
                    + "<p style=\"font-size:24px;font-weight:bold;text-align:center;letter-spacing:8px;\">"
                    + code + "</p>"
                    + "<p>Aquest codi expira en 10 minuts.</p>"
                    + "<p>Si no has sol·licitat aquest canvi, ignora aquest missatge.</p>", true);
                mailSender.send(msg);
            } else {
                model.addAttribute("devCode", code);
            }
        } catch (Exception e) {
            model.addAttribute("devCode", code);
        }
        return "forgot-password";
    }

    @GetMapping("/verify-code")
    /**
     * Executa l'operació verifyCode.
     */
    public String verifyCode(@RequestParam(required = false) String email, Model model) {
        model.addAttribute("email", email);
        return "verify-code";
    }

    @PostMapping("/verify-code")
    /**
     * Executa l'operació verifyCodePost.
     */
    public String verifyCodePost(@RequestParam String email, @RequestParam String code, Model model) {
        ResetData data = resetCodes.get(code);
        if (data == null || !data.email.equals(email) || data.isExpired()) {
            model.addAttribute("error", "Codi invàlid o expirat");
            model.addAttribute("email", email);
            return "verify-code";
        }
        data.verified = true;
        String token = code + "_" + System.currentTimeMillis();
        resetCodes.put(token, data);
        return "redirect:/reset-password?token=" + token;
    }

    @GetMapping("/reset-password")
    /**
     * Executa l'operació resetPassword.
     */
    public String resetPassword(@RequestParam String token, Model model) {
        ResetData data = resetCodes.get(token);
        if (data == null || data.isExpired() || !data.verified) {
            model.addAttribute("error", "Enllaç invàlid o expirat");
            return "reset-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    /**
     * Executa l'operació resetPasswordPost.
     */
    public String resetPasswordPost(@RequestParam String token, @RequestParam String password,
                                     @RequestParam String confirmPassword, Model model) throws Exception {
        ResetData data = resetCodes.get(token);
        if (data == null || data.isExpired() || !data.verified) {
            model.addAttribute("error", "Enllaç invàlid o expirat");
            return "reset-password";
        }
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Les contrasenyes no coincideixen");
            model.addAttribute("token", token);
            return "reset-password";
        }
        if (password.length() < 6) {
            model.addAttribute("error", "La contrasenya ha de tenir almenys 6 caràcters");
            model.addAttribute("token", token);
            return "reset-password";
        }
        Optional<Usuari> usuari = usuariService.findByEmail(data.email);
        if (usuari.isPresent()) {
            usuariService.cambiarPassword(usuari.get().getId(), password);
            model.addAttribute("success", "Contrasenya restablerta correctament");
        } else {
            model.addAttribute("error", "Usuari no trobat");
        }
        resetCodes.remove(token);
        return "reset-password";
    }
}
