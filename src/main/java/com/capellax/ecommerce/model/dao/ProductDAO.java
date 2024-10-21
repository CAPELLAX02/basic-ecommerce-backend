package com.capellax.ecommerce.model.dao;

import com.capellax.ecommerce.model.Product;
import org.springframework.data.repository.ListCrudRepository;

public interface ProductDAO extends ListCrudRepository<Product, Long> {



}
