package com.gestion.controller.user;

import com.gestion.dto.request.user.UserRequest;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.model.User;
import com.gestion.service.user.UserCreatorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/-----") // a completar con lo correcto
public class UserPostController {
    private final UserCreatorService userCreatorService;

    public UserPostController(UserCreatorService userCreatorService){
        this.userCreatorService = userCreatorService;
    }


    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request){
        User user = userCreatorService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.toResponse(user));
    }
}
