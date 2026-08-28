package com.gestion.controller.user;

import com.gestion.dto.response.user.UserResponse;
import com.gestion.mappers.UserMapper;
import com.gestion.model.User;
import com.gestion.service.user.UserFinderByIdService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users") //esto lo vas a revisar vos poorq ni idea de las rutas que usaremos
@AllArgsConstructor
public class UserGetController {
    private final UserFinderByIdService userFinderByIdService;
    private final UserMapper userMapper;

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        User user = userFinderByIdService.findById(id);
        UserResponse response = userMapper.toResponse(user);

        return ResponseEntity.ok(response);
    }
}
