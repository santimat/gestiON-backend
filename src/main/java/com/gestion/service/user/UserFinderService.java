package com.gestion.service.user;

import com.gestion.exception.UserNotFoundException;
import com.gestion.model.User;
import com.gestion.repository.user.JpaUserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserFinderService {
    private final JpaUserRepository jpaUserRepository;

    public UserFinderService(JpaUserRepository jpaUserRepository){
        this.jpaUserRepository = jpaUserRepository;
    }

    public User find(Long id){
        return jpaUserRepository.findById(id)
                .orElseThrow( ()-> new UserNotFoundException(id));
    }
}
