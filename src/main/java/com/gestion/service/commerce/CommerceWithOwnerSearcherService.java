package com.gestion.service.commerce;

import com.gestion.dto.response.commerce.CommerceWithOwnerResponse;
import com.gestion.enums.Role;
import com.gestion.mappers.CommerceMapper;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.properties.MinioProperties;
import com.gestion.repository.JpaUserRepository;
import com.gestion.service.file.FileFinderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommerceWithOwnerSearcherService {

    private final JpaUserRepository userRepository;
    private final FileFinderService fileFinderService;
    private final MinioProperties minioDirProperties;

    public Page<CommerceWithOwnerResponse> findAllWithOwner(Pageable pageable) {
        Page<User> owners = userRepository.findAllByRoleWithCommerce(Role.OWNER, pageable);

        return owners.map(owner -> {
            Commerce commerce = owner.getCommerce();
            String businessLogoUrl = commerce.getLogoName() != null ?
                    fileFinderService.getObjectUrl(commerce.getLogoName(),
                            minioDirProperties.dir().businessLogos()) : null;
            return CommerceMapper.toCommerceWithOwnerResponse(commerce, owner, businessLogoUrl);
        });
    }
}
