package com.gestion.service.user;

import com.gestion.dto.project.user.UserSessionSnapshot;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserSessionSnapshotFinderService {

    private final JpaUserRepository userRepository;

    @Transactional(readOnly = true)
    public Optional<UserSessionSnapshot> findSessionSnapshotByUserId(Long userId) {
        return userRepository.findSessionSnapshotById(userId);
    }
}
