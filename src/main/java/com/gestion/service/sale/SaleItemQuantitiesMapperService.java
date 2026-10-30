package com.gestion.service.sale;

import com.gestion.dto.project.SaleItemCount;
import com.gestion.repository.JpaSaleDetailRepository;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
public class SaleItemQuantitiesMapperService {

    private final JpaSaleDetailRepository jpaSaleDetailRepository;

    public Map<Long, Long> mapSaleItemQuantities(List<Long> saleIds) {
        if (saleIds.isEmpty()) {
            return Map.of();
        }
        // cada elemento de la lista es un objeto SaleItemCount. collect (similar a reducer) y usamos el metodo
        // estatico de Collectors para convertirlo a un hashmap
        return jpaSaleDetailRepository.countItemsBySaleIds(saleIds).stream().collect(Collectors.toMap(
                SaleItemCount::saleId,
                SaleItemCount::itemCount
        ));
    }
}
