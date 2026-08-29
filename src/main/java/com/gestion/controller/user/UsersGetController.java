//package com.gestion.controller.user;
//
//import com.gestion.dto.response.user.UserResponse;
//import com.gestion.mappers.UserMapper;
//import com.gestion.service.user.UserGetAllService;
//import lombok.AllArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/users")
//@AllArgsConstructor
//public class UsersGetController {
//    private final UserGetAllService userGetAllService;
//    private final UserMapper userMapper;
//
//    @GetMapping
//    public ResponseEntity<List<UserResponse>> getAll() {
//        return ResponseEntity.ok(userGetAllService.findAll());
//    }
//    // Bueno, aca comparando con lo de Mati, retorna un List de respons y no pasa parametros.
//    // obvio que asi mirandolo no lo entiendo, esto es para ver como lo dejamos
//}
