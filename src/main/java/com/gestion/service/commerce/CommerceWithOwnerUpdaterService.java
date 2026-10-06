package com.gestion.service.commerce;

import com.gestion.dto.request.commerce.CommerceRequest;
import com.gestion.dto.request.commerce.CommerceWithOwnerUpdateRequest;
import com.gestion.dto.request.user.UserUpdateRequest;
import com.gestion.dto.response.commerce.CommerceWithOwnerResponse;
import com.gestion.mappers.CommerceMapper;
import com.gestion.mappers.UserMapper;
import com.gestion.model.Commerce;
import com.gestion.model.User;
import com.gestion.properties.MinioProperties;
import com.gestion.service.file.FileFinderService;
import com.gestion.service.user.UserUpdaterService;
import io.minio.errors.MinioException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceWithOwnerUpdaterService {
    private final CommerceUpdaterService commerceUpdaterService;
    private final UserUpdaterService userUpdaterService;
    private final FileFinderService fileFinderService;
    private final MinioProperties minioProperties;


    @Transactional
    public CommerceWithOwnerResponse updateCommerceWithOwner(CommerceWithOwnerUpdateRequest request, Long commerceId, Long userId) {
        CommerceRequest commerceRequest = CommerceMapper.toRequestFromCWOUR(request);
        Commerce updatedCommerce = commerceUpdaterService.updateCommerce(commerceRequest, commerceId);

        UserUpdateRequest userRequest = UserMapper.toUpdateRequestFromCWOUR(request);
        User updatedUser = userUpdaterService.updateUser(userRequest, userId);

        String businessLogoUrl = null;

        if (updatedCommerce.getLogoName() != null && !updatedCommerce.getLogoName().isEmpty()) {
            try {
                businessLogoUrl = fileFinderService.getObjectUrl(updatedCommerce.getLogoName(),
                        minioProperties.dir().businessLogos());
            } catch (MinioException e) {
                System.out.println("Error retrieving business logo from MinIO: " + e.getCause());
            }
        }

        return CommerceMapper.toCommerceWithOwnerResponse(updatedCommerce, updatedUser, businessLogoUrl);
    }
}
