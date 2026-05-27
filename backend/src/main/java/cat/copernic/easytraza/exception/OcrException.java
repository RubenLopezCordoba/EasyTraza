package cat.copernic.easytraza.exception;

/**
 * Excepció personalitzada per a errors en el procés OCR.
 */
public class OcrException extends RuntimeException {
    
    public OcrException(String message) {
        super(message);
    }
    
    public OcrException(String message, Throwable cause) {
        super(message, cause);
    }
}