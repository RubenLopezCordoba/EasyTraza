package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.service.AlbarraClientService;
import cat.copernic.easytraza.model.*;
import cat.copernic.easytraza.service.ClientService;
import cat.copernic.easytraza.utils.ResponseUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api")
public class AlbarraClientController {
    private static final Logger log = LoggerFactory.getLogger(AlbarraClientController.class);
    
    @Autowired
    private AlbarraClientService service;
    @Autowired
    private ClientService clientService;

    @GetMapping("/clients")
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/clients/{nif}")
    public ResponseEntity<Client> getClient(@PathVariable String nif) {
        return clientService.getClientById(nif)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/clients")
    public ResponseEntity<?> createClient(@RequestBody Client client, HttpSession session) {
        try {
            Usuari u = (Usuari) session.getAttribute("usuario");
            if (u == null || Rol.TRABAJADOR.name().equals(u.getRol()))
                return ResponseUtil.forbidden("No tens permís");
            return ResponseEntity.ok(clientService.createClient(client));
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @PutMapping("/clients/{nif}")
    public ResponseEntity<?> updateClient(@PathVariable String nif, @RequestBody Client client, HttpSession session) {
        try {
            Usuari u = (Usuari) session.getAttribute("usuario");
            if (u == null || Rol.TRABAJADOR.name().equals(u.getRol()))
                return ResponseUtil.forbidden("No tens permís");
            return ResponseEntity.ok(clientService.updateClient(nif, client));
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseUtil.error("S'ha produït un error inesperat");
        }
    }

    @DeleteMapping("/clients/{nif}")
    public ResponseEntity<?> deleteClient(@PathVariable String nif, HttpSession session) {
        try {
            Usuari u = (Usuari) session.getAttribute("usuario");
            if (u == null || Rol.TRABAJADOR.name().equals(u.getRol()))
                return ResponseUtil.forbidden("No tens permís");
            clientService.deleteClient(nif);
            return ResponseUtil.ok("Client eliminat");
        } catch (DataIntegrityViolationException e) {
            return ResponseUtil.error("No es pot eliminar: té albarans associats");
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseUtil.error("S'ha produït un error inesperat");
        }
    }

    @GetMapping("/client-albarans")
    public ResponseEntity<List<AlbarraClient>> getAllAlbarans() {
        return ResponseEntity.ok(service.getAllAlbarans());
    }

    @GetMapping("/client-albarans/{nifClient}/{dataProduccio}")
    public ResponseEntity<AlbarraClient> getAlbara(@PathVariable String nifClient, @PathVariable String dataProduccio) {
        return ResponseEntity.ok(service.getAlbaraById(nifClient, LocalDate.parse(dataProduccio)));
    }

    @PostMapping("/client-albarans")
    public ResponseEntity<?> createAlbara(HttpSession session, @RequestBody CreateAlbaraRequest request) {
        try {
            request.albara.setOperari(getSessionUser(session));
            return ResponseUtil.ok("Albarà creat", "albara", service.createAlbara(request.albara, request.linies));
        } catch (Exception e) {
            log.error("Error al crear albarà client", e);
            return ResponseUtil.error(e.getMessage());
        }
    }

    @PutMapping("/client-albarans/{nifClient}/{dataProduccio}")
    public ResponseEntity<?> updateAlbara(HttpSession session, @PathVariable String nifClient, 
                                         @PathVariable String dataProduccio, 
                                         @RequestBody CreateAlbaraRequest request) {
        try {
            request.albara.setOperari(getSessionUser(session));
            return ResponseUtil.ok("Albarà actualitzat", "albara", 
                service.updateAlbara(nifClient, LocalDate.parse(dataProduccio), request.albara, request.linies));
        } catch (Exception e) {
            log.error("Error al actualitzar albarà client", e);
            return ResponseUtil.error(e.getMessage());
        }
    }

    @PutMapping("/client-albarans/{nifClient}/{dataProduccio}/marcar-lliurat")
    public ResponseEntity<?> marcarLliurat(@PathVariable String nifClient, @PathVariable String dataProduccio) {
        try {
            return ResponseUtil.ok("Marcat com a LLIURAT", "albara", 
                service.marcarLliurat(nifClient, LocalDate.parse(dataProduccio)));
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseUtil.error("S'ha produït un error inesperat");
        }
    }

    @DeleteMapping("/client-albarans/{nifClient}/{dataProduccio}")
    public ResponseEntity<?> deleteAlbara(@PathVariable String nifClient, @PathVariable String dataProduccio) {
        try {
            service.deleteAlbara(nifClient, LocalDate.parse(dataProduccio));
            return ResponseUtil.ok("Albarà eliminat");
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseUtil.error("S'ha produït un error inesperat");
        }
    }

    @PutMapping("/client-albarans/{nifClient}/{dataProduccio}/linies/{producteId}")
    public ResponseEntity<?> updateLinia(@PathVariable String nifClient, 
                                        @PathVariable String dataProduccio, 
                                        @PathVariable Long producteId,  // ← Cambiado de lineaId a producteId
                                        @RequestBody Map<String, Object> body) {
        try {
            AlbarraClient a = service.getAlbaraById(nifClient, LocalDate.parse(dataProduccio));
            // ← Cambiado de getIdProducte() a getProducteId()
            LiniarClient linia = a.getLinies().stream()
                    .filter(l -> l.getIdProducte().equals(producteId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Línia no trobada"));
            if (body.containsKey("quantitat")) {
                linia.setQuantitat(((Number) body.get("quantitat")).doubleValue());
            }
            List<AlbarraClientService.LineaRequest> remaining = a.getLinies().stream()
                .map(l -> {
                    AlbarraClientService.LineaRequest lr = new AlbarraClientService.LineaRequest();
                    lr.producteId = l.getIdProducte();
                    lr.quantitat = l.getQuantitat();
                    return lr;
                })
                .collect(java.util.stream.Collectors.toList());
            service.updateAlbara(nifClient, LocalDate.parse(dataProduccio), a, remaining);
            return ResponseUtil.ok("Línia actualitzada");
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseUtil.error("S'ha produït un error inesperat");
        }
    }

    @DeleteMapping("/client-albarans/{nifClient}/{dataProduccio}/linies/{producteId}")
    public ResponseEntity<?> deleteLinia(@PathVariable String nifClient, 
                                        @PathVariable String dataProduccio, 
                                        @PathVariable Long producteId) {
        try {
            AlbarraClient a = service.getAlbaraById(nifClient, LocalDate.parse(dataProduccio));
            a.getLinies().removeIf(l -> l.getIdProducte().equals(producteId));
            List<AlbarraClientService.LineaRequest> remaining = a.getLinies().stream()
                .map(l -> {
                    AlbarraClientService.LineaRequest lr = new AlbarraClientService.LineaRequest();
                    lr.producteId = l.getIdProducte();
                    lr.quantitat = l.getQuantitat();
                    return lr;
                })
                .collect(java.util.stream.Collectors.toList());
            service.updateAlbara(nifClient, LocalDate.parse(dataProduccio), a, remaining);
            return ResponseUtil.ok("Línia eliminada");
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseUtil.error("S'ha produït un error inesperat");
        }
    }

    private Usuari getSessionUser(HttpSession session) {
        Usuari u = (Usuari) session.getAttribute("usuario");
        if (u == null) {
            throw new RuntimeException("Usuari no autenticat");
        }
        return u;
    }

    public static class CreateAlbaraRequest {
        public AlbarraClient albara;
        public List<AlbarraClientService.LineaRequest> linies;
    }
}