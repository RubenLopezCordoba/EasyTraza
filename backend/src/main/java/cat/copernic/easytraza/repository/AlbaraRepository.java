package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.model.AlbarraProveidor;
import cat.copernic.easytraza.model.AlbarraProveidorId;
import cat.copernic.easytraza.model.Provedor;
import cat.copernic.easytraza.model.Usuari;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
/**
 * Repositori JPA per a operacions de persistència d'AlbarraProveidor.
 */
public interface AlbaraRepository extends JpaRepository<AlbarraProveidor, AlbarraProveidorId> {
    List<AlbarraProveidor> findByProvedor(Provedor provedor);
    List<AlbarraProveidor> findByUsuari(Usuari usuari);
    List<AlbarraProveidor> findByDataRecepcioBetween(LocalDateTime inicio, LocalDateTime fin);
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM AlbarraProveidor a WHERE a.idAlbarra = :idAlbarra")
    boolean existsByIdAlbarra(@Param("idAlbarra") String idAlbarra);

    @Query("SELECT DISTINCT a FROM AlbarraProveidor a LEFT JOIN FETCH a.linies")
    List<AlbarraProveidor> findAllWithLinies();

    @Query("SELECT a FROM AlbarraProveidor a JOIN FETCH a.linies WHERE a.nifProveidor = :nifProveidor AND a.idAlbarra = :idAlbarra")
    Optional<AlbarraProveidor> findByIdWithLinies(@Param("nifProveidor") String nifProveidor, @Param("idAlbarra") String idAlbarra);

    @Modifying
    @Transactional
    @Query("DELETE FROM AlbarraProveidor a WHERE a.nifProveidor = :nifProveidor AND a.idAlbarra = :idAlbarra")
    void deleteDirect(@Param("nifProveidor") String nifProveidor, @Param("idAlbarra") String idAlbarra);
}
