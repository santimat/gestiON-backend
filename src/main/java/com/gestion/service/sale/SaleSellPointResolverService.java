package com.gestion.service.sale;

import com.gestion.model.SellPoint;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class SaleSellPointResolverService {
    private final EntityManager entityManager;

    public SellPoint resolveSellPoint(Long sellPointId) {
        if (sellPointId == null) {
            return null;
        }
        // el id viene del JWT (generado con el user validado, no del request): proxy directo
        return entityManager.getReference(SellPoint.class, sellPointId);
    }
}
