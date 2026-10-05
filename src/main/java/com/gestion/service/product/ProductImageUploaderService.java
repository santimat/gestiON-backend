package com.gestion.service.product;

import com.gestion.model.Product;
import com.gestion.properties.MinioProperties;
import com.gestion.repository.JpaProductRepository;
import com.gestion.service.file.FileDeleterService;
import com.gestion.service.file.FileUploaderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@AllArgsConstructor
public class ProductImageUploaderService {
    private final JpaProductRepository productRepository;
    private final FileUploaderService fileUploaderService;
    private final FileDeleterService fileDeleterService;
    private final MinioProperties minioProperties;

    public String uploadProductImage(MultipartFile image, Product product) {

        if (product.getImageName() != null) {
            fileDeleterService.deleteFile(product.getImageName());
        }

        String imageName = fileUploaderService.uploadFile(image, minioProperties.dir().productImages());

        product.setImageName(imageName);
        productRepository.save(product);

        return imageName;
    }
}
