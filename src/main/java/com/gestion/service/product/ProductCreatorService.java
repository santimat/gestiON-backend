package com.gestion.service.product;

import com.gestion.dto.request.product.ProductRequest;
import com.gestion.mappers.ProductMapper;
import com.gestion.model.Category;
import com.gestion.model.Commerce;
import com.gestion.model.Product;
import com.gestion.repository.JpaProductRepository;
import com.gestion.service.category.CategoryFinderByIdService;
import jakarta.persistence.EntityManager;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductCreatorService {
    private final JpaProductRepository productRepository;
    private final CategoryFinderByIdService categoryFinderByIdService;
    private final ProductMapper productMapper;
    private final EntityManager entityManager;

    public Product createProduct(ProductRequest request) {

        Category category = categoryFinderByIdService.findById(request.categoryId());
        Product newProduct = productMapper.toEntity(request);
        newProduct.setCategory(category);

        // para crear el proxy tomamos la referencía de la clase con Commerce.class y luego le pasamos el id del comercio que viene en el request
        // Este va a ser una instancia del objeto Commerce que solo vive en memoria y va a contener simplemente el id del comercio.
        Commerce commerceProxy = entityManager.getReference(Commerce.class, request.commerceId());
        newProduct.setCommerce(commerceProxy);

        // TODO: trabajar lo de la imagen
        return productRepository.save(newProduct);
    }
}
