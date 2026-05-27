package cat.copernic.easytraza.repository;

import cat.copernic.easytraza.model.LiniarClientLot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
/**
 * Repositori JPA per a operacions de persistència de LiniarClientLot.
 */
public interface LiniarClientLotRepository extends JpaRepository<LiniarClientLot, Long> {

    List<LiniarClientLot> findByAlbaraNifClientAndAlbaraDataProduccio(String nifClient, LocalDate dataProduccio);

    void deleteByAlbaraNifClientAndAlbaraDataProduccio(String nifClient, LocalDate dataProduccio);

    List<LiniarClientLot> findByLotNifProveidorAndLotIdLot(String nifProveidor, String idLot);
}
