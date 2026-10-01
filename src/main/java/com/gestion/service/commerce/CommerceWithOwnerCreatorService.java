package com.gestion.service.commerce;

import com.gestion.dto.request.commerce.CommerceRequest;
import com.gestion.dto.request.commerce.CommerceWithOwnerRequest;
import com.gestion.dto.request.user.UserRequest;
import com.gestion.dto.response.commerce.CommerceWithOwnerResponse;
import com.gestion.enums.Role;
import com.gestion.mappers.CommerceMapper;
import com.gestion.mappers.UserMapper;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.service.user.UserCreatorService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceWithOwnerCreatorService {

    private final UserCreatorService userCreatorService;
    private final CommerceCreatorService commerceCreatorService;
    private final CommerceLogoUploaderService commerceLogoUploaderService;

    @Transactional
    public CommerceWithOwnerResponse createCommerceWithOwner(CommerceWithOwnerRequest request
    ) {
        CommerceRequest commerceRequest = CommerceMapper.toRequestFromCWOR(request);
        Commerce commerce = commerceCreatorService.createCommerce(commerceRequest);

        UserRequest userRequest = UserMapper.toRequestFromCWOR(request);
        User user = userCreatorService.createUser(userRequest, Role.OWNER, commerce);

        String businessLogoUrl = null;

        if (request.businessLogo() != null) {
            businessLogoUrl = commerceLogoUploaderService.uploadLogoAndGetName(request.businessLogo(), commerce);
        }

        return CommerceMapper.toCommerceWithOwnerResponse(commerce, user, businessLogoUrl);
    }
}
