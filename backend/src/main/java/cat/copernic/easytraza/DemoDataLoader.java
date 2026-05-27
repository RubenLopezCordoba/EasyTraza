/*
 * Demo data loader for EasyTraza project demonstration.
 * To reset demo data: drop the database and restart the application.
 * To disable: delete or comment this file.
 */

package cat.copernic.easytraza;

import cat.copernic.easytraza.model.*;
import cat.copernic.easytraza.repository.*;
import cat.copernic.easytraza.utils.PasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.nio.file.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;

@Component
@Order(1)
public class DemoDataLoader implements CommandLineRunner {

    @Autowired private UsuariRepository usuariRepository;
    @Autowired private ProvedorRepository provedorRepository;
    @Autowired private ClientRepository clientRepository;
    @Autowired private MateriaPrimeraRepository materiaPrimeraRepository;
    @Autowired private ProducteRepository producteRepository;
    @Autowired private LotRepository lotRepository;
    @Autowired private AlbaraRepository albaraRepository;
    @Autowired private AlbarraClientRepository albaraClientRepository;
    @Autowired private LiniarProveidorRepository liniarProveidorRepository;
    @Autowired private LiniarClientRepository liniarClientRepository;
    @Autowired private LiniarClientLotRepository liniarClientLotRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private boolean dadesCreated = false;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (usuariRepository.count() > 1) {
            System.out.println("[DemoData] Dades demo ja existeixen, saltant...");
            return;
        }
        if (dadesCreated) {
            return;
        }

        System.out.println("==============================================");
        System.out.println("CREANT DADES DEMO PER EASYTRAZA...");
        System.out.println("==============================================");

        crearUsuaris();
        crearProveïdors();
        crearClients();
        crearMateriesPrimeres();
        crearProductes();
        crearLots();
        crearAlbaransProveidor();
        crearAlbaransClient();
        crearPasswordTxt();

        dadesCreated = true;
        System.out.println("==============================================");
        System.out.println("DADES DEMO CREADES AMB EXIT!");
        System.out.println("==============================================");
    }

    private void crearUsuaris() {
        System.out.println("[DemoData] Creant usuaris...");

        Usuari admin1 = new Usuari();
        admin1.setNombre("Super Admin");
        admin1.setEmail("superadmin@easytraza.com");
        admin1.setPassword(passwordEncoder.encode("admin123"));
        admin1.setRol("SUPER_ADMIN");
        admin1.setEsAdmin(true);
        admin1.setTelefono("600000001");
        admin1.setNif("00000000T");
        admin1.setActivo(true);
        admin1.setFechaCreacion(LocalDateTime.now());
        admin1.setPasswordCambiada(false);
        usuariRepository.save(admin1);

        Usuari admin2 = new Usuari();
        admin2.setNombre("Administrador");
        admin2.setEmail("admin@easytraza.com");
        admin2.setPassword(passwordEncoder.encode("Admin123"));
        admin2.setRol("ADMIN");
        admin2.setEsAdmin(true);
        admin2.setTelefono("600000002");
        admin2.setNif("00000001R");
        admin2.setActivo(true);
        admin2.setFechaCreacion(LocalDateTime.now());
        admin2.setPasswordCambiada(false);
        usuariRepository.save(admin2);

        Usuari op1 = new Usuari();
        op1.setNombre("Juan Pérez");
        op1.setEmail("operador1@easytraza.com");
        op1.setPassword(passwordEncoder.encode("Op12345"));
        op1.setRol("TRABAJADOR");
        op1.setEsAdmin(false);
        op1.setTelefono("600000003");
        op1.setNif("00000002W");
        op1.setActivo(true);
        op1.setFechaCreacion(LocalDateTime.now());
        op1.setPasswordCambiada(false);
        usuariRepository.save(op1);

        Usuari op2 = new Usuari();
        op2.setNombre("María López");
        op2.setEmail("operador2@easytraza.com");
        op2.setPassword(passwordEncoder.encode("Op12345"));
        op2.setRol("TRABAJADOR");
        op2.setEsAdmin(false);
        op2.setTelefono("600000004");
        op2.setNif("00000003A");
        op2.setActivo(true);
        op2.setFechaCreacion(LocalDateTime.now());
        op2.setPasswordCambiada(false);
        usuariRepository.save(op2);

        System.out.println("[DemoData]   4 usuaris creats");
    }

    private void crearProveïdors() {
        System.out.println("[DemoData] Creant proveïdors...");

        Provedor p1 = new Provedor();
        p1.setNif("U03650785");
        p1.setNombre("Distribucions García SL");
        p1.setDireccion("Carrer Major 15, Barcelona");
        p1.setTelefono("930000001");
        provedorRepository.save(p1);

        Provedor p2 = new Provedor();
        p2.setNif("D03651585");
        p2.setNombre("Mercat Fresc SL");
        p2.setDireccion("Avda del Port 32, Tarragona");
        p2.setTelefono("930000002");
        provedorRepository.save(p2);

        Provedor p3 = new Provedor();
        p3.setNif("Q7258758G");
        p3.setNombre("Proveïments del Nord SA");
        p3.setDireccion("Carrer de la Mer 8, Girona");
        p3.setTelefono("930000003");
        provedorRepository.save(p3);

        Provedor p4 = new Provedor();
        p4.setNif("V36593846");
        p4.setNombre("Laticinis del Vallès SL");
        p4.setDireccion("Polígon Industrial 5, Sabadell");
        p4.setTelefono("930000004");
        provedorRepository.save(p4);

        Provedor p5 = new Provedor();
        p5.setNif("R4790605B");
        p5.setNombre("Total Food Supply SL");
        p5.setDireccion("Carrer del Llac 12, Lleida");
        p5.setTelefono("930000005");
        provedorRepository.save(p5);

        Provedor p6 = new Provedor();
        p6.setNif("C74638693");
        p6.setNombre("Fruites del Mediterrani SL");
        p6.setDireccion("Avda del Mar 44, Valencia");
        p6.setTelefono("930000006");
        provedorRepository.save(p6);

        System.out.println("[DemoData]   6 proveïdors creats");
    }

    private void crearClients() {
        System.out.println("[DemoData] Creant clients...");

        Client c1 = new Client();
        c1.setNif("40603576C");
        c1.setNom("Supermercats");
        c1.setCognoms("MATA SL");
        c1.setAdreca("Carrer Major 100, Barcelona");
        c1.setTelefon("930100001");
        c1.setEmail("supermercatsmata@email.com");
        c1.setActivo(true);
        clientRepository.save(c1);

        Client c2 = new Client();
        c2.setNif("41580707L");
        c2.setNom("Botiga del");
        c2.setCognoms("Barri SL");
        c2.setAdreca("Carrer Nou 25, Tarragona");
        c2.setTelefon("930100002");
        c2.setEmail("botigadelbarri@email.com");
        c2.setActivo(true);
        clientRepository.save(c2);

        Client c3 = new Client();
        c3.setNif("47510987W");
        c3.setNom("Distribuidora Costa");
        c3.setCognoms("Daurada SL");
        c3.setAdreca("Avda de la Costa 78, Girona");
        c3.setTelefon("930100003");
        c3.setEmail("districosta@email.com");
        c3.setActivo(true);
        clientRepository.save(c3);

        System.out.println("[DemoData]   3 clients creats");
    }

    private void crearMateriesPrimeres() {
        System.out.println("[DemoData] Creant matèries primeres...");

        String[][] materies = {
            {"Sal refinada iodada", "Sal refinada amb iode per consum humà"},
            {"Sal marina", "Sal marina natural sense tractar"},
            {"Sucre blanc", "Sucre refinat blanc"},
            {"Sucre morè", "Sucre de canya integral"},
            {"Farina de forment", "Farina de trigo blanch"},
            {"Farina de mill", "Farina per a pa sense gluten"},
            {"Oli d'oliva verge", "Oli d'oliva verge extra primer premsada"},
            {"Oli de gira-sol", "Oli de gira-sol refinat"},
            {"Llet pasteuritzada", "Llet fresca pasteuritzada 3,5% greix"},
            {"Mantega", "Mantega verge de llet de vaca"},
            {"Tomàquet triturat", "Tomàquet natural triturat"},
            {"Ceba", "Ceba blanca de temporada"},
            {"All", "All blanc segarí"},
            {"Pebrot Vermell", "Pebrot vermell dolç"},
            {"Comí", "Comí mòlt per a especejaments"},
            {"Pebre roig", "Pebre roig de la Vera"},
            {"Xocolate negre 70%", "Xocolate negre 70% cacau"},
            {"Cacau en pols", "Cacau magro en pols"},
            {"Nou moscada", "Nou moscada mòlta"},
            {"Canela", "Canela en bressol mòlta"}
        };

        for (String[] mp : materies) {
            MateriaPrimera m = new MateriaPrimera();
            m.setNom(mp[0]);
            m.setDescripcio(mp[1]);
            materiaPrimeraRepository.save(m);
        }

        System.out.println("[DemoData]   20 matèries primeres creades");
    }

    private void crearProductes() {
        System.out.println("[DemoData] Creant productes finals...");

        String[][] productes = {
            {"Pa de pagès", "Pa rústic tradicional, 450g"},
            {"Barra francesa", "Pa allargat cruixent, 250g"},
            {"Pa de motlle", "Pa tou per torrar, 500g"},
            {"Flauta", "Pa llarg i prim, 200g"},
            {"Croissant", "Pasta de full amb mantega, 80g"},
            {"Ensaimada", "Cobertura Sucre i farina, 100g"},
            {"Panettone", "Pa dolç amb nabius i fruites, 500g"},
            {"Pa integral", "Farina integral i llavors, 450g"},
            {"Magdalena", "Berenar dolç individual, 50g"},
            {"Teulat", "Pa tipus català rustit, 600g"}
        };

        for (String[] p : productes) {
            Producte prod = new Producte();
            prod.setNom(p[0]);
            prod.setDescripcio(p[1]);
            producteRepository.save(prod);
        }

        System.out.println("[DemoData]   10 productes finals creats");
    }

    private void crearLots() {
        System.out.println("[DemoData] Creant lots...");

        var proveidors = provedorRepository.findAll().stream()
                .sorted(java.util.Comparator.comparing(Provedor::getNif))
                .toList();
        var materies = materiaPrimeraRepository.findAll();
        var usuarisActius = usuariRepository.findByActivoTrue();

        Usuari usuariInici = usuarisActius.stream()
                .filter(u -> "TRABAJADOR".equals(u.getRol()))
                .findFirst()
                .orElse(usuarisActius.get(0));

        LocalDateTime avui = LocalDateTime.now();

        Provedor[] proveïdorsArray = proveidors.toArray(new Provedor[0]);
        MateriaPrimera[] materiesArray = materies.toArray(new MateriaPrimera[0]);

        Object[][] lotsDef = {
            {"LOT-001", proveïdorsArray[0].getNif(), materiesArray[0].getId(), "EN_ESTOC", 500, "kg", avui.minusDays(5), null, avui.minusDays(5)},
            {"LOT-002", proveïdorsArray[0].getNif(), materiesArray[1].getId(), "EN_ESTOC", 300, "kg", avui.minusDays(4), null, avui.minusDays(4)},
            {"LOT-003", proveïdorsArray[1].getNif(), materiesArray[2].getId(), "EN_ESTOC", 400, "kg", avui.minusDays(3), null, avui.minusDays(3)},
            {"LOT-004", proveïdorsArray[1].getNif(), materiesArray[3].getId(), "EN_ESTOC", 250, "kg", avui.minusDays(2), null, avui.minusDays(2)},
            {"LOT-005", proveïdorsArray[2].getNif(), materiesArray[4].getId(), "EN_ESTOC", 600, "kg", avui.minusDays(1), null, avui.minusDays(1)},
            {"LOT-006", proveïdorsArray[2].getNif(), materiesArray[5].getId(), "EN_ESTOC", 200, "kg", avui, null, avui},
            {"LOT-007", proveïdorsArray[3].getNif(), materiesArray[6].getId(), "EN_ESTOC", 350, "L", avui.minusDays(6), null, avui.minusDays(6)},
            {"LOT-008", proveïdorsArray[3].getNif(), materiesArray[7].getId(), "EN_ESTOC", 450, "L", avui.minusDays(7), null, avui.minusDays(7)},
            {"LOT-009", proveïdorsArray[4].getNif(), materiesArray[8].getId(), "OBERT", 100, "L", avui.minusDays(1), avui.minusDays(1), avui.minusDays(1)},
            {"LOT-010", proveïdorsArray[4].getNif(), materiesArray[9].getId(), "OBERT", 150, "kg", avui.minusDays(3), avui.minusDays(2), avui.minusDays(3)},
            {"LOT-011", proveïdorsArray[5].getNif(), materiesArray[10].getId(), "OBERT", 200, "kg", avui.minusDays(5), avui.minusDays(4), avui.minusDays(5)},
            {"LOT-012", proveïdorsArray[0].getNif(), materiesArray[11].getId(), "EN_ESTOC", 180, "kg", null, null, avui.minusDays(6)},
            {"LOT-013", proveïdorsArray[1].getNif(), materiesArray[12].getId(), "OBERT", 90, "kg", avui.minusDays(7), avui.minusDays(6), avui.minusDays(7)},
            {"LOT-014", proveïdorsArray[2].getNif(), materiesArray[13].getId(), "OBERT", 120, "kg", avui.minusDays(8), avui.minusDays(7), avui.minusDays(8)},
            {"LOT-015", proveïdorsArray[3].getNif(), materiesArray[14].getId(), "OBERT", 80, "kg", avui.minusDays(9), avui.minusDays(8), avui.minusDays(9)},
            {"LOT-016", proveïdorsArray[0].getNif(), materiesArray[15].getId(), "EN_ESTOC", 300, "kg", null, null, avui.minusDays(10)},
            {"LOT-017", proveïdorsArray[1].getNif(), materiesArray[16].getId(), "ACABAT", 250, "kg", avui.minusDays(8), avui.minusDays(4), avui.minusDays(8)},
            {"LOT-018", proveïdorsArray[2].getNif(), materiesArray[17].getId(), "ACABAT", 400, "kg", avui.minusDays(6), avui.minusDays(2), avui.minusDays(6)},
            {"LOT-019", proveïdorsArray[3].getNif(), materiesArray[18].getId(), "ACABAT", 150, "kg", avui.minusDays(4), avui.minusDays(1), avui.minusDays(4)},
            {"LOT-020", proveïdorsArray[4].getNif(), materiesArray[19].getId(), "ACABAT", 200, "kg", avui.minusDays(2), avui, avui.minusDays(2)}
        };

        for (int i = 0; i < lotsDef.length; i++) {
            Object[] def = lotsDef[i];
            Lot lot = new Lot();
            lot.setIdLot((String) def[0]);
            lot.setNifProveidor((String) def[1]);
            lot.setProvedor(provedorRepository.findByNif((String) def[1]).orElse(proveïdorsArray[0]));
            lot.setMateriaPrimera(materiaPrimeraRepository.findById((Long) def[2]).orElse(materiesArray[0]));
            lot.setEstat((String) def[3]);
            lot.setQuantitat((Integer) def[4]);
            lot.setUnitat((String) def[5]);
            lot.setDataRecepcio((LocalDateTime) def[8]);
            lot.setUsuariInici(usuariInici);

            if (((String) def[3]).equals("OBERT") || ((String) def[3]).equals("ACABAT")) {
                lot.setDataObertura((LocalDateTime) def[6]);
            }
            if (((String) def[3]).equals("ACABAT")) {
                lot.setDataAcabament((LocalDateTime) def[7]);
            }

            lotRepository.save(lot);
        }

        System.out.println("[DemoData]   20 lots creats");
    }

    private void crearAlbaransProveidor() {
        System.out.println("[DemoData] Creant albarans de proveïdor...");

        var proveidors = provedorRepository.findAll();
        var usuarisActius = usuariRepository.findByActivoTrue();

        Usuari operador = usuarisActius.stream()
                .filter(u -> "TRABAJADOR".equals(u.getRol()))
                .findFirst()
                .orElse(usuarisActius.get(0));

        String[] provNifs = {"C74638693", "D03651585", "Q7258758G", "R4790605B", "U03650785"};

        LocalDateTime[] dates = {
            LocalDateTime.of(2026, 5, 2, 9, 0),
            LocalDateTime.of(2026, 5, 5, 10, 30),
            LocalDateTime.of(2026, 5, 10, 11, 0),
            LocalDateTime.of(2026, 5, 15, 8, 45),
            LocalDateTime.of(2026, 5, 22, 14, 0)
        };

        String[][] lotsPerAlbara = {
            {"LOT-001", "LOT-002", "LOT-012", "LOT-016"},
            {"LOT-003", "LOT-004", "LOT-013", "LOT-017"},
            {"LOT-005", "LOT-006", "LOT-014", "LOT-018"},
            {"LOT-007", "LOT-008", "LOT-015", "LOT-019"},
            {"LOT-009", "LOT-010", "LOT-020"}
        };

        for (int i = 0; i < 5; i++) {
            String provNif = provNifs[i];
            Provedor provedor = provedorRepository.findByNif(provNif).orElse(null);

            if (provedor == null) {
                System.out.println("[DemoData]   ERROR: No es troba proveïdor " + provNif);
                continue;
            }

            AlbarraProveidor albara = new AlbarraProveidor();
            albara.setNifProveidor(provNif);
            albara.setIdAlbarra("ALB-00" + (i + 1));
            albara.setProvedor(provedor);
            albara.setUsuari(operador);
            albara.setDataRecepcio(dates[i]);
            albara.setProvedorNombre(provedor.getNombre());
            albaraRepository.save(albara);

            for (int j = 0; j < lotsPerAlbara[i].length; j++) {
                String lotId = lotsPerAlbara[i][j];
                Lot lot = lotRepository.findByIdLot(lotId).orElse(null);

                if (lot == null) {
                    System.out.println("[DemoData]   ERROR: No es troba lot " + lotId);
                    continue;
                }

                Provedor lotProvedor = lot.getProvedor();
                if (lotProvedor == null) {
                    lotProvedor = provedorRepository.findByNif(lot.getNifProveidor()).orElse(provedor);
                }

                LiniarProveidor linia = new LiniarProveidor();
                linia.setNifProveidor(lot.getNifProveidor());
                linia.setIdLot(lot.getIdLot());
                linia.setIdAlbarra(albara.getIdAlbarra());
                linia.setLot(lot);
                linia.setAlbara(albara);
                linia.setProvedor(lotProvedor);
                linia.setQuantitat(lot.getQuantitat());
                linia.setUnitat(lot.getUnitat());
                liniarProveidorRepository.save(linia);

                System.out.println("[DemoData]     Linia creada: " + lotId + " -> ALB-00" + (i + 1) + " (prov: " + lot.getNifProveidor() + ")");
            }
        }

        System.out.println("[DemoData]   5 albarans de proveïdor creats");
    }

    private void crearAlbaransClient()   {
        System.out.println("[DemoData] Creant albarans de client...");

        var clients = clientRepository.findAll();
        var productes = producteRepository.findAll();
        var usuarisActius = usuariRepository.findByActivoTrue();
        var lots = lotRepository.findAll();

        Usuari operador = usuarisActius.stream()
                .filter(u -> "TRABAJADOR".equals(u.getRol()))
                .findFirst()
                .orElse(usuarisActius.get(0));

        LocalDate[] datesProduccio = {
            LocalDate.of(2026, 5, 15),
            LocalDate.of(2026, 5, 17),
            LocalDate.of(2026, 5, 19),
            LocalDate.of(2026, 5, 21),
            LocalDate.of(2026, 5, 24)
        };

        String[] estats = {"NO_LLIURAT", "NO_LLIURAT", "LLIURAT", "NO_LLIURAT", "LLIURAT"};

        for (int i = 0; i < 5; i++) {
            Client client = clients.get(i % clients.size());

            AlbarraClient albara = new AlbarraClient(client, datesProduccio[i], operador);
            albara.setEstat(estats[i]);
            albaraClientRepository.save(albara);

            for (int p = 0; p < 5; p++) {
                int prodIdx = (i + p) % productes.size();
                Producte prod = productes.get(prodIdx);

                LiniarClient linia = new LiniarClient();
                linia.setNifClient(client.getNif());
                linia.setDataProduccio(datesProduccio[i]);
                linia.setIdProducte(prod.getIdProducte());
                linia.setQuantitat((p + 1) * 10.0);
                linia.setProducte(prod);
                linia.setAlbarraClient(albara);
                liniarClientRepository.save(linia);
            }

            LocalDateTime dateTimeStart = datesProduccio[i].atStartOfDay();
            LocalDateTime dateTimeEnd = datesProduccio[i].atTime(23, 59, 59);
            var lotsPerTra = lotRepository.findLotsObertsPerTraçabilitat(dateTimeStart, dateTimeEnd);
            for (Lot lot : lotsPerTra) {
                LiniarClientLot link = new LiniarClientLot(albara, lot);
                liniarClientLotRepository.save(link);
            }
        }

        System.out.println("[DemoData]   5 albarans de client creats");
    }

    private void crearPasswordTxt() {
        try {
            Path projectRoot = Paths.get("..").toAbsolutePath().normalize();
            Path passwordFile = projectRoot.resolve("password.txt");

            try (PrintWriter writer = new PrintWriter(new FileWriter(passwordFile.toFile()))) {
                writer.println("EASYTRAZA - CONTRASENYES D'ACCES");
                writer.println("================================");
                writer.println("Generat automàticament per DemoDataLoader");
                writer.println("Data: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")));
                writer.println();
                writer.println("superadmin@easytraza.com / admin123 / SUPER_ADMIN");
                writer.println("admin@easytraza.com / Admin123 / ADMIN");
                writer.println("operador1@easytraza.com / Op12345 / TRABAJADOR");
                writer.println("operador2@easytraza.com / Op12345 / TRABAJADOR");
            }

            System.out.println("[DemoData]   Fitxer password.txt creat: " + passwordFile);
        } catch (Exception e) {
            System.err.println("[DemoData] Error creant password.txt: " + e.getMessage());
        }
    }
}
