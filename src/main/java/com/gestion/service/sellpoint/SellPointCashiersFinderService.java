package com.gestion.service.sellpoint;

import com.gestion.config.UserPrincipal;
import com.gestion.dto.response.user.UserResponse;
import com.gestion.mappers.CashierMapper;
import com.gestion.model.SellPoint;
import com.gestion.model.User;
import com.gestion.repository.JpaUserRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class SellPointCashiersFinderService {
    private final JpaUserRepository jpaUserRepository;
    private final SellPointFinderByIdAndCommerceIdService sellPointFinderByIdAndCommerceIdService;

    @Transactional(readOnly = true)
    public Page<UserResponse> findCashiersBySellPoint(Long sellPointId, UserPrincipal authenticatedUser, Pageable pageable) {
        SellPoint sellPoint = sellPointFinderByIdAndCommerceIdService
                .findSellPointByIdAndCommerceId(sellPointId, authenticatedUser.getCommerceId());
        Page<User> cashiers = jpaUserRepository
                .findAllBySellPointIdAndCommerceId(sellPoint.getId(), authenticatedUser.getCommerceId(), pageable);
        return cashiers.map(CashierMapper::toResponse);
    }
}
