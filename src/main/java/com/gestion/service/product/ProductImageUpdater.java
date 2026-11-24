package com.gestion.service.product;

import com.gestion.model.Product;
import com.gestion.properties.MinioProperties;
import com.gestion.service.file.FileFinderService;
import com.gestion.service.product.ProductImageUploaderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class ProductImageUpdater {
    private final ProductImageUploaderService imageUploader;
    private final FileFinderService fileFinder;
    private final MinioProperties minioProperties;

    public String updateImageIfPresent(MultipartFile image, Product product) {
        if (image != null) {
            imageUploader.uploadProductImage(image, product);
        }
        return fileFinder.getObjectUrl(product.getImageName(), minioProperties.dir().productImages());
    }
}