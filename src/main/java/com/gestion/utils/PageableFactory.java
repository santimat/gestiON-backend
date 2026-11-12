package com.gestion.utils;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.InvalidParameterException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

/**
 * Construye el Pageable de los endpoints paginados validando los parametros de entrada.
 *
 * Los controllers arman el PageRequest a mano, asi que esquivan el max-page-size que aplica
 * PageableHandlerMethodArgumentResolver y no tienen ningun tope: con ?size=1000000 un endpoint
 * paginado se convierte en un dump de la tabla. Peor: la query agregada del resumen de cuenta
 * de los clientes expande IN :clientIds a un placeholder por id y MySQL corta en 65.535
 * placeholders, asi que una pagina grande revienta con un error SQL.
 *
 * Ademas, un page negativo o un size cero hacen que PageRequest.of lance IllegalArgumentException
 * y un sortBy desconocido hace que Hibernate lance PropertyReferenceException: los dos terminaban
 * en un 500 INTERNAL_ERROR, cuando son errores del cliente.
 *
 * Por eso los parametros fuera de rango se rechazan con un 400 INVALID_PARAMETER en vez de
 * clampearse en silencio: el cliente tiene que saber que lo que pidio no se puede servir.
 */
public final class PageableFactory {

    public static final int MAX_PAGE_SIZE = 100;

    private PageableFactory() {
    }

    /**
     * @param sortBy      propiedad por la que se ordena, tiene que estar en allowedSortProperties
     * @param sortDir     direccion del orden: asc o desc
     * @param allowedSortProperties allow-list de las propiedades ordenables del recurso
     */
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
