

package cat.copernic.easytraza;

import cat.copernic.easytraza.service.UsuariService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
/**
 * Classe principal d'inici de l'aplicació Spring Boot.
 */
public class EasytrazaApplication implements CommandLineRunner {
    
    @Autowired
    private UsuariService usuariService;
    
    public static void main(String[] args) {
        SpringApplication.run(EasytrazaApplication.class, args);
    }
    
    @Override
    public void run(String... args) throws Exception {
        usuariService.crearSuperAdminSiNoExiste();
    }


}