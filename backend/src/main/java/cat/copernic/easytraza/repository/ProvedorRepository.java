package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.model.Provedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repositori JPA per a operacions de persistència de Provedor.
 */
public interface ProvedorRepository extends JpaRepository<Provedor, String> {

    Optional<Provedor> findByNif(String nif);

    boolean existsByNif(String nif);

    List<Provedor> findByNombreContainingIgnoreCase(String nombre);

    void deleteByNif(String nif);
}
