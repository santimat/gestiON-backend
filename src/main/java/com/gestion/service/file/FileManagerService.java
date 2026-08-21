package com.gestion.service.file;

import com.gestion.exception.FileManagerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

// TODO esto hay que reveerlo ya que deberíamos usar un servicio en la nube
@Service
public class FileManagerService {
    private final Path uploadsDir;

    public FileManagerService(
            // Se inyecta la ruta definida en application.properties
            @Value("${app.uploads.dir}") Path uploadsDir) {
        this.uploadsDir = uploadsDir;

        try {
            // Crea el directorio solo si no existe
            Files.createDirectory(uploadsDir);
        } catch (IOException e) {
            throw new FileManagerException("Could not create uploads directory" + e.getMessage());
        }
    }

    public String saveFile(MultipartFile file, String subdirectory) {
        if (file == null || file.isEmpty()) throw new FileManagerException("File is empty or null");

        try {
            String originalFileName = file.getOriginalFilename();
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                // Recortamos lo que está despues del .
                // lastIndexOf nos da la posición del ultimo punto en el nombre del archivo
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            // Nombre unico para el archivo, importante agregar la extensión
            String nameToSaveFile = UUID.randomUUID() + extension;
            // creamos la ruta donde se va a guardar agregandole la subcarpeta.
            // sería algo así /uploads/subdirectory/uuid.extension
            Path pathToSaveFile = this.uploadsDir.resolve(subdirectory + nameToSaveFile);

            // createDirectories se usa para manejar archivos con varios niveles de carpetas.
            // si la carpeta padre no existiera esto lo soluciona creandola
            Files.createDirectories(pathToSaveFile.getParent());

            // el inputStream son los datos binarios guardados en el archivo que llega por request
            // pathToSaveFile va a tener la ruta y el nombre con el que queremos guardar el archivo
            // StandardCopyOption.REPLACE_EXISTING se usa para reemplazar el archivo si ya existiera, ya que por
            // defecto tiraría una excepción, esto nos da un poco más de seguridad.
            // hay otras maneras de hacer esto como simplemente transferir esos binarios, pero este metodo nos deja un poco más de control sobre el proceso.
            Files.copy(file.getInputStream(), pathToSaveFile, StandardCopyOption.REPLACE_EXISTING);

            // devolvemos la ruta donde se guardó para despues pasarsela a la base de datos y poder acceder a ella.
            return pathToSaveFile.toString();

        } catch (IOException e) {
            throw new FileManagerException("Could not save file: " + e.getMessage());
        }
    }
}
