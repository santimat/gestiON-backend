package com.gestion.service.sale;

import com.gestion.dto.request.sale.SaleDetailRequest;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class SaleDetailsMergerService {

    public Map<Long, Integer> mergeDetails(Collection<SaleDetailRequest> details) {
        Map<Long, Integer> quantitiesByProductId = new LinkedHashMap<>();
        for (SaleDetailRequest detailRequest : details) {
            quantitiesByProductId.merge(detailRequest.productId(), detailRequest.quantity(), Integer::sum);
        }
        return quantitiesByProductId;
    }
}
