package com.gestion.mappers;

import com.gestion.dto.request.sale.SaleDetailRequest;
import com.gestion.dto.response.sale.SaleDetailResponse;
import com.gestion.model.Product;
import com.gestion.model.Sale;
import com.gestion.model.SaleDetail;

public class SaleDetailMapper {

    public static SaleDetail toEntity(SaleDetailRequest request, Product product, Sale sale) {
        SaleDetail saleDetail = new SaleDetail();
        saleDetail.setSale(sale);
        saleDetail.setProduct(product);
        // el precio queda registrado tal como estaba publicado al momento de la venta
        saleDetail.setPrice(product.getSalePrice());
        saleDetail.setQuantity(request.quantity());
        return saleDetail;
    }

    public static SaleDetailResponse toResponse(SaleDetail saleDetail) {
        return new SaleDetailResponse(
                saleDetail.getId(),
                saleDetail.getProduct().getId(),
                saleDetail.getProduct().getName(),
                saleDetail.getPrice(),
                saleDetail.getQuantity()
        );
    }
}
