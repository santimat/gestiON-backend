package com.gestion.service.sale;

import com.gestion.dto.request.sale.SaleRequest;
import com.gestion.mappers.SaleMapper;
import com.gestion.model.Commerce;
import com.gestion.model.Sale;
import com.gestion.model.User;
import com.gestion.repository.JpaSaleRepository;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class SaleCreatorService {
    private final JpaSaleRepository jpaSaleRepository;
    private final EntityManager entityManager;

    public Sale createSale(SaleRequest request, Long userId, Long commerceId) {
        Sale sale = SaleMapper.toEntity(request);
        User userProxy = entityManager.getReference(User.class, userId);
        sale.setUser(userProxy);

        Commerce commerceProxy = entityManager.getReference(Commerce.class, commerceId);
        sale.setCommerce(commerceProxy);

        return jpaSaleRepository.save(sale);
    }

}
