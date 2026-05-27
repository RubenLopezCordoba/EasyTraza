package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.model.PasswordResetToken;
import cat.copernic.easytraza.model.Usuari;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
/**
 * Repositori JPA per a operacions de persistència de PasswordResetToken.
 */
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    Optional<PasswordResetToken> findByCode(String code);

    Optional<PasswordResetToken> findByUsuari(Usuari usuari);

    void deleteByUsuari(Usuari usuari);
}
