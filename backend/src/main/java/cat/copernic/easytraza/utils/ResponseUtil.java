package cat.copernic.easytraza.utils;

import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

/**
 * Utilitat per a crear respostes HTTP consistents en format JSON amb success/error.
 */
public class ResponseUtil {

    private ResponseUtil() {}

    public static ResponseEntity<Map<String, Object>> ok(String message) {
        return ResponseEntity.ok(Map.of("success", true, "message", message));
    }

    public static ResponseEntity<Map<String, Object>> ok(String message, String dataKey, Object dataValue) {
        Map<String, Object> r = new HashMap<>();
        r.put("success", true);
        r.put("message", message);
        if (dataKey != null && dataValue != null) {
            r.put(dataKey, dataValue);
        }
        return ResponseEntity.ok(r);
    }

    public static ResponseEntity<Map<String, Object>> error(String message) {
        return ResponseEntity.badRequest().body(Map.of("success", false, "error", message));
    }

    public static ResponseEntity<Map<String, Object>> error(int status, String message) {
        return ResponseEntity.status(status).body(Map.of("success", false, "error", message));
    }

    public static ResponseEntity<Map<String, Object>> unauthorized(String message) {
        return ResponseEntity.status(401).body(Map.of("success", false, "error", message));
    }

    public static ResponseEntity<Map<String, Object>> forbidden(String message) {
        return ResponseEntity.status(403).body(Map.of("success", false, "error", message));
    }
}
