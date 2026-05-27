package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.model.MateriaPrimera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
/**
 * Repositori JPA per a operacions de persistència de MateriaPrimera.
 */
public interface MateriaPrimeraRepository extends JpaRepository<MateriaPrimera, Long> {

    List<MateriaPrimera> findByNomContainingIgnoreCase(String nom);

    List<MateriaPrimera> findByNomIgnoreCase(String nom);
}
