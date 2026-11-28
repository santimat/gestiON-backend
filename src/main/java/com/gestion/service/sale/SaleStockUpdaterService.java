package com.gestion.service.sale;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.InsufficientStockException;
import com.gestion.model.Product;
import com.gestion.model.SaleDetail;
import com.gestion.repository.JpaProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;


@Service
@AllArgsConstructor
public class SaleStockUpdaterService {

    private final JpaProductRepository productRepository;

    @Transactional
    public void applyStockDecrement(List<SaleDetail> details, Long commerceId) {
        List<SaleDetail> orderedDetails = details.stream()
                .sorted(Comparator.comparing(detail -> detail.getProduct().getId()))
                .toList();

        for (SaleDetail detail : orderedDetails) {
            Product product = detail.getProduct();
            int updatedRows = productRepository.decrementStock(product.getId(), commerceId, detail.getQuantity());

            if (updatedRows == 0) {
                // otra venta consumio el stock entre la validacion y el descuento
                throw new InsufficientStockException(ErrorCode.INSUFFICIENT_STOCK,
                        "Not enough stock for product '" + product.getName()
                                + "' (requested: " + detail.getQuantity() + ")");
            }
        }
    }
}
