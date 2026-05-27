package cat.copernic.easytraza.service;

import cat.copernic.easytraza.exception.OcrException;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
/**
 * Servei per gestionar la logica de negoci de OcrService.
 */
public class OcrService {
    private static final Logger log = LoggerFactory.getLogger(OcrService.class);
@Value("${tesseract.datapath}")
    private String tesseractDataPath;

    @Value("${tesseract.language}")
    private String language;

    @Value("${tesseract.page-seg-mode}")
    private int pageSegMode;

    public String extractText(MultipartFile file) {
        try {
            // Leer imagen
            BufferedImage original = ImageIO.read(file.getInputStream());
            if (original == null) {
                throw new IOException("No se pudo leer la imagen");
            }

            // AUMENTAR TAMAÑO (x2) para mejorar OCR
            int newWidth = original.getWidth() * 2;
            int newHeight = original.getHeight() * 2;
            BufferedImage scaled = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_BYTE_GRAY);
            Graphics2D g2d = scaled.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
            g2d.dispose();

            // Aumentar contraste
            RescaleOp rescale = new RescaleOp(2.0f, 0, null);
            BufferedImage enhanced = rescale.filter(scaled, null);

            // Configurar Tesseract
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tesseractDataPath);
            tesseract.setLanguage(language);
            tesseract.setPageSegMode(pageSegMode);
            tesseract.setOcrEngineMode(1); // LSTM only

            String resultado = tesseract.doOCR(enhanced);

            log.info("=== TEXTO EXTRAÍDO POR OCR ===");
            log.info(String.valueOf(resultado));
            log.info("=== FIN TEXTO OCR ===");

            return resultado;

        } catch (TesseractException e) {
            throw new OcrException("Error de Tesseract: " + e.getMessage(), e);
        } catch (IOException e) {
            throw new OcrException("Error al leer la imagen: " + e.getMessage(), e);
        }
    }
}