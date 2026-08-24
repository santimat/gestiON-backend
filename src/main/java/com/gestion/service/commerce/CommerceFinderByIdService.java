package com.gestion.service.commerce;

import com.gestion.exception.ResourceNotFoundException;
import com.gestion.model.Commerce;
import com.gestion.repository.JpaCommerceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceFinderByIdService {
    private final JpaCommerceRepository jpaCommerceRepository;

    public Commerce findBy(Long id){
        return jpaCommerceRepository.findById(id).
                orElseThrow(()-> new ResourceNotFoundException("Commerce with id " + id + "not found"));
    }
}
