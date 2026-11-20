package com.gestion.service.client;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ClientSearchTextNormalizerService {

    public String normalize(String searchText, String name, String lastName) {
        String effectiveSearchText = searchText;

        if ((effectiveSearchText == null || effectiveSearchText.isBlank())
                && (name != null || lastName != null)) {
            StringBuilder builder = new StringBuilder();
            if (name != null) {
                builder.append(name).append(" ");
            }
            if (lastName != null) {
                builder.append(lastName);
            }
            effectiveSearchText = builder.toString().trim();
            if (effectiveSearchText.isBlank()) {
                effectiveSearchText = null;
            }
        } else if (effectiveSearchText != null && effectiveSearchText.isBlank()) {
            effectiveSearchText = null;
        }

        return effectiveSearchText;
    }
}