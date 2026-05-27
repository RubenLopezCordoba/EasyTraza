package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.model.Control;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositori JPA per a operacions de persistència de Control.
 */
public interface ControlRepository extends JpaRepository<Control, Long> {

    List<Control> findAllByOrderByDataDesc();

    @Query("SELECT c FROM Control c WHERE c.data >= :start AND c.data <= :end ORDER BY c.data DESC")
    List<Control> findByDateRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT c FROM Control c WHERE YEAR(c.data) = :year AND WEEK(c.data) = :week ORDER BY c.data DESC")
    List<Control> findByYearAndWeek(@Param("year") int year, @Param("week") int week);

    Optional<Control> findByData(LocalDate data);
}