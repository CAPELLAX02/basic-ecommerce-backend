package com.capellax.ecommerce.api.controller.order;

import com.capellax.ecommerce.model.LocalUser;
import com.capellax.ecommerce.model.WebOrder;
import com.capellax.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public List<WebOrder> getOrders(
            @AuthenticationPrincipal LocalUser user
            ) {
        return orderService.getOrders(user);
    }

}
