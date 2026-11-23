package com.gestion.utils;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.InvalidParameterException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public final class PageableFactory {

    public static final int MAX_PAGE_SIZE = 100;

    private PageableFactory() {
    }


    public static Pageable create(int page, int size, String sortBy, String sortDir,
                                  Set<String> allowedSortProperties) {
        if (page < 0) {
            throw new InvalidParameterException(ErrorCode.INVALID_PARAMETER,
                    "Parameter 'page' must be greater than or equal to 0");
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new InvalidParameterException(ErrorCode.INVALID_PARAMETER,
                    "Parameter 'size' must be between 1 and " + MAX_PAGE_SIZE);
        }

        if (!allowedSortProperties.contains(sortBy)) {
            throw new InvalidParameterException(ErrorCode.INVALID_PARAMETER,
                    "Parameter 'sortBy' must be one of " + allowedSortProperties);
        }

        Sort.Direction direction;
        try {
            direction = Sort.Direction.fromString(sortDir);
        } catch (IllegalArgumentException exception) {
            throw new InvalidParameterException(ErrorCode.INVALID_PARAMETER,
                    "Parameter 'sortDir' must be 'asc' or 'desc'");
        }

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}
