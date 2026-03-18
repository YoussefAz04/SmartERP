package BuissnesManager.BuissnesManager.service;

import BuissnesManager.BuissnesManager.entity.CategorieClient;
import BuissnesManager.BuissnesManager.entity.Client;
import BuissnesManager.BuissnesManager.repository.CategorieClientRepo;
import BuissnesManager.BuissnesManager.repository.ClientRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientRepo clientRepo ;
    @Autowired
    private CategorieClientRepo categorieClientRepo ;


    @Transactional
    public Client addClient(Client client,Long idCategorie){
         client.setCategorieClient(categorieClientRepo.findById(idCategorie).get());
         return clientRepo.save(client);
    }


    public List<Client> getAllClient(){
        return clientRepo.findAll();
    }
    public Client getClientById(Long idClient){
        return clientRepo.findById(idClient).get();
    }
    public CategorieClient addCategorieClient(CategorieClient categorieClient){
        return categorieClientRepo.save(categorieClient);
    }

    public List<CategorieClient> getAllCategorieClient(){
        return categorieClientRepo.findAll();
    }
    public CategorieClient getCategorieClientById(Long id){
        return categorieClientRepo.findById(id).get();
    }








}
