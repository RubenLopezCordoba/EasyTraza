package cat.copernic.easytraza.service;

import cat.copernic.easytraza.model.Client;
import cat.copernic.easytraza.repository.AlbarraClientRepository;
import cat.copernic.easytraza.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
/**
 * Servei per a la gestió de clients (CRUD i validació de dades).
 */
public class ClientService {
    private static final Logger log = LoggerFactory.getLogger(ClientService.class);
@Autowired
    private ClientRepository clientRepository;

    @Autowired
    private AlbarraClientRepository albarraClientRepository;

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Optional<Client> getClientById(String nif) {
        return clientRepository.findById(nif);
    }

    public Client createClient(Client client) {
        if (client.getNif() == null || client.getNif().trim().isEmpty())
            throw new IllegalArgumentException("El NIF és obligatori");
        if (clientRepository.existsById(client.getNif()))
            throw new IllegalArgumentException("Ja existeix un client amb aquest NIF");
        if (client.getNom() == null || client.getNom().trim().isEmpty())
            throw new IllegalArgumentException("El nom és obligatori");
        if (client.getTelefon() == null || client.getTelefon().trim().isEmpty())
            throw new IllegalArgumentException("El telèfon és obligatori");
        return clientRepository.save(client);
    }

    @Transactional
    public Client updateClient(String nif, Client client) {
        Client existing = clientRepository.findById(nif)
                .orElseThrow(() -> new RuntimeException("Client no trobat"));
        boolean nameChanged = false;
        if (client.getNom() != null && !client.getNom().equals(existing.getNom())) {
            existing.setNom(client.getNom().trim());
            nameChanged = true;
        }
        if (client.getCognoms() != null && !client.getCognoms().equals(existing.getCognoms())) {
            existing.setCognoms(client.getCognoms().trim());
            nameChanged = true;
        }
        if (client.getTelefon() != null) existing.setTelefon(client.getTelefon().trim());
        if (client.getEmail() != null) existing.setEmail(client.getEmail().trim());
        if (client.getObservacions() != null) existing.setObservacions(client.getObservacions());
        if (client.getActivo() != null) existing.setActivo(client.getActivo());
        Client saved = clientRepository.save(existing);
        if (nameChanged) {
            String nouNom = saved.getNom() + " " + saved.getCognoms();
            List<cat.copernic.easytraza.model.AlbarraClient> albarans = albarraClientRepository.findByClientNif(nif);
            for (cat.copernic.easytraza.model.AlbarraClient a : albarans) a.setNomClient(nouNom);
            albarraClientRepository.saveAll(albarans);
        }
        return saved;
    }

    public void deleteClient(String nif) {
        if (!clientRepository.existsById(nif))
            throw new RuntimeException("Client no trobat");
        clientRepository.deleteById(nif);
    }
}
