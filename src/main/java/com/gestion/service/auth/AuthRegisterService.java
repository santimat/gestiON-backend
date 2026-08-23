package com.gestion.service.auth;

import com.gestion.dto.request.commerce.CommerceRequest;
import com.gestion.dto.request.user.UserRequest;
import com.gestion.model.Commerce;
import com.gestion.service.commerce.CommerceCreatorService;
import com.gestion.service.user.UserCreatorService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class AuthRegisterService {
    private final UserCreatorService userCreatorService;
    private final CommerceCreatorService commerceCreatorService;

    // Esta anotación es para que en caso de que falle la creación de alguno de los dos, se haga un rollback y no se cree ninguno.
    // Esto nos previene tener comercios sin usuarios o usuarios sin comercios.
    @Transactional
    public void registerUserAndCommerce(UserRequest userRequest, CommerceRequest commerceRequest) {
        Commerce commerce = commerceCreatorService.createCommerce(commerceRequest);
        userCreatorService.createUser(userRequest, commerce);
        // TODO: enviar correo de confirmación al usuario
    }
}
