package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.model.LiniarClient;
import cat.copernic.easytraza.model.LiniarClientId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LiniarClientRepository extends JpaRepository<LiniarClient, LiniarClientId> {

    List<LiniarClient> findByDataProduccioBetween(LocalDate start, LocalDate end);

    List<LiniarClient> findByIdProducte(Long idProducte);

    @Query("SELECT l FROM LiniarClient l WHERE l.dataProduccio BETWEEN :start AND :end AND l.idProducte = :idProducte")
    List<LiniarClient> findByDataProduccioBetweenAndIdProducte(
        @Param("start") LocalDate start, 
        @Param("end") LocalDate end, 
        @Param("idProducte") Long idProducte
    );
}