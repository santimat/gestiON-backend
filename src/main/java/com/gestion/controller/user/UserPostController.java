package com.gestion.controller.user;

import com.gestion.dto.request.user.UserRequest;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.mappers.UserMapper;
import com.gestion.service.user.UserCreatorService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserPostController {
    private final UserCreatorService userCreatorService;
    private final UserMapper userMapper;

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        UserResponse response = userMapper.toResponse(userCreatorService.createUser(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
