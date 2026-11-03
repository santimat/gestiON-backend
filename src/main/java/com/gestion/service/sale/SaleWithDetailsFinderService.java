package com.gestion.service.sale;

import com.gestion.dto.response.sale.SaleResponse;
import com.gestion.mappers.SaleMapper;
import com.gestion.model.Sale;
import com.gestion.model.SaleDetail;
import com.gestion.repository.JpaSaleDetailRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class SaleWithDetailsFinderService {
    private final JpaSaleDetailRepository saleDetailRepository;
    private final SaleFinderByIdAndCommerceIdService saleFinderByIdAndCommerceIdService;

    public SaleResponse findBySaleIdAndCommerceId(Long saleId, Long CommerceId) {
        Sale sale = saleFinderByIdAndCommerceIdService.findSaleByIdAndCommerceId(saleId, CommerceId);
        List<SaleDetail> saleDetails = saleDetailRepository.findAllBySaleId(sale.getId());

        return SaleMapper.toResponse(sale, saleDetails);
    }
}
