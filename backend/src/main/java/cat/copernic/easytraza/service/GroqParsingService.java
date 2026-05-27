package cat.copernic.easytraza.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
/**
 * Servei per gestionar la logica de negoci de GroqParsingService.
 */
public class GroqParsingService {
    private static final Logger log = LoggerFactory.getLogger(GroqParsingService.class);
@Value("${groq.api-key}")
    private String apiKey;

    @Value("${groq.model}")
    private String model;

    @Value("${groq.endpoint}")
    private String endpoint;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GroqParsingService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Envía el texto OCR a Groq y parsea la respuesta JSON
     */
    public AlbaranParsedDTO parseText(String ocrText) {
        log.info("=== ENVIANDO A GROQ PARA PARSEO ===");
        
        // Construir el body de la petición
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.1); // Precisión máxima
        requestBody.put("messages", List.of(
            Map.of("role", "system", "content", getSystemPrompt()),
            Map.of("role", "user", "content", "Extrae los datos de este albarán:\n\n" + ocrText)
        ));

        // Configurar headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);

        try {
            // Llamar a Groq
            ResponseEntity<Map> response = restTemplate.postForEntity(
                endpoint, requestEntity, Map.class);

            // Extraer el JSON de la respuesta
            String jsonContent = extractJsonFromResponse(response.getBody());
            
            log.info("=== JSON PARSEADO POR IA ===");
            log.info(String.valueOf(jsonContent));
            log.info("=== FIN JSON ===");

            // Convertir JSON a DTO
            return objectMapper.readValue(jsonContent, AlbaranParsedDTO.class);

        } catch (Exception e) {
            log.error("Error en parsing Groq: {}", e.getMessage());
            throw new RuntimeException("Error al parsear con IA: " + e.getMessage(), e);
        }
    }

    /**
     * Prompt del sistema para Groq
     */
    private String getSystemPrompt() {
        return """
            Eres un asistente ESPECIALIZADO en extraer datos de albaranes españoles.
            
            Devuelve ÚNICAMENTE un JSON válido, sin explicaciones, sin markdown.
            
            ESTRUCTURA EXACTA:
            {
              "numAlbara": "string o null",
              "proveedor": {
                "cif": "string o null",
                "nombre": "string o null",
                "direccion": "string o null"
              },
              "fecha": "dd/MM/yyyy o null",
              "lots": [
                {
                  "codigoArticulo": "string o null",
                  "descripcion": "string o null",
                  "cantidad": numero,
                  "unidad": "string o null",
                  "lote": "string o null",
                  "fechaConsumo": "dd/MM/yyyy o null"
                }
              ]
            }
            
            REGLAS IMPORTANTES:
            1. Numero albarán: Suele empezar por AVH o ser numerico. Ej: "AVH316590", "011893"
            2. CIF: Letra + 8 dígitos. Ej: "J59087312", "B12345678"
            3. Fechas SIEMPRE en formato dd/MM/yyyy
            4. Cantidad: número decimal con PUNTO. Ej: 1.250 (NO usar coma)
            5. Unidad: "TONELADAS", "SACOS", "Kg", "Uds"
            6. Lote: código del fabricante. Ej: "M1952322", "CS1213204"
            7. IGNORA palets, embalajes, artículos de transporte
            8. Si no encuentras un campo, usa NULL (no texto vacío)
            9. Extrae TODOS los lotes/productos que aparezcan en tablas
            """;
    }

    /**
     * Extrae el texto JSON de la respuesta de Groq
     */
    private String extractJsonFromResponse(Map responseBody) {
        try {
            List<Map<String, Object>> choices = (List<Map<String, Object>>) responseBody.get("choices");
            Map<String, Object> choice = choices.get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            String content = (String) message.get("content");

            // Limpiar formato markdown si lo trae
            content = content.replace("```json", "").replace("```", "").trim();
            
            return content;
        } catch (Exception e) {
            throw new RuntimeException("No se pudo extraer JSON de la respuesta", e);
        }
    }
}