package com.gestion.service.user;

import com.gestion.model.User;
import com.gestion.repository.user.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserGetAllService {
    private final JpaUserRepository jpaUserRepository;

    public Page<User> findAll(Pageable pageable) {
        return jpaUserRepository.findAll(pageable);
    }
}
