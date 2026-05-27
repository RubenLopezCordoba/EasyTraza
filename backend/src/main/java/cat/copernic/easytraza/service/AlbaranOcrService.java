package cat.copernic.easytraza.service;

import cat.copernic.easytraza.model.*;
import cat.copernic.easytraza.repository.*;
import cat.copernic.easytraza.utils.CifValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
/**
 * Servei per gestionar la logica de negoci de AlbaranOcrService.
 */
public class AlbaranOcrService {
    private static final Logger log = LoggerFactory.getLogger(AlbaranOcrService.class);
@Autowired
    private OcrService ocrService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private GroqParsingService parsingService;

    @Autowired
    private AlbaraRepository albaraRepository;

    @Autowired
    private ProvedorRepository provedorRepository;

    @Autowired
    private ProducteRepository producteRepository;

    @Autowired
    private MateriaPrimeraRepository materiaPrimeraRepository;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private LiniarProveidorRepository liniarProveidorRepository;

    @Autowired
    private UsuariRepository usuariRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Transactional
    public AlbarraProveidor procesarAlbaranCompleto(MultipartFile imagen, Long usuarioId) {
        String textoOCR = ocrService.extractText(imagen);
        if (textoOCR == null || textoOCR.trim().isEmpty()) {
            throw new RuntimeException("No se pudo extraer texto de la imagen");
        }

        AlbaranParsedDTO parsed = parsingService.parseText(textoOCR);
        if (parsed.getNumAlbara() == null || parsed.getNumAlbara().isEmpty()) {
            throw new RuntimeException("No se pudo identificar el número de albarán");
        }

        Usuari usuari = usuariRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuarioId));

        Provedor provedor = buscarOcrearProvedor(parsed.getProveedor());

        if (albaraRepository.existsByIdAlbarra(parsed.getNumAlbara())) {
            throw new RuntimeException("Ja existeix un albarà amb el número " + parsed.getNumAlbara());
        }

        AlbarraProveidor albara = new AlbarraProveidor();
        albara.setIdAlbarra(parsed.getNumAlbara());
        albara.setProvedor(provedor);
        albara.setUsuari(usuari);

        if (parsed.getFecha() != null && !parsed.getFecha().isEmpty()) {
            try {
                LocalDateTime fecha = LocalDate.parse(parsed.getFecha(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay();
                albara.setDataRecepcio(fecha);
            } catch (Exception e) {
                albara.setDataRecepcio(LocalDateTime.now());
            }
        } else {
            albara.setDataRecepcio(LocalDateTime.now());
        }

        albara.setTextoOcr(textoOCR);
        try {
            albara.setJsonParsejat(objectMapper.writeValueAsString(parsed));
        } catch (Exception e) {
            albara.setJsonParsejat("Error serializando JSON");
        }

        String imagenUrl = guardarImagen(imagen);
        albara.setImagenUrl(imagenUrl);

        AlbarraProveidor albaraGuardado = albaraRepository.save(albara);

        if (parsed.getLots() != null) {
            for (AlbaranParsedDTO.LoteParsed loteParsed : parsed.getLots()) {
                if (loteParsed.getDescripcion() == null || loteParsed.getDescripcion().isEmpty()) continue;

                MateriaPrimera mp = (MateriaPrimera) buscarOcrearProducte(loteParsed);
                String idLot = loteParsed.getLote() != null ? loteParsed.getLote() : "L" + System.currentTimeMillis();

                if (lotRepository.existsByIdLot(idLot)) {
                    throw new RuntimeException("Ja existeix un lot amb el número " + idLot);
                }

                Integer cantidad = convertirCantidad(loteParsed);
                String unitat = loteParsed.getUnidad() != null ? loteParsed.getUnidad() : "kg";
                Lot lote = new Lot(idLot, provedor, mp);
                lote.setQuantitat(cantidad);
                lote.setUnitat(unitat);
                lote.setLotProveidor(loteParsed.getLote());

                if (loteParsed.getFechaConsumo() != null && !loteParsed.getFechaConsumo().isEmpty()) {
                    try {
                        lote.setDataCaducitat(LocalDate.parse(loteParsed.getFechaConsumo(),
                                DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay());
                    } catch (Exception e) { }
                }

                lote.setEstat("EN_ESTOC");
                lote.setDataRecepcio(albaraGuardado.getDataRecepcio());
                lote = lotRepository.saveAndFlush(lote);

                LiniarProveidor linia = new LiniarProveidor(albaraGuardado, lote, cantidad, unitat);
                albaraGuardado.getLinies().add(linia);
            }
        }

        albaraRepository.saveAndFlush(albaraGuardado);
        entityManager.clear();
        AlbarraProveidor albaranFinal = albaraRepository.findByIdWithLinies(provedor.getNif(), parsed.getNumAlbara())
                .orElse(albaraGuardado);

        if (albaranFinal.getLinies() == null || albaranFinal.getLinies().isEmpty()) {
            liniarProveidorRepository.deleteByAlbaraId(provedor.getNif(), parsed.getNumAlbara());
            albaraRepository.deleteById(new AlbarraProveidorId(provedor.getNif(), parsed.getNumAlbara()));
            return null;
        }

        return albaranFinal;
    }

    private Provedor buscarOcrearProvedor(AlbaranParsedDTO.ProveedorParsed p) {
        if (p == null || p.getCif() == null || p.getCif().isEmpty()) {
            if (p != null && p.getNombre() != null && !p.getNombre().isEmpty()) {
                List<Provedor> porNombre = provedorRepository.findByNombreContainingIgnoreCase(p.getNombre().trim());
                if (!porNombre.isEmpty()) return porNombre.get(0);
            }
            throw new RuntimeException("No se encontró CIF ni nombre del proveedor");
        }

        String cifLimpio = p.getCif().replaceAll("[\\s\\-\\.,;_]", "").toUpperCase().trim();
        Optional<Provedor> existente = provedorRepository.findByNif(cifLimpio);
        if (existente.isPresent()) return existente.get();

        if (p.getNombre() != null && !p.getNombre().isEmpty()) {
            List<Provedor> porNombre = provedorRepository.findByNombreContainingIgnoreCase(p.getNombre().trim());
            if (!porNombre.isEmpty()) return porNombre.get(0);
        }

        if (!CifValidator.isValid(cifLimpio)) {
            throw new RuntimeException("CIF no vàlid: " + cifLimpio);
        }

        Provedor nuevo = new Provedor();
        nuevo.setNif(cifLimpio);
        nuevo.setNombre(p.getNombre() != null ? p.getNombre().trim() : "PROVEEDOR_" + cifLimpio);
        nuevo.setDireccion(p.getDireccion() != null ? p.getDireccion().trim() : "");
        nuevo.setTelefono(null);
        nuevo.setObservaciones("Creado automáticamente por OCR");
        return provedorRepository.save(nuevo);
    }

    private MateriaPrimera buscarOcrearProducte(AlbaranParsedDTO.LoteParsed loteParsed) {
        if (loteParsed.getDescripcion() == null || loteParsed.getDescripcion().isEmpty()) {
            throw new RuntimeException("Lote sin descripción");
        }
        String nomProducte = loteParsed.getDescripcion().trim().replaceAll("\\s+", " ").toUpperCase();
        if (nomProducte.length() > 100) nomProducte = nomProducte.substring(0, 97) + "...";

        List<MateriaPrimera> existentes = materiaPrimeraRepository.findByNomIgnoreCase(nomProducte);
        if (!existentes.isEmpty()) return existentes.get(0);

        MateriaPrimera nuevo = new MateriaPrimera();
        nuevo.setNom(nomProducte);
        nuevo.setDescripcio(loteParsed.getDescripcion());
        return materiaPrimeraRepository.save(nuevo);
    }

    private Integer convertirCantidad(AlbaranParsedDTO.LoteParsed lote) {
        if (lote.getCantidad() == null) return 0;
        String unidad = lote.getUnidad() != null ? lote.getUnidad().toUpperCase() : "";
        return switch (unidad) {
            case "TONELADAS", "TONELADA", "T" -> (int) (lote.getCantidad() * 1000);
            case "SACOS", "SACO", "UDS", "UD", "UNIDADES" -> lote.getCantidad().intValue();
            default -> (int) Math.round(lote.getCantidad());
        };
    }

    private String guardarImagen(MultipartFile imagen) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);
            String nombreArchivo = UUID.randomUUID() + "_" + imagen.getOriginalFilename();
            Path filePath = uploadPath.resolve(nombreArchivo);
            Files.copy(imagen.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public AlbaranParsedDTO analizarSinGuardar(MultipartFile imagen) {
        String textoOCR = ocrService.extractText(imagen);
        if (textoOCR == null || textoOCR.trim().isEmpty()) {
            throw new RuntimeException("No se pudo extraer texto de la imagen");
        }
        AlbaranParsedDTO parsed = parsingService.parseText(textoOCR);
        parsed.setImagenTemporal(guardarImagen(imagen));
        return parsed;
    }

    @Transactional
    public AlbarraProveidor guardarDesdeConfirmacion(AlbaranParsedDTO datos, Long usuarioId) {
        Usuari usuari = usuariRepository.findById(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + usuarioId));

        if (albaraRepository.existsByIdAlbarra(datos.getNumAlbara())) {
            throw new RuntimeException("Ja existeix un albarà amb el número " + datos.getNumAlbara());
        }

        Provedor provedor = buscarOcrearProvedor(datos.getProveedor());

        AlbarraProveidor albara = new AlbarraProveidor();
        albara.setIdAlbarra(datos.getNumAlbara());
        albara.setProvedor(provedor);
        albara.setUsuari(usuari);

        if (datos.getFecha() != null && !datos.getFecha().isEmpty()) {
            try {
                LocalDateTime fecha = LocalDate.parse(datos.getFecha(),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay();
                albara.setDataRecepcio(fecha);
            } catch (Exception e) {
                albara.setDataRecepcio(LocalDateTime.now());
            }
        } else {
            albara.setDataRecepcio(LocalDateTime.now());
        }

        albara.setObservacions(datos.getObservacions());
        try {
            albara.setJsonParsejat(objectMapper.writeValueAsString(datos));
        } catch (Exception e) {
            albara.setJsonParsejat("{}");
        }
        albara.setImagenUrl(datos.getImagenTemporal());

        AlbarraProveidor albaraGuardado = albaraRepository.save(albara);

        if (datos.getLots() != null) {
            for (AlbaranParsedDTO.LoteParsed loteParsed : datos.getLots()) {
                MateriaPrimera mp;
                if (loteParsed.getCatalogo() != null && loteParsed.getCatalogo().getId() != null && loteParsed.getCatalogo().getId() > 0) {
                    Long catId = loteParsed.getCatalogo().getId();
                    mp = materiaPrimeraRepository.findById(catId).orElse(null);
                    if (mp == null) {
                        mp = new MateriaPrimera();
                        mp.setNom(loteParsed.getDescripcion() != null ? loteParsed.getDescripcion().toUpperCase() : "MP_" + catId);
                        mp.setDescripcio(loteParsed.getDescripcion());
                        mp = materiaPrimeraRepository.save(mp);
                    }
                } else if (loteParsed.getDescripcion() != null && !loteParsed.getDescripcion().isEmpty()) {
                    mp = (MateriaPrimera) buscarOcrearProducte(loteParsed);
                } else {
                    continue;
                }

                String idLot = loteParsed.getNumLot() != null && !loteParsed.getNumLot().isEmpty()
                    ? loteParsed.getNumLot()
                    : (loteParsed.getLote() != null ? loteParsed.getLote() : "L" + System.currentTimeMillis());

                Integer cantidad = loteParsed.getCantidad() != null ? (int) Math.round(loteParsed.getCantidad()) : 0;
                if (loteParsed.getQuantitat() != null) cantidad = loteParsed.getQuantitat();
                String unitat = loteParsed.getUnidad() != null ? loteParsed.getUnidad() : "kg";

                Lot lote;
                Optional<Lot> existingLot = lotRepository.findById(new LotId(provedor.getNif(), idLot));
                if (existingLot.isPresent()) {
                    lote = existingLot.get();
                } else {
                    lote = new Lot(idLot, provedor, mp);
                    lote.setQuantitat(cantidad);
                    lote.setUnitat(unitat);
                    if (loteParsed.getFechaConsumo() != null && !loteParsed.getFechaConsumo().isEmpty()) {
                        try {
                            lote.setDataCaducitat(LocalDate.parse(loteParsed.getFechaConsumo(),
                                    DateTimeFormatter.ofPattern("dd/MM/yyyy")).atStartOfDay());
                        } catch (Exception e) { }
                    }
                    lote.setEstat("EN_ESTOC");
                    lote.setDataRecepcio(albaraGuardado.getDataRecepcio());
                    lote = lotRepository.saveAndFlush(lote);
                }

                LiniarProveidor linia = new LiniarProveidor(albaraGuardado, lote, cantidad, unitat);
                albaraGuardado.getLinies().add(linia);
            }
        }

        albaraRepository.saveAndFlush(albaraGuardado);
        entityManager.clear();

        AlbarraProveidor albaranFinal = albaraRepository.findByIdWithLinies(provedor.getNif(), datos.getNumAlbara())
                .orElse(albaraGuardado);

        if (albaranFinal.getLinies() == null || albaranFinal.getLinies().isEmpty()) {
            liniarProveidorRepository.deleteByAlbaraId(provedor.getNif(), datos.getNumAlbara());
            albaraRepository.deleteById(new AlbarraProveidorId(provedor.getNif(), datos.getNumAlbara()));
            return null;
        }
        return albaranFinal;
    }
}
