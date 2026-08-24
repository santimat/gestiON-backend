package com.gestion.service.commerce;

import com.gestion.model.Commerce;
import com.gestion.repository.JpaCommerceRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceGetAllService {
    private final JpaCommerceRepository jpaCommerceRepository;

    public Page<Commerce> findAll(Pageable pageable){
        return jpaCommerceRepository.findAll(pageable);
    }
}
