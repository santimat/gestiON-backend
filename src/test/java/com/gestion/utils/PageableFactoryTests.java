package com.gestion.utils;

import com.gestion.enums.ErrorCode;
import com.gestion.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Los endpoints paginados arman el PageRequest a mano, asi que esquivan el max-page-size de
 * Spring y no tienen ningun tope: con ?size=1000000 un endpoint paginado se convierte en un
 * dump de la tabla. Un page negativo o un size cero hacian que PageRequest.of lanzara
 * IllegalArgumentException y un sortBy desconocido hacia que Hibernate lanzara
 * PropertyReferenceException: los dos terminaban en un 500 por un error del cliente.
 */
class PageableFactoryTests {

    private static final Set<String> ALLOWED = Set.of("id", "name", "createdAt");

    @Test
    @DisplayName("un page y un size validos arman el pageable con el orden pedido")
    void validParametersBuildThePageable() {
        Pageable pageable = PageableFactory.create(2, 20, "name", "desc", ALLOWED);

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort().getOrderFor("name").getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    @DisplayName("el size maximo es 100")
    void maximumSizeIsAccepted() {
        assertThatCode(() -> PageableFactory.create(0, PageableFactory.MAX_PAGE_SIZE, "id", "asc", ALLOWED))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("un page negativo se rechaza")
    void negativePageIsRejected() {
        assertThatThrownBy(() -> PageableFactory.create(-1, 10, "id", "asc", ALLOWED))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getCode())
                .isEqualTo(ErrorCode.INVALID_PARAMETER);
    }

    @Test
    @DisplayName("un size de cero se rechaza")
    void zeroSizeIsRejected() {
        assertThatThrownBy(() -> PageableFactory.create(0, 0, "id", "asc", ALLOWED))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getCode())
                .isEqualTo(ErrorCode.INVALID_PARAMETER);
    }

    @Test
    @DisplayName("un size negativo se rechaza")
    void negativeSizeIsRejected() {
        assertThatThrownBy(() -> PageableFactory.create(0, -5, "id", "asc", ALLOWED))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getCode())
                .isEqualTo(ErrorCode.INVALID_PARAMETER);
    }

    @Test
    @DisplayName("un size por encima del maximo se rechaza en vez de clampearse")
    void sizeAboveMaximumIsRejected() {
        assertThatThrownBy(() -> PageableFactory.create(0, PageableFactory.MAX_PAGE_SIZE + 1, "id", "asc", ALLOWED))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getCode())
                .isEqualTo(ErrorCode.INVALID_PARAMETER);
    }

    @Test
    @DisplayName("una propiedad de orden que no esta en la allow-list se rechaza")
    void sortPropertyOutsideTheAllowListIsRejected() {
        assertThatThrownBy(() -> PageableFactory.create(0, 10, "password", "asc", ALLOWED))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getCode())
                .isEqualTo(ErrorCode.INVALID_PARAMETER);
    }

    @Test
    @DisplayName("una direccion de orden invalida se rechaza")
    void invalidSortDirectionIsRejected() {
        assertThatThrownBy(() -> PageableFactory.create(0, 10, "id", "lateral", ALLOWED))
                .isInstanceOf(BusinessException.class)
                .extracting(exception -> ((BusinessException) exception).getCode())
                .isEqualTo(ErrorCode.INVALID_PARAMETER);
    }
}
