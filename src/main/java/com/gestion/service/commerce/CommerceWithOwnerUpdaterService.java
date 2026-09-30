package com.gestion.service.commerce;


import com.gestion.dto.request.commerce.CommerceRequest;
import com.gestion.dto.request.commerce.CommerceWithOwnerRequest;
import com.gestion.dto.request.user.UserRequest;
import com.gestion.dto.response.commerce.CommerceWithOwnerResponse;
import com.gestion.mappers.CommerceMapper;
import com.gestion.mappers.UserMapper;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.service.file.FileFinderService;
import com.gestion.service.user.UserUpdaterService;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceWithOwnerUpdaterService {
    private final CommerceUpdaterService commerceUpdaterService;
    private final UserUpdaterService userUpdaterService;
    private final FileFinderService fileFinderService;

    @Transactional
    public CommerceWithOwnerResponse updateCommerceWithOwner(CommerceWithOwnerRequest request, Long commerceId, Long userId) {
        CommerceRequest commerceRequest = CommerceMapper.toRequest(request);
        Commerce updatedCommerce = commerceUpdaterService.updateCommerce(commerceRequest, commerceId);

        UserRequest userRequest = UserMapper.toRequest(request);
        User updatedUser = userUpdaterService.updateUser(userRequest, userId);

        String businessLogoUrl = fileFinderService.getObjectUrl(updatedCommerce.getLogoName(), "business-logos");

        return CommerceMapper.toCommerceWithOwnerResponse(updatedCommerce, updatedUser, businessLogoUrl);
    }
}
