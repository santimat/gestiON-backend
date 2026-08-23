package com.gestion.service.user;

import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserGetAllService {
    private final JpaUserRepository userRepository;

    // TODO: agregar query que acepte filtros de busqueda (hay que hacer la query en el repository)
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
