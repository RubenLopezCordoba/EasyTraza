package cat.copernic.easytraza.service;

import cat.copernic.easytraza.model.MateriaPrimera;
import cat.copernic.easytraza.model.Producte;
import cat.copernic.easytraza.repository.LiniarClientRepository;
import cat.copernic.easytraza.repository.MateriaPrimeraRepository;
import cat.copernic.easytraza.repository.ProducteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
/**
 * Servei per gestionar la logica de negoci de ProducteService.
 */
public class ProducteService {
    private static final Logger log = LoggerFactory.getLogger(ProducteService.class);
@Autowired
    private ProducteRepository producteRepository;

    @Autowired
    private MateriaPrimeraRepository materiaPrimeraRepository;

    @Autowired
    private cat.copernic.easytraza.repository.LotRepository lotRepository;

    @Autowired
    private LiniarClientRepository liniarClientRepository;

    public List<Producte> findAll() {
        return producteRepository.findAll();
    }

    public Optional<Producte> findById(Long id) {
        return producteRepository.findById(id);
    }

    public List<Producte> findByNom(String nom) {
        return producteRepository.findByNomContainingIgnoreCase(nom);
    }

    public Producte save(Producte producte) throws IllegalArgumentException {
        if (producte.getNom() == null || producte.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (producte.getId() == null) {
            if (existsByNom(producte.getNom())) {
                throw new IllegalArgumentException("Ja existeix un producte amb el nom: " + producte.getNom());
            }
        } else {
            if (existsByNomExcludingId(producte.getNom(), producte.getId())) {
                throw new IllegalArgumentException("Ja existeix un altre producte amb el nom: " + producte.getNom());
            }
        }
        producte.setNom(producte.getNom().trim());
        if (producte.getDescripcio() != null) {
            producte.setDescripcio(producte.getDescripcio().trim());
        }
        return producteRepository.save(producte);
    }

    public List<MateriaPrimera> findAllMateriesPrimeres() {
        return materiaPrimeraRepository.findAll();
    }

    public Optional<MateriaPrimera> findMateriaPrimeraById(Long id) {
        return materiaPrimeraRepository.findById(id);
    }

    public List<MateriaPrimera> findMateriaPrimeraByNom(String nom) {
        return materiaPrimeraRepository.findByNomIgnoreCase(nom);
    }

    public MateriaPrimera saveMateriaPrimera(MateriaPrimera mp) throws IllegalArgumentException {
        if (mp.getNom() == null || mp.getNom().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        return materiaPrimeraRepository.save(mp);
    }

    public void deleteMateriaPrimeraById(Long id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        if (!materiaPrimeraRepository.existsById(id)) {
            throw new IllegalArgumentException("No existe una materia prima con ID: " + id);
        }
        var lots = lotRepository.findByMateriaPrimeraId(id);
        if (!lots.isEmpty()) {
            throw new IllegalArgumentException(
                "No es pot eliminar la matèria primera perquè té " + lots.size() + " lot(s) associat(s)." +
                "Primer has d'eliminar els lots que la referencien."
            );
        }
        materiaPrimeraRepository.deleteById(id);
    }

    public void deleteById(Long id) throws IllegalArgumentException {
        if (id == null) {
            throw new IllegalArgumentException("El ID no puede ser nulo");
        }
        Optional<Producte> producteOpt = producteRepository.findById(id);
        if (!producteOpt.isPresent()) {
            throw new IllegalArgumentException("No existe un producto con ID: " + id);
        }
        Producte producte = producteOpt.get();
        var lots = lotRepository.findByMateriaPrimeraId(id);
        if (!lots.isEmpty()) {
            throw new IllegalArgumentException(
                "No es pot eliminar el producte \"" + producte.getNom() +
                "\" perquè té " + lots.size() + " lot(s) associat(s). " +
                "Primer has d'eliminar els lots que el referencien."
            );
        }
        var liniesClient = liniarClientRepository.findByIdProducte(id);
        if (!liniesClient.isEmpty()) {
            throw new IllegalArgumentException(
                "No es pot eliminar el producte \"" + producte.getNom() +
                "\" perquè té " + liniesClient.size() + " línia(s) en albarans de client. " +
                "Primer has d'eliminar les línies que el referencien."
            );
        }
        producteRepository.deleteById(id);
    }

    private boolean existsByNom(String nom) {
        return !producteRepository.findByNomIgnoreCase(nom).isEmpty();
    }

    private boolean existsByNomExcludingId(String nom, Long id) {
        List<Producte> existents = producteRepository.findByNomIgnoreCase(nom);
        return existents.stream().anyMatch(p -> !p.getId().equals(id));
    }
}
