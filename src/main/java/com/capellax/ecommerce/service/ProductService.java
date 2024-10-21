package com.capellax.ecommerce.service;

import com.capellax.ecommerce.model.Product;
import com.capellax.ecommerce.model.dao.ProductDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductDAO productDAO;

    public List<Product> getProducts() {
        return productDAO.findAll();
    }

}
