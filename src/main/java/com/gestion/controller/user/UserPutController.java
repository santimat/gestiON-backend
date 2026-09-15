package com.gestion.controller.user;

import com.gestion.dto.request.user.UserRequest;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.service.user.UserUpdaterService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserPutController {
    private final UserUpdaterService userUpdaterService;

    @PutMapping("/:id")
    public ResponseEntity<UserResponse> updateUserById(@RequestParam Long id, @RequestBody @Valid UserRequest userRequest) {
        
    }
}
