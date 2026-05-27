package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.model.Usuari;
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
 * Repositori JPA per a operacions de persistència d'Usuari.
 */
public interface UsuariRepository extends JpaRepository<Usuari, Long> {

    Optional<Usuari> findByEmail(String email);

    List<Usuari> findByRol(String rol);

    List<Usuari> findByActivoTrue();

    boolean existsByEmail(String email);

    boolean existsByNif(String nif);

    Optional<Usuari> findByNif(String nif);

    long countByRol(String rol);

    @Modifying
    @Transactional
    @Query("UPDATE Usuari u SET u.fotoUrl = :fotoUrl WHERE u.id = :id")
    int updateFotoUrl(@Param("id") Long id, @Param("fotoUrl") String fotoUrl);
}
