package com.gestion.service.file;

/**
 * Construye la clave completa de un objeto en MinIO a partir del subdirectorio y del nombre del
 * objeto.
 *
 * El upload guarda el objeto como "subDir/objectName" pero devuelve solo "objectName", que es lo
 * que se persiste en la entidad. El finder anteponia el subDir para armar la URL, pero el deleter
 * no lo hacia: la clave nunca coincidia y el borrado era un no-op silencioso (MinIO no falla al
 * borrar una clave inexistente).
 *
 * Centralizar el armado de la clave evita que upload, finder y deleter vuelvan a divergir.
 */
public final class FileObjectKey {

    private static final String SEPARATOR = "/";

    private FileObjectKey() {
    }

    public static String of(String subDir, String objectName) {
        if (objectName == null || objectName.isBlank()) {
            return null;
        }
        if (subDir == null || subDir.isBlank()) {
            return objectName;
        }
        return subDir + SEPARATOR + objectName;
    }
}
