package com.gestion.service.user;

import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserDeleterService {
    private final JpaUserRepository userRepository;
    private final UserFinderService userFinderService;


    public void delete(Long id) {
        User user = userFinderService.findById(id);
        userRepository.delete(user);
    }
}
