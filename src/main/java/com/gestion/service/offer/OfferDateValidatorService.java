package com.gestion.service.offer;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.InvalidDateException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class OfferDateValidatorService {

    public void validateOfferDates(Instant startDate, Instant endDate, Instant now) {
        if (startDate.isBefore(now)) {
            throw new InvalidDateException(ErrorCode.INVALID_DATE, "Offer start date cannot be in the past");
        }
        if (endDate.isBefore(startDate)) {
            throw new InvalidDateException(ErrorCode.INVALID_DATE, "Offer end date cannot be before start date");
        }
    }
}
