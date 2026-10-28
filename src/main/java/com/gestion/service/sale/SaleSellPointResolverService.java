package com.gestion.service.sale;

import com.gestion.model.SellPoint;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Resuelve el punto de venta de una venta desde el id que viaja en el JWT.
 * OWNER/SUDO (y tokens viejos sin claim) no tienen punto de venta asociado:
 * en ese caso devuelve null y la venta queda registrada sin punto.
 */
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
