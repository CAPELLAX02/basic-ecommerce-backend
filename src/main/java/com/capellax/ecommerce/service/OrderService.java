package com.capellax.ecommerce.service;

import com.capellax.ecommerce.model.LocalUser;
import com.capellax.ecommerce.model.WebOrder;
import com.capellax.ecommerce.model.dao.WebOrderDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final WebOrderDAO webOrderDAO;

    public List<WebOrder> getOrders(LocalUser user) {
        return webOrderDAO.findByUser(user);
    }

}
