package com.gestion.dto.project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * La sesion de un usuario es valida si el usuario esta activo y, cuando tiene comercio, el
 * comercio tambien lo esta. Los usuarios sin comercio (SUDO) no dependen de ningun comercio.
 */
class UserActiveStatusTests {

    @Test
    @DisplayName("un usuario activo sin comercio tiene una sesion valida")
    void activeUserWithoutCommerceHasAValidSession() {
        assertThat(new UserActiveStatus(true, null).isSessionValid()).isTrue();
    }

    @Test
    @DisplayName("un usuario activo con comercio activo tiene una sesion valida")
    void activeUserWithActiveCommerceHasAValidSession() {
        assertThat(new UserActiveStatus(true, true).isSessionValid()).isTrue();
    }

    @Test
    @DisplayName("un usuario desactivado no tiene una sesion valida")
    void deactivatedUserHasAnInvalidSession() {
        assertThat(new UserActiveStatus(false, true).isSessionValid()).isFalse();
        assertThat(new UserActiveStatus(false, false).isSessionValid()).isFalse();
        assertThat(new UserActiveStatus(false, null).isSessionValid()).isFalse();
    }

    @Test
    @DisplayName("un usuario activo con comercio desactivado no tiene una sesion valida")
    void activeUserWithDeactivatedCommerceHasAnInvalidSession() {
        assertThat(new UserActiveStatus(true, false).isSessionValid()).isFalse();
    }
}
