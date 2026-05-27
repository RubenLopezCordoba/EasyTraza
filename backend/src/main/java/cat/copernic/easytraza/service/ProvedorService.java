package cat.copernic.easytraza.service;

import cat.copernic.easytraza.model.Provedor;
import cat.copernic.easytraza.repository.ProvedorRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
/**
 * Servei per gestionar la logica de negoci de ProvedorService.
 */
public class ProvedorService {
    private static final Logger log = LoggerFactory.getLogger(ProvedorService.class);
    @Autowired
    private ProvedorRepository repo;

    public List<Provedor> findAll() { return repo.findAll(); }

    public Optional<Provedor> findById(String nif) { return repo.findByNif(nif); }

    public List<Provedor> findByNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) return repo.findAll();
        return repo.findByNombreContainingIgnoreCase(nombre.trim());
    }

    public Provedor save(Provedor provedor) throws IllegalArgumentException {
        if (provedor.getNif() == null || provedor.getNif().trim().isEmpty()) {
            throw new IllegalArgumentException("El NIF es obligatorio");
        }
        if (provedor.getNombre() == null || provedor.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        provedor.setNif(provedor.getNif().trim().toUpperCase());
        provedor.setNombre(provedor.getNombre().trim());
        return repo.save(provedor);
    }

    public void deleteById(String nif) {
        if (nif == null) throw new IllegalArgumentException("El NIF no puede ser nulo");
        if (!repo.existsByNif(nif)) throw new IllegalArgumentException("No existe proveedor con NIF: " + nif);
        try {
            repo.deleteById(nif);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("No es pot eliminar el proveïdor perquè té albarans associats");
        }
    }

    public long count() { return repo.count(); }
}
