//package com.gestion.controller.product;
//
//import com.gestion.mappers.ProductMapper;
//import com.gestion.service.product.ProductDeleterService;
//import lombok.AllArgsConstructor;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.DeleteMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("/api/products")
//@AllArgsConstructor
//public class ProductDeleterController {
//    private final ProductDeleterService productDeleterService;
//    private final ProductMapper productMapper;
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> delete(@PathVariable Long id) {
//        productDeleterService.delete(id);
//        return ResponseEntity.noContent().build();
//    }
//}
