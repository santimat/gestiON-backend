package com.gestion.service.user;

import com.gestion.dto.request.user.UserUpdateRequest;
import com.gestion.enums.Role;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserUpdaterService {
    private final JpaUserRepository userRepository;
    private final UserFinderByIdAndCommerceIdService userFinderByIdAndCommerceIdService;
    private final UserValidatorByEmailService userValidatorByEmailService;

    @Transactional
    public User updateUser(UserUpdateRequest request, Long userId, Long commerceId) {
        // el finder scopeado por comercio: el id viene del path y no de un token, asi que sin
        // este filtro se podrian actualizar usuarios de otro comercio.
        User userToUpdate = userFinderByIdAndCommerceIdService
                .findUserByIdAndCommerceIdAndRole(userId, commerceId, Role.OWNER);

        // el usuario ya tiene ese email: el chequeo lo excluye para no chocar con el mismo.
        userValidatorByEmailService.checkExistingUserByEmailAndIdNot(request.email(), userId);

        userToUpdate.setName(request.name());
        userToUpdate.setEmail(request.email());
        userToUpdate.setPhoneNumber(request.phoneNumber());
        return userRepository.save(userToUpdate);
    }
}
