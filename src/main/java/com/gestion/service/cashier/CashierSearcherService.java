package com.gestion.service.cashier;


import com.gestion.dto.response.user.UserResponse;
import com.gestion.mappers.UserMapper;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CashierSearcherService {
    private final JpaUserRepository jpaUserRepository;

    public Page<UserResponse> searchCashiers(Long commerceId, String searchText, Pageable pageable) {
        String effectiveSearchText = (searchText == null || searchText.isBlank()) ? null : searchText.trim();
        Page<User> cashiers = jpaUserRepository
                .findAllCashiersWithFilters(commerceId, effectiveSearchText, pageable);
        return cashiers.map(UserMapper::toResponse);
    }
}
