package com.gestion.service.commerce;

import com.gestion.dto.response.commerce.CommerceWithOwnerResponse;
import com.gestion.enums.Role;
import com.gestion.mappers.CommerceMapper;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import com.gestion.service.file.FileFinderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceFindAllWithOwnerService {

    private final JpaUserRepository userRepository;
    private final FileFinderService fileFinderService;

    public Page<CommerceWithOwnerResponse> findAllWithOwner(Pageable pageable) {
        Page<User> owners = userRepository.findAllByRoleWithCommerce(Role.OWNER, pageable);

        return owners.map(owner -> {
            Commerce commerce = owner.getCommerce();
            String businessLogoUrl = commerce.getLogoName() != null ?
                    fileFinderService.getObjectUrl(commerce.getLogoName(), "business-logos") : null;
            return CommerceMapper.toCommerceWithOwnerResponse(commerce, owner, businessLogoUrl);
        });
    }
}
