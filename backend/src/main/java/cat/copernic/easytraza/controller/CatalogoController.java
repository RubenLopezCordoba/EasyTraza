package cat.copernic.easytraza.controller;

import cat.copernic.easytraza.model.MateriaPrimera;
import cat.copernic.easytraza.model.Producte;
import cat.copernic.easytraza.model.Rol;
import cat.copernic.easytraza.model.Usuari;
import cat.copernic.easytraza.repository.LotRepository;
import cat.copernic.easytraza.repository.MateriaPrimeraRepository;
import cat.copernic.easytraza.repository.ProducteRepository;
import cat.copernic.easytraza.service.ProducteService;
import cat.copernic.easytraza.utils.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequestMapping("/catalogo")
/**
 * Controlador REST per gestionar operacions relacionades amb CatalogoController.
 */
public class CatalogoController {
    private static final Logger log = LoggerFactory.getLogger(CatalogoController.class);
@Autowired
    private ProducteService producteService;

    @Autowired
    private ProducteRepository producteRepository;

    @Autowired
    private MateriaPrimeraRepository materiaPrimeraRepository;

    @Autowired
    private LotRepository lotRepository;

    @GetMapping
    /**
     * Executa l'operació listarCatalogo.
     */
    public String listarCatalogo(Model model, HttpSession session) {
        Usuari u = (Usuari) session.getAttribute("usuario");
        if (u != null && Rol.TRABAJADOR.name().equals(u.getRol())) return "redirect:/dashboard";
        model.addAttribute("ingredientes", materiaPrimeraRepository.findAll());
        model.addAttribute("productos", producteRepository.findAll());
        return "catalogo";
    }

    @GetMapping("/api/listar")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> listarApi() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (MateriaPrimera mp : materiaPrimeraRepository.findAll()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", mp.getId());
            item.put("nombre", mp.getNombre());
            item.put("descripcion", mp.getDescripcio());
            item.put("tipo", "INGREDIENTE");
            result.add(item);
        }
        for (Producte p : producteRepository.findAll()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", p.getId());
            item.put("nombre", p.getNombre());
            item.put("descripcion", p.getDescripcio());
            item.put("tipo", "PRODUCTO");
            result.add(item);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/api/ingredientes")
    @ResponseBody
    /**
     * Obté dades relacionades amb Ingredientes.
     */
    public ResponseEntity<List<MateriaPrimera>> getIngredientes() {
        return ResponseEntity.ok(materiaPrimeraRepository.findAll());
    }

    @GetMapping("/api/productos")
    @ResponseBody
    /**
     * Obté dades relacionades amb Productos.
     */
    public ResponseEntity<List<Producte>> getProductos() {
        return ResponseEntity.ok(producteRepository.findAll());
    }

    @GetMapping("/api/{id}")
    @ResponseBody
    /**
     * Executa l'operació obtenerItem.
     */
    public ResponseEntity<?> obtenerItem(@PathVariable Long id) {
        Optional<Producte> p = producteRepository.findById(id);
        if (p.isPresent()) {
            Producte prod = p.get();
            Map<String, Object> item = new HashMap<>();
            item.put("id", prod.getId());
            item.put("nombre", prod.getNombre());
            item.put("descripcion", prod.getDescripcio());
            item.put("tipo", "PRODUCTO");
            return ResponseEntity.ok(item);
        }
        Optional<MateriaPrimera> m = materiaPrimeraRepository.findById(id);
        if (m.isPresent()) {
            MateriaPrimera mp = m.get();
            Map<String, Object> item = new HashMap<>();
            item.put("id", mp.getId());
            item.put("nombre", mp.getNombre());
            item.put("descripcion", mp.getDescripcio());
            item.put("tipo", "INGREDIENTE");
            return ResponseEntity.ok(item);
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/api/guardar")
    @ResponseBody
    /**
     * Executa l'operació guardarItem.
     */
    public ResponseEntity<?> guardarItem(@RequestBody Map<String, Object> body, HttpSession session) {
        try {
            Usuari usuariActual = (Usuari) session.getAttribute("usuario");
            if (usuariActual == null || Rol.TRABAJADOR.name().equals(usuariActual.getRol()))
                return ResponseUtil.forbidden("No tens permís per crear items");
            String tipo = (String) body.get("tipo");
            String nom = (String) body.get("nombre");
            String descripcio = (String) body.get("descripcion");

            if (!materiaPrimeraRepository.findByNomIgnoreCase(nom).isEmpty() ||
                !producteRepository.findByNomIgnoreCase(nom).isEmpty()) {
                return ResponseUtil.error("Ja existeix un element amb aquest nom al catàleg");
            }
            if ("INGREDIENTE".equals(tipo)) {
                MateriaPrimera mp = new MateriaPrimera();
                mp.setNom(nom);
                mp.setDescripcio(descripcio);
                materiaPrimeraRepository.save(mp);
            } else {
                Producte p = new Producte();
                p.setNom(nom);
                p.setDescripcio(descripcio);
                producteRepository.save(p);
            }
            return ResponseUtil.ok("Ítem guardado correctamente");
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseUtil.error("S'ha produït un error inesperat");
        }
    }

    @PutMapping("/api/actualizar/{id}")
    @ResponseBody
    /**
     * Executa l'operació actualizarItem.
     */
    public ResponseEntity<?> actualizarItem(@PathVariable Long id, @RequestBody Map<String, Object> body, HttpSession session) {
        try {
            Usuari u = (Usuari) session.getAttribute("usuario");
            if (u == null || Rol.TRABAJADOR.name().equals(u.getRol()))
                return ResponseUtil.forbidden("No tens permís per actualizar items");
            String nom = (String) body.get("nombre");
            String descripcio = (String) body.get("descripcion");
            String tipo = (String) body.get("tipo");

            boolean existsInMaterias = materiaPrimeraRepository.findByNomIgnoreCase(nom).stream()
                .anyMatch(m -> !m.getId().equals(id));
            boolean existsInProductes = producteRepository.findByNomIgnoreCase(nom).stream()
                .anyMatch(p -> !p.getId().equals(id));
            if (existsInMaterias || existsInProductes) {
                return ResponseUtil.error("Ja existeix un element amb aquest nom al catàleg");
            }
            if ("INGREDIENTE".equals(tipo)) {
                MateriaPrimera mp = materiaPrimeraRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Ingredient no trobat"));
                mp.setNom(nom);
                mp.setDescripcio(descripcio);
                materiaPrimeraRepository.save(mp);
            } else {
                Producte p = producteRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Producte no trobat"));
                p.setNom(nom);
                p.setDescripcio(descripcio);
                producteRepository.save(p);
            }
            return ResponseUtil.ok("Ítem actualizado correctamente");
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseUtil.error("S'ha produït un error inesperat");
        }
    }

    @DeleteMapping("/api/eliminar/{id}")
    @ResponseBody
    /**
     * Executa l'operació eliminarItem.
     */
    public ResponseEntity<?> eliminarItem(@PathVariable Long id, HttpSession session) {
        try {
            Usuari u = (Usuari) session.getAttribute("usuario");
            if (u == null || Rol.TRABAJADOR.name().equals(u.getRol()))
                return ResponseUtil.forbidden("No tens permís per eliminar items");
            if (producteRepository.existsById(id)) {
                producteService.deleteById(id);
            } else if (materiaPrimeraRepository.existsById(id)) {
                var lots = lotRepository.findByMateriaPrimeraId(id);
                if (!lots.isEmpty()) {
                    return ResponseUtil.error("No es pot eliminar: " + lots.size() + " lots utilitzen aquesta matèria primera");
                }
                materiaPrimeraRepository.deleteById(id);
            } else {
                return ResponseUtil.error("Ítem no trobat");
            }
            return ResponseUtil.ok("Ítem eliminado correctamente");
        } catch (RuntimeException e) {
            log.error("Error", e);
            return ResponseUtil.error(e.getMessage());
        } catch (Exception e) {
            log.error("Error", e);
            return ResponseUtil.error("S'ha produït un error inesperat");
        }
    }
}
