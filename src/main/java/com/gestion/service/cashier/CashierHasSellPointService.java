package com.gestion.service.cashier;

import com.gestion.enums.Role;
import com.gestion.exception.CashierWithoutSellPointException;
import org.springframework.stereotype.Service;

@Service
public class CashierHasSellPointService {

    public void validate(Role role, Long sellPointId) {
        if (role == Role.CASHIER && sellPointId == null) {
            throw new CashierWithoutSellPointException();
        }
    }
}
