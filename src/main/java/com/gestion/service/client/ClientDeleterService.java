//package com.gestion.service.client;
//
//import com.gestion.model.Client;
//import com.gestion.repository.JpaClientRepository;
//import lombok.AllArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@AllArgsConstructor
//public class ClientDeleterService {
//    private final JpaClientRepository clientRepository;
//    private final ClientFinderByDniService clientFinderByDniService;
//
//    public void delete(String dni){
//        Client client = clientFinderByDniService.findByDni(dni);
//        clientRepository.delete(client);
//    }
//}
