package com.gestion.service.commerce;

import com.gestion.repository.JpaCommerceRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CommerceUpdaterService {
    private final JpaCommerceRepository commerceRepository;
}
