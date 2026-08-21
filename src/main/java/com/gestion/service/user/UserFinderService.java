package com.gestion.service.user;

import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.User;
import com.gestion.repository.user.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserFinderService {
    private final JpaUserRepository jpaUserRepository;

    public User findById(Long id) {
        return jpaUserRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + id + " not found"));
    }
}
