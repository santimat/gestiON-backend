package com.gestion.service.user;

import com.gestion.model.User;
import com.gestion.repository.user.JpaUserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserDeleterService {
    private final JpaUserRepository jpaUserRepository;
    private final UserFinderService userFinderService;

    public UserDeleterService(JpaUserRepository jpaUserRepository,
                              UserFinderService userFinderService){
        this.jpaUserRepository = jpaUserRepository;
        this.userFinderService = userFinderService;
    }

    public void delete(Long id){
        User user = userFinderService.find(id);
        jpaUserRepository.delete(user);
    }
}
