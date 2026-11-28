package com.gestion.service.commerce;

import com.gestion.dto.response.commerce.CommerceWithOwnerResponse;
import com.gestion.enums.Role;
import com.gestion.mappers.CommerceWithOwnerMapper;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.properties.MinioProperties;
import com.gestion.repository.JpaCommerceRepository;
import com.gestion.repository.JpaUserRepository;
import com.gestion.service.file.FileFinderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CommerceWithOwnerSearcherService {

    private final JpaCommerceRepository commerceRepository;
    private final JpaUserRepository userRepository;
    private final FileFinderService fileFinderService;
    private final MinioProperties minioDirProperties;

    @Transactional(readOnly = true)
    public Page<CommerceWithOwnerResponse> findAllWithOwner(Pageable pageable) {
        Page<Commerce> commerces = commerceRepository.findAll(pageable);

        List<Long> commerceIds = commerces.getContent().stream().map(Commerce::getId).toList();
        Map<Long, User> ownerByCommerceId = commerceIds.isEmpty()
                ? Map.of()
                : userRepository.findAllByRoleAndCommerceIdIn(Role.OWNER, commerceIds).stream()
                        .collect(Collectors.toMap(
                                owner -> owner.getCommerce().getId(),
                                Function.identity(),
                                // si un comercio tuviera mas de un owner, se muestra el de menor id
                                (first, second) -> first.getId() <= second.getId() ? first : second));

        return commerces.map(commerce -> {
            String businessLogoUrl =
                    fileFinderService.getObjectUrl(commerce.getLogoName(),
                            minioDirProperties.dir().businessLogos());

            return CommerceWithOwnerMapper.toCommerceWithOwnerResponse(
                    commerce, ownerByCommerceId.get(commerce.getId()), businessLogoUrl);
        });
    }
}
