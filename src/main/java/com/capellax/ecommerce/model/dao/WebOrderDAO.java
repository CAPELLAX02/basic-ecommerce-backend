package com.capellax.ecommerce.model.dao;

import com.capellax.ecommerce.model.LocalUser;
import com.capellax.ecommerce.model.WebOrder;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface WebOrderDAO extends ListCrudRepository<WebOrder, Long> {

    List<WebOrder> findByUser(LocalUser user);

}
