package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.model.Lot;
import cat.copernic.easytraza.model.LotId;
import cat.copernic.easytraza.model.MateriaPrimera;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repositori JPA per a operacions de persistència de Lot.
 */
public interface LotRepository extends JpaRepository<Lot, LotId> {
    List<Lot> findByEstat(String estat);

    List<Lot> findByEstatIn(List<String> estats);
    List<Lot> findByMateriaPrimera(MateriaPrimera materiaPrimera);
    List<Lot> findByMateriaPrimeraId(Long materiaPrimeraId);
    List<Lot> findByMateriaPrimeraIdAndEstat(Long materiaPrimeraId, String estat);
    List<Lot> findByNifProveidorAndEstat(String nifProveidor, String estat);
    boolean existsByIdLot(String idLot);

    @Query("SELECT l FROM Lot l WHERE l.estat = 'OBERT'")
    List<Lot> findLotsOberts();

    @Query("SELECT l FROM Lot l WHERE l.estat = 'OBERT' AND l.dataRecepcio <= :dataProduccio")
    List<Lot> findLotsPerTraçabilitat(@Param("dataProduccio") java.time.LocalDateTime dataProduccio);

    Optional<Lot> findByNifProveidorAndIdLot(String nifProveidor, String idLot);

    Optional<Lot> findByIdLot(String idLot);

    @Query("SELECT l FROM Lot l WHERE " +
           "l.dataObertura <= :dataFi " +
           "AND (l.dataAcabament IS NULL OR l.dataAcabament >= :dataInici)")
    List<Lot> findLotsObertsPerTraçabilitat(
        @Param("dataInici") java.time.LocalDateTime dataInici,
        @Param("dataFi") java.time.LocalDateTime dataFi);

    @Modifying
    @Transactional
    @Query("DELETE FROM Lot l WHERE l.nifProveidor = :nifProveidor AND l.idLot = :idLot AND l.estat = 'EN_ESTOC'")
    int deleteByIdIfEnEstoc(@Param("nifProveidor") String nifProveidor, @Param("idLot") String idLot);
}
