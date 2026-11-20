package com.gestion.service.offer;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class OfferValidationPipeline {
    private final List<OfferValidator> validators;

    public void validate(OfferValidationContext ctx) {
        for (OfferValidator validator : validators) {
            validator.validate(ctx);
        }
    }

    @FunctionalInterface
    public interface OfferValidator {
        void validate(OfferValidationContext ctx);
    }
}