package cat.copernic.easytraza.service;

import cat.copernic.easytraza.model.*;
import cat.copernic.easytraza.repository.AlbaraRepository;
import cat.copernic.easytraza.repository.LiniarProveidorRepository;
import cat.copernic.easytraza.repository.LotRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
/**
 * Servei per gestionar la logica de negoci de AlbaraService.
 */
public class AlbaraService {
    private static final Logger log = LoggerFactory.getLogger(AlbaraService.class);
@Autowired
    private AlbaraRepository albaraRepository;

    @Autowired
    private LotRepository lotRepository;

    @Autowired
    private LiniarProveidorRepository liniarProveidorRepository;

    public List<AlbarraProveidor> findAll() {
        return albaraRepository.findAll();
    }

    public Optional<AlbarraProveidor> findById(String nifProveidor, String idAlbarra) {
        return albaraRepository.findById(new AlbarraProveidorId(nifProveidor, idAlbarra));
    }

    public List<AlbarraProveidor> findByUsuari(Long usuariId) {
        return albaraRepository.findAll();
    }

    @Transactional
    public AlbarraProveidor save(AlbarraProveidor albara) {
        if (albara.getDataRecepcio() == null) {
            albara.setDataRecepcio(LocalDateTime.now());
        }
        return albaraRepository.save(albara);
    }

    @Transactional
    public void deleteById(String nifProveidor, String idAlbarra) {
        liniarProveidorRepository.deleteByAlbaraId(nifProveidor, idAlbarra);
        albaraRepository.deleteById(new AlbarraProveidorId(nifProveidor, idAlbarra));
    }
}
