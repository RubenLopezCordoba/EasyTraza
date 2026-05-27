package cat.copernic.easytraza.service;

import cat.copernic.easytraza.model.Control;
import cat.copernic.easytraza.model.Usuari;
import cat.copernic.easytraza.repository.ControlRepository;
import cat.copernic.easytraza.repository.UsuariRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
/**
 * Servei per gestionar la logica de negoci de ControlService.
 */
public class ControlService {
    private static final Logger log = LoggerFactory.getLogger(ControlService.class);
@Autowired
    private ControlRepository controlRepository;

    @Autowired
    private UsuariRepository usuariRepository;

    public List<Control> getAllControls() {
        return controlRepository.findAllByOrderByDataDesc();
    }

    public List<Control> getControlsByDateRange(LocalDate start, LocalDate end) {
        return controlRepository.findByDateRange(start, end);
    }

    public Map<String, Object> getWeeklyStatus() {
        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate weekEnd = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        List<Control> weekControls = controlRepository.findByDateRange(weekStart, weekEnd);
        Map<String, Object> result = new HashMap<>();
        result.put("weekStart", weekStart.toString());
        result.put("weekEnd", weekEnd.toString());
        result.put("controls", weekControls);
        result.put("completed", !weekControls.isEmpty());
        return result;
    }

    @Transactional
    public Control createControl(Double ph, Long usuariId, String observacions) {
        LocalDate today = LocalDate.now();
        if (controlRepository.findByData(today).isPresent()) {
            throw new RuntimeException("Ja hi ha un control de pH registrat avui");
        }
        Usuari usuari = usuariRepository.findById(usuariId)
                .orElseThrow(() -> new RuntimeException("Usuari no trobat"));
        Control control = new Control(today, ph, usuari);
        control.setObservacions(observacions);
        return controlRepository.save(control);
    }
}
