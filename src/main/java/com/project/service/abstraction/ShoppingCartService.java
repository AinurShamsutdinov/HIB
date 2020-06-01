package com.project.service.abstraction;

import com.project.model.ShoppingCartDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface ShoppingCartService {
    ShoppingCartDTO getCartById(Long id);

    void updateCart(ShoppingCartDTO cart);

    void deleteCartItem(Long id);

    void mergeCarts(HttpServletRequest request, Long id);

    List getMaxIdCartItem();

    int getCartSize(Long cartId);
}
