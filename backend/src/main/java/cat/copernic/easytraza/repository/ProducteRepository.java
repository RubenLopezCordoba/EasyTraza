package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.model.Producte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
/**
 * Repositori JPA per a operacions de persistència de Producte.
 */
public interface ProducteRepository extends JpaRepository<Producte, Long> {

    List<Producte> findByNomContainingIgnoreCase(String nom);

    List<Producte> findByNomIgnoreCase(String nom);

    @Query("SELECT p FROM Producte p WHERE LOWER(p.nom) LIKE LOWER(CONCAT('%', :searchText, '%')) OR LOWER(p.descripcio) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<Producte> searchByText(@Param("searchText") String searchText);

    @Query("SELECT p FROM Producte p ORDER BY p.id DESC")
    List<Producte> findLastFiveItems();
}
