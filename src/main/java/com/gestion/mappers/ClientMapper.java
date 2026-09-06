//package com.gestion.mappers;
//
//import com.gestion.dto.request.client.ClientRequest;
//import com.gestion.dto.response.ClientResponse;
//import com.gestion.model.Client;
//import org.springframework.stereotype.Component;
//
//@Component
//public class ClientMapper {
//
//    public ClientResponse toResponse(Client client) {
//        if (client == null) {
//            return null;
//        }
//
//        return new ClientResponse(
//                client.getId(),
//                client.getName(),
//                client.getLastName(),
//                client.getAddress(),
//                client.getPhoneNumber(),
//                client.getDni()
//        );
//    }
//
//    public Client toEntity(ClientRequest request) {
//        if (request == null) {
//            return null;
//        }
//        Client client = new Client();
//        client.setName(request.name());
//        client.setLastName(request.lastName());
//        client.setAddress(request.address());
//        client.setPhoneNumber(request.phoneNumber());
//        client.setDni(request.dni());
//        return client;
//    }
//}
