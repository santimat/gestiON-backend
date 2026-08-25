package com.gestion.service.user;

import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserDeleterService {
    private final JpaUserRepository userRepository;
    private final UserFinderByIdService userFinderByService;

    public void delete(Long id) {

        // TODO: chequear la eliminación debido a que este puede tener relaciones.
        User user = userFinderByService.findById(id);
        userRepository.delete(user);
    }
}
