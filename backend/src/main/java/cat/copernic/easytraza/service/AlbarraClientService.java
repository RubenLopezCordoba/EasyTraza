package cat.copernic.easytraza.service;

import cat.copernic.easytraza.model.*;
import cat.copernic.easytraza.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
/**
 * Servei per gestionar la logica de negoci de AlbarraClientService.
 */
public class AlbarraClientService {
    private static final Logger log = LoggerFactory.getLogger(AlbarraClientService.class);
    @PersistenceContext
    private EntityManager entityManager;
    @Autowired
    private AlbarraClientRepository albarraClientRepository;
    
     @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ProducteRepository producteRepository;

    @Autowired
    private UsuariRepository usuariRepository;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private LiniarClientLotRepository liniarClientLotRepository;

    @Autowired
    private cat.copernic.easytraza.repository.LiniarClientRepository liniarClientRepository;

    public List<AlbarraClient> getAllAlbarans() {
        return albarraClientRepository.findAllByOrderByDataProduccioDesc();
    }

    @Transactional
    public AlbarraClient getAlbaraById(String nifClient, LocalDate dataProduccio) {
        AlbarraClient albara = albarraClientRepository.findByIdWithLinies(nifClient, dataProduccio)
                .orElseThrow(() -> new RuntimeException("Albarà de client no trobat: " + nifClient + "/" + dataProduccio));
        
        if (!"LLIURAT".equals(albara.getEstat())) {
            LocalDateTime dataFi = albara.getDataLliurament() != null 
                    ? albara.getDataLliurament() 
                    : albara.getDataProduccio().atTime(23, 59, 59);
            List<Lot> lotsActuals = lotRepository.findLotsObertsPerTraçabilitat(
                    albara.getDataProduccio().atStartOfDay(),
                    dataFi
            );
            liniarClientLotRepository.deleteByAlbaraNifClientAndAlbaraDataProduccio(nifClient, dataProduccio);
            for (Lot lot : lotsActuals) {
                liniarClientLotRepository.save(new LiniarClientLot(albara, lot));
            }
            entityManager.flush();
            entityManager.clear();
            albara = albarraClientRepository.findByIdWithLinies(nifClient, dataProduccio)
                    .orElseThrow(() -> new RuntimeException("Albarà de client no trobat: " + nifClient + "/" + dataProduccio));
        }
        return albara;
    }

    @Transactional
    public void recalcularLotsAssociats(String nifClient, LocalDate dataProduccio) {
        AlbarraClient albara = albarraClientRepository.findById(new AlbarraClientId(nifClient, dataProduccio))
                .orElseThrow(() -> new RuntimeException("Albarà no trobat"));
        if ("LLIURAT".equals(albara.getEstat())) return;
        List<Lot> lotsActuals = lotRepository.findLotsObertsPerTraçabilitat(
                albara.getDataProduccio().atStartOfDay(),
                albara.getDataLliurament() != null ? albara.getDataLliurament() : LocalDateTime.now()
        );
        liniarClientLotRepository.deleteByAlbaraNifClientAndAlbaraDataProduccio(nifClient, dataProduccio);
        for (Lot lot : lotsActuals) {
            liniarClientLotRepository.save(new LiniarClientLot(albara, lot));
        }
    }

    private List<Lot> getLotsOberts() {
        return lotRepository.findLotsOberts();
    }

@Transactional
    public AlbarraClient createAlbara(AlbarraClient albara, List<LineaRequest> linies) {
        Optional<AlbarraClient> existing = albarraClientRepository.findById(
            new AlbarraClientId(albara.getNifClient(), albara.getDataProduccio()));
        if (existing.isPresent()) {
            throw new RuntimeException("Ja existeix un albarà per a aquest client i data");
        }
        if (linies != null) {
            Set<Long> productesAfegits = new java.util.HashSet<>();
            for (LineaRequest lr : linies) {
                if (productesAfegits.contains(lr.producteId)) {
                    throw new RuntimeException("Producte duplicat a l'albarà");
                }
                productesAfegits.add(lr.producteId);
            }
        }
        Client client = clientRepository.findById(albara.getNifClient())
                .orElseThrow(() -> new RuntimeException("Client no trobat"));
        Usuari operari = usuariRepository.findById(albara.getOperari().getId())
                .orElseThrow(() -> new RuntimeException("Operari no trobat"));
        AlbarraClient newAlbara = new AlbarraClient(client, albara.getDataProduccio(), operari);
        if (albara.getDataLliurament() != null) {
            newAlbara.setDataLliurament(albara.getDataLliurament());
        } else {
            newAlbara.setDataLliurament(albara.getDataProduccio().atTime(23, 59, 59));
        }
        AlbarraClient saved = albarraClientRepository.saveAndFlush(newAlbara);
        entityManager.clear();
        if (linies != null) {
            List<Lot> lotsPerTraçabilitat = lotRepository.findLotsObertsPerTraçabilitat(
                    saved.getDataProduccio().atStartOfDay(),
                    saved.getDataLliurament()
            );
            for (LineaRequest lr : linies) {
                Producte producte = producteRepository.findById(lr.producteId)
                        .orElseThrow(() -> new RuntimeException("Producte no trobat: " + lr.producteId));
                if (lr.quantitat <= 0) throw new RuntimeException("La quantitat ha de ser superior a zero");
                LiniarClient lc = new LiniarClient();
                lc.setNifClient(saved.getNifClient());
                lc.setDataProduccio(saved.getDataProduccio());
                lc.setIdProducte(lr.producteId);
                lc.setQuantitat(lr.quantitat);
                lc.setProducte(producte);
                lc.setAlbarraClient(saved);
                liniarClientRepository.save(lc);
            }
            liniarClientLotRepository.deleteByAlbaraNifClientAndAlbaraDataProduccio(saved.getNifClient(), saved.getDataProduccio());
            for (Lot lot : lotsPerTraçabilitat) {
                liniarClientLotRepository.save(new LiniarClientLot(saved, lot));
            }
        }
        return saved;
    }

    @Transactional
    public AlbarraClient updateAlbara(String nifClient, LocalDate dataProduccio, AlbarraClient albara, List<LineaRequest> linies) {
        AlbarraClient existing = getAlbaraById(nifClient, dataProduccio);
        if (!"NO_LLIURAT".equals(existing.getEstat()))
            throw new RuntimeException("Només es poden modificar albarans no lliurats");
        LocalDateTime existingDataLliurament = existing.getDataLliurament();
        if (linies != null) {
            Set<Long> productesAfegits = new java.util.HashSet<>();
            for (LineaRequest lr : linies) {
                if (productesAfegits.contains(lr.producteId)) {
                    throw new RuntimeException("Producte duplicat a l'albarà");
                }
                productesAfegits.add(lr.producteId);
            }
        }
        Client client = clientRepository.findById(albara.getNifClient())
                .orElseThrow(() -> new RuntimeException("Client no trobat"));
        String newNif = client.getNif();
        LocalDate newData = albara.getDataProduccio();
        boolean keyChanged = !nifClient.equals(newNif) || !dataProduccio.equals(newData);
        AlbarraClient newAlbara;
        if (keyChanged) {
            deleteAlbara(nifClient, dataProduccio);
            newAlbara = new AlbarraClient(client, newData, null);
        } else {
            Usuari operariOriginal = existing.getOperari();
            entityManager.remove(existing);
            entityManager.flush();
            entityManager.clear();
            newAlbara = new AlbarraClient(client, newData, operariOriginal);
        }
        newAlbara.setDataLliurament(
            albara.getDataLliurament() != null ? albara.getDataLliurament() : existingDataLliurament
        );
        if (albara.getOperari() != null && albara.getOperari().getId() != null) {
            Usuari operari = usuariRepository.findById(albara.getOperari().getId())
                    .orElseThrow(() -> new RuntimeException("Operari no trobat"));
            newAlbara.setOperari(operari);
        }
AlbarraClient saved = albarraClientRepository.saveAndFlush(newAlbara);
        entityManager.clear();
        if (linies != null) {
            List<Lot> lotsPerTraçabilitat = lotRepository.findLotsObertsPerTraçabilitat(
                    saved.getDataProduccio().atStartOfDay(),
                    saved.getDataLliurament() != null ? saved.getDataLliurament() : saved.getDataProduccio().atTime(23, 59, 59)
            );
            for (LineaRequest lr : linies) {
                Producte producte = producteRepository.findById(lr.producteId)
                        .orElseThrow(() -> new RuntimeException("Producte no trobat: " + lr.producteId));
                if (lr.quantitat <= 0) throw new RuntimeException("La quantitat ha de ser superior a zero");
                LiniarClient lc = new LiniarClient();
                lc.setNifClient(saved.getNifClient());
                lc.setDataProduccio(saved.getDataProduccio());
                lc.setIdProducte(lr.producteId);
                lc.setQuantitat(lr.quantitat);
                lc.setProducte(producte);
                lc.setAlbarraClient(saved);
                liniarClientRepository.save(lc);
            }
            liniarClientLotRepository.deleteByAlbaraNifClientAndAlbaraDataProduccio(saved.getNifClient(), saved.getDataProduccio());
            for (Lot lot : lotsPerTraçabilitat) {
                liniarClientLotRepository.save(new LiniarClientLot(saved, lot));
            }
        }
        return saved;
    }

   @Transactional
public void deleteAlbara(String nifClient, LocalDate dataProduccio) {
    try {
        log.info("Intentant eliminar albarà: nifClient={}, dataProduccio={}", nifClient, dataProduccio);
        
        AlbarraClient albara = getAlbaraById(nifClient, dataProduccio);
        
        log.info("Albarà trobat: estat={}, nifClient={}, data={}", albara.getEstat(), nifClient, dataProduccio);
        
        if (!"NO_LLIURAT".equals(albara.getEstat())) {
            throw new RuntimeException("Només es poden eliminar albarans no lliurats. Estat actual: " + albara.getEstat());
        }
        
        // Primer eliminar los lots asociados
        log.info("Eliminant lots associats per a: {}/{}", nifClient, dataProduccio);
        liniarClientLotRepository.deleteByAlbaraNifClientAndAlbaraDataProduccio(nifClient, dataProduccio);
        
        // Luego eliminar las líneas (si no están en cascada)
        // Nota: Si tienes cascade en la relación, esto puede no ser necesario
        
        // Finalmente eliminar el albarán
        log.info("Eliminant albarà: {}/{}", nifClient, dataProduccio);
        albarraClientRepository.deleteById(new AlbarraClientId(nifClient, dataProduccio));
        
        log.info("Albarà eliminat correctament: {}/{}", nifClient, dataProduccio);
        
    } catch (Exception e) {
        log.error("Error al eliminar albarà: {}", e.getMessage(), e);
        throw new RuntimeException("No s'ha pogut eliminar l'albarà: " + e.getMessage(), e);
    }
}

@Transactional
    public AlbarraClient marcarLliurat(String nifClient, LocalDate dataProduccio) {
        AlbarraClient albara = getAlbaraById(nifClient, dataProduccio);
        albara.setEstat("LLIURAT");
        LocalDateTime dataLliurament;
        if (albara.getDataLliurament() != null) {
            dataLliurament = albara.getDataLliurament();
        } else {
            dataLliurament = albara.getDataProduccio().atTime(23, 59, 59);
        }
        albara.setDataLliurament(dataLliurament);
        AlbarraClient saved = albarraClientRepository.save(albara);
        entityManager.flush();
        entityManager.clear();
        List<Lot> lotsPerTraçabilitat = lotRepository.findLotsObertsPerTraçabilitat(
                saved.getDataProduccio().atStartOfDay(),
                dataLliurament
        );
        liniarClientLotRepository.deleteByAlbaraNifClientAndAlbaraDataProduccio(saved.getNifClient(), saved.getDataProduccio());
        for (Lot lot : lotsPerTraçabilitat) {
            liniarClientLotRepository.save(new LiniarClientLot(saved, lot));
        }
        return saved;
    }

    public static class LineaRequest {
        public Long producteId;
        public Double quantitat;
    }
}
