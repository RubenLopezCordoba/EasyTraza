package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.model.AlbarraClient;
import cat.copernic.easytraza.model.AlbarraClientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

/**
 * Repositori JPA per a operacions de persistència d'AlbarraClient.
 */
public interface AlbarraClientRepository extends JpaRepository<AlbarraClient, AlbarraClientId> {
    List<AlbarraClient> findAllByOrderByDataProduccioDesc();

    @Query("SELECT a FROM AlbarraClient a LEFT JOIN FETCH a.linies WHERE a.estat = ?1")
    List<AlbarraClient> findByEstatWithLinies(String estat);

    List<AlbarraClient> findByEstat(String estat);

    @Query("SELECT a FROM AlbarraClient a LEFT JOIN FETCH a.linies WHERE a.nifClient = :nifClient AND a.dataProduccio = :dataProduccio")
    Optional<AlbarraClient> findByIdWithLinies(@Param("nifClient") String nifClient, @Param("dataProduccio") java.time.LocalDate dataProduccio);

    List<AlbarraClient> findByClientNif(String clientNif);
}
