package cat.copernic.easytraza.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
/**
 * Interceptador HTTP que verifica la sessió d'usuari abans de processar les peticions.
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("usuario") == null) {
            String path = request.getRequestURI();
            if (path.startsWith("/api/")) {
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"No autenticat\"}");
            } else {
                response.sendRedirect("/login");
            }
            return false;
        }
        return true;
    }
}
