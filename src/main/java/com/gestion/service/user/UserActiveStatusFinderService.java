package com.gestion.service.user;

import com.gestion.dto.project.UserActiveStatus;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class UserActiveStatusFinderService {

    private final JpaUserRepository userRepository;

    @Transactional()
    public UserActiveStatus findActiveStatusByUserId(Long userId) {
        return userRepository.findActiveStatusById(userId).orElse(null);
    }
}
