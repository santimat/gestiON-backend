package com.gestion.mappers;

import com.gestion.dto.request.client.ClientRequest;
import com.gestion.dto.response.client.ClientResponse;
import com.gestion.model.Client;

public class ClientMapper {

    public static ClientResponse toResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getName(),
                client.getLastName(),
                client.getAddress(),
                client.getPhoneNumber(),
                client.getDni()
        );
    }

    public static Client toEntity(ClientRequest request) {
        if (request == null) {
            return null;
        }
        Client client = new Client();
        client.setName(request.name());
        client.setLastName(request.lastName());
        client.setAddress(request.address());
        client.setPhoneNumber(request.phoneNumber());
        client.setDni(request.dni());
        return client;
    }
}
