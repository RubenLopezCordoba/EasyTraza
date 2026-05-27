package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.model.LiniarProveidor;
import cat.copernic.easytraza.model.LiniarProveidorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
/**
 * Repositori JPA per a operacions de persistència de LiniarProveidor.
 */
public interface LiniarProveidorRepository extends JpaRepository<LiniarProveidor, LiniarProveidorId> {

    List<LiniarProveidor> findByNifProveidorAndIdAlbarra(String nifProveidor, String idAlbarra);

    List<LiniarProveidor> findByNifProveidorAndIdLot(String nifProveidor, String idLot);

    @Modifying
    @Transactional
    @Query("DELETE FROM LiniarProveidor l WHERE l.nifProveidor = :nifProveidor AND l.idAlbarra = :idAlbarra")
    void deleteByAlbaraId(@Param("nifProveidor") String nifProveidor, @Param("idAlbarra") String idAlbarra);

    @Modifying
@Transactional
@Query("DELETE FROM LiniarProveidor l WHERE l.nifProveidor = :nifProveidor AND l.idLot = :idLot")
void deleteByLotId(@Param("nifProveidor") String nifProveidor, @Param("idLot") String idLot);
}
