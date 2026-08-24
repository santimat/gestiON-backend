package com.gestion.service.commerce;

import com.gestion.model.Commerce;
import com.gestion.repository.JpaCommerceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceDeleterService {
    private final JpaCommerceRepository jpaCommerceRepository;
    private final CommerceFinderByIdService commerceFinderByService;

    public void delete(Long id){
        Commerce commerce = commerceFinderByService.findBy(id);
        jpaCommerceRepository.delete(commerce);
    }
}
