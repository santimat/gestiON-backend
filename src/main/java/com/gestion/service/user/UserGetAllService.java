package com.gestion.service.user;

import com.gestion.model.User;
import com.gestion.repository.user.JpaUserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class UserGetAllService {
    private final JpaUserRepository jpaUserRepository;

    public UserGetAllService(JpaUserRepository jpaUserRepository){
        this.jpaUserRepository = jpaUserRepository;
    }

    public Page<User> findAll(Pageable pageable){
        return jpaUserRepository.findAll(pageable);
    }
}
