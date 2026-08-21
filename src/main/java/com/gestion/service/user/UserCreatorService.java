package com.gestion.service.user;

import com.gestion.dto.request.user.UserRequest;
import com.gestion.model.User;
import com.gestion.repository.commerce.JpaCommerceRepository;
import com.gestion.repository.user.JpaUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserCreatorService {
    private final JpaUserRepository jpaUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JpaCommerceRepository jpaCommerceRepository;

    public UserCreatorService(JpaUserRepository jpaUserRepository,
                              PasswordEncoder passwordEncoder,  // entiendo que lo de la password va en config a chequear!
                              JpaCommerceRepository jpaCommerceRepository){
        this.jpaUserRepository = jpaUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jpaCommerceRepository = jpaCommerceRepository;
    }

    // el create lo hago directo, ya que no entiendo que el unico que crea usuarios es el ADMIN
    //no hay validaciones de rol. Podemos chequear si existe por email pero despues me diras

    public User create(UserRequest request){
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(request.password());
        user.setRole(request.role());
        user.setStatus(request.status());
        user.setCommerce(jpaCommerceRepository.findsById(request.commerceId()));  // a chequear!!

        jpaUserRepository.save(user);
    }
}

