package com.gestion.controller.user;

import com.gestion.dto.request.user.UserRequest;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.mappers.UserMapper;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.service.user.UserCreatorService;
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
    public ResponseEntity<UserResponse> create(@RequestBody UserRequest request, Commerce commerce) {
        User user = userCreatorService.createUser(request, commerce);
        UserResponse response = userMapper.toResponse(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
