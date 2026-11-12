package com.gestion.dto.project;

/**
 * Proyeccion minima para revalidar el estado de una sesion sin cargar la entidad User.
 *
 * El JwtFilter corre en cada request autenticado, asi que la consulta tiene que ser lo mas
 * barata posible: seselectionan solo los dos flags que decides si la sesion sigue viva, en
 * lugar de cargar la entidad con sus relaciones (comercio, punto de venta).
 *
 * @param userActive    flag active del usuario
 * @param commerceActive flag active del comercio del usuario, null si el usuario no tiene comercio
 */
public record UserActiveStatus(
        boolean userActive,
        Boolean commerceActive
) {
    /**
     * Una sesion es valida si el usuario esta activo y, cuando tiene comercio, el comercio
     * tambien lo esta. Los usuarios sin comercio (SUDO) no dependen del estado de ningun
     * comercio.
     */
    public boolean isSessionValid() {
        if (!userActive) {
            return false;
        }
        return commerceActive == null || commerceActive;
    }
}
