package com.gestion.service.user;

import com.gestion.model.User;
import com.gestion.repository.user.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserDeleterService {
    private final JpaUserRepository jpaUserRepository;
    private final UserFinderService userFinderService;


    public void delete(Long id) {
        User user = userFinderService.findById(id);
        jpaUserRepository.delete(user);
    }
}
