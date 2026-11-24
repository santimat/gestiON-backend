package com.gestion.service.commerce;

import com.gestion.dto.response.commerce.CommerceWithOwnerResponse;
import com.gestion.enums.Role;
import com.gestion.mappers.CommerceWithOwnerMapper;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.properties.MinioProperties;
import com.gestion.repository.JpaUserRepository;
import com.gestion.service.file.FileFinderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class CommerceWithOwnerSearcherService {

    private final JpaUserRepository userRepository;
    private final FileFinderService fileFinderService;
    private final MinioProperties minioDirProperties;

    @Transactional(readOnly = true)
    public Page<CommerceWithOwnerResponse> findAllWithOwner(String searchText, Boolean active, Pageable pageable) {
        String effectiveSearchText = (searchText == null || searchText.isBlank()) ? null : searchText.trim();
        Page<User> owners = userRepository.findAllByRoleWithCommerce(
                Role.OWNER, effectiveSearchText, active, pageable);

        return owners.map(owner -> {
            Commerce commerce = owner.getCommerce();
            String businessLogoUrl =
                    fileFinderService.getObjectUrl(commerce.getLogoName(),
                            minioDirProperties.dir().businessLogos());

            return CommerceWithOwnerMapper.toCommerceWithOwnerResponse(commerce, owner, businessLogoUrl);
        });
    }
}
