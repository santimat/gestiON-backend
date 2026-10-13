package com.gestion.service.cashier;


import com.gestion.dto.response.user.UserResponse;
import com.gestion.enums.Role;
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
    private JpaUserRepository jpaUserRepository;

    public Page<UserResponse> searchCashier(Pageable pageable) {
        Page<User> cashiers = jpaUserRepository.findAllByRole(Role.CASHIER, pageable);
        return cashiers.map(UserMapper::toResponse);
    }
}
