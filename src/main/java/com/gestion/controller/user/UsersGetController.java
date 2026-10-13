package com.gestion.controller.user;

import com.gestion.dto.response.user.UserResponse;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UsersGetController {

    //TODO: usar de forma general en usuarios permitiendo recibir el role (no me parece lo mejor) o crear un endpoint
    // para obtener los cajeros
    @GetMapping
    public ResponseEntity<Page<UserResponse>> getAllUsers() {

    }
}
