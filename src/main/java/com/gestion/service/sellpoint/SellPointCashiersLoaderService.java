package com.gestion.service.sellpoint;

import com.gestion.enums.ErrorCode;
import com.gestion.enums.Role;
import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SellPointCashiersLoaderService {
    private final JpaUserRepository userRepository;

    @Transactional(readOnly = true)
    public Set<User> loadCashiers(Set<Long> cashierIds, Long commerceId) {
        if (cashierIds == null || cashierIds.isEmpty()) {
            return Set.of();
        }

        Set<User> cashiers = new HashSet<>(userRepository
                .findAllByIdInAndCommerceIdAndRole(cashierIds, commerceId, Role.CASHIER));

        Optional.of(cashierIds)
                .filter(ids -> ids.size() != cashiers.size())
                .ifPresent(ids -> {
                    String missingIds = ids.stream()
                            .filter(id -> cashiers.stream().noneMatch(cashier -> cashier.getId().equals(id)))
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "));
                    throw new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND,
                            "Cashiers with ids [" + missingIds + "] not found in this commerce");
                });

        return cashiers;
    }
}
