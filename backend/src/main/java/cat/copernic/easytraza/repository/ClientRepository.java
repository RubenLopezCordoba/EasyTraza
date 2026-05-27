package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * Repositori JPA per a operacions de persistència de Client.
 */
public interface ClientRepository extends JpaRepository<Client, String> {
    Optional<Client> findByNif(String nif);
    boolean existsByNif(String nif);
}
