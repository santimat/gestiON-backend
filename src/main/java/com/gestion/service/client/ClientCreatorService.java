//package com.gestion.service.client;
//
//import com.gestion.dto.request.client.ClientRequest;
//import com.gestion.exception.DuplicateResourceException;
//import com.gestion.mappers.ClientMapper;
//import com.gestion.model.Client;
//import com.gestion.repository.JpaClientRepository;
//import lombok.AllArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@AllArgsConstructor
//public class ClientCreatorService {
//    private final JpaClientRepository clientRepository;
//    private final ClientMapper clientMapper;
//
//    public Client createClient(ClientRequest request) {
//        if (clientRepository.existsClientByDni(request.dni())) {
//            throw new DuplicateResourceException("Client with DNI: " + request.dni() + "already exists");
//        }
//        Client newClient = clientMapper.toEntity(request);
//        return clientRepository.save(newClient);
//    }
//}
