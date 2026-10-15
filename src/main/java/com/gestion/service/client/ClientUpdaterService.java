package com.gestion.service.client;

import com.gestion.dto.request.client.ClientRequest;
import com.gestion.model.Client;
import com.gestion.repository.JpaClientRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@AllArgsConstructor
public class ClientUpdaterService {
    private final JpaClientRepository jpaClientRepository;

    @Transactional
    public Client updateClient(ClientRequest request) {
        Client clientToUpdate = jpaClientRepository.findById();

        clientToUpdate.setName(request.name());
        clientToUpdate.setLastName(request.lastName());
        clientToUpdate.setAddress(request.address());
        clientToUpdate.setPhoneNumber(request.phoneNumber());
        clientToUpdate.setDni(request.dni());
        clientToUpdate.setCreatedAt(LocalDate.now()); // esto revisar si corresponde
        return jpaClientRepository.save(clientToUpdate);
    }
}
