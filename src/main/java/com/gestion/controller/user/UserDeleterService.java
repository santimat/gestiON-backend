//package com.gestion.controller.user;
//
//import com.gestion.mappers.UserMapper;
//import lombok.AllArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("api/users")
//@AllArgsConstructor
//public class UserDeleterService {
//    private final UserDeleterService userDeleterService;
//    private final UserMapper userMapper;
//
//    // falta el @Preauthorize creo
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        userDeleterService.delete(id);
//        return ResponseEntity.noContent().build();
//    }
//}
