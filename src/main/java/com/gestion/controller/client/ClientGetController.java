package com.gestion.controller.client;

import com.gestion.dto.response.client.ClientResponse;
import com.gestion.service.client.ClientFinderByDniService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
@AllArgsConstructor
public class ClientGetController {
    private final ClientFinderByDniService finderByDniService;

    @GetMapping("/{dni}")
    public ClientResponse getClientByDni(@PathVariable String dni) {
        return finderByDniService.findByDni(dni);
    }
}
