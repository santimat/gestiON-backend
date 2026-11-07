package com.gestion.service.offer;

import com.gestion.enums.OfferStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OfferStatusResolverService {

    public OfferStatus resolveOfferStatus(Instant startDate, Instant endDate, Instant now) {

        if (now.isBefore(startDate)) {
            return OfferStatus.SCHEDULED;
        } else if (now.isAfter(endDate)) {
            return OfferStatus.EXPIRED;
        } else {
            return OfferStatus.ACTIVE;
        }
    }
}
