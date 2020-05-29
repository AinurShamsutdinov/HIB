package com.project.controller.restcontroller;

import com.project.model.*;
import com.project.service.DataEnterInAdminPanelService;
import com.project.service.abstraction.*;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;


@RestController
@AllArgsConstructor
public class OrderController {

    private ShoppingCartService cartService;
    private OrderService orderService;
    private UserAccountService userAccountService;
    private BookService bookService;
    private DataEnterInAdminPanelService dataEnterInAdminPanelService;

    @PostMapping("/api/user/order/confirmaddress")
    private OrderDTO addOder(HttpSession httpSession) {
        ShoppingCartDTO shoppingCartDTO = null;
        OrderDTO order = new OrderDTO();
        if (httpSession.getAttribute("cartId") == null) {
            shoppingCartDTO = cartService.getCartById((Long) httpSession.getAttribute("cartId1click"));
            order.setItems(((ShoppingCartDTO) httpSession.getAttribute("shoppingcart")).getCartItems());
            order.setItemsCost((int) ((ShoppingCartDTO) httpSession.getAttribute("shoppingcart")).getTotalCostItems());
        } else {
            shoppingCartDTO = cartService.getCartById((Long) httpSession.getAttribute("cartId"));
            order.setItems(shoppingCartDTO.getCartItems());
            order.setItemsCost((int) shoppingCartDTO.getTotalCostItems());
        }
        order.setDate(Instant.now().getEpochSecond());
        order.setShippingCost(350);
        order.setStatus(Status.PROCESSING);
        Long userId = (Long) httpSession.getAttribute("userId");
        order.setUserAccount(userAccountService.getUserById(userId));
        httpSession.setAttribute("order", order);
        return order;
    }

    @PostMapping("/api/user/order/confirmContacts")
    private ContactsOfOrderDTO addContacts(HttpSession httpSession, @RequestBody ContactsOfOrderDTO contacts) {
        httpSession.setAttribute("contacts", contacts);
        return contacts;
    }

    @PostMapping("/order")
    private void confirmOrder(HttpSession httpSession) {
        ShoppingCartDTO shoppingCartDTO = new ShoppingCartDTO();
        OrderDTO order = (OrderDTO) httpSession.getAttribute("order");
        ContactsOfOrderDTO contacts = (ContactsOfOrderDTO) httpSession.getAttribute("contacts");
        order.setContacts(contacts);
        order.setComment(contacts.getComment());
        if (httpSession.getAttribute("cartId") == null) {

            order.setItems(((ShoppingCartDTO) httpSession.getAttribute("shoppingcart")).getCartItems());
            for (int i = 1; i <= ((ShoppingCartDTO) httpSession.getAttribute("shoppingcart")).getCartItems().size(); i++) {
                order.getItems().get(i - 1).setId((long) i + cartService.getMaxIdCartItem().size());
                shoppingCartDTO.addCartItem(order.getItems().get(i - 1).getBook());
            }
            cartService.updateCart(shoppingCartDTO);
        } else {
            shoppingCartDTO = cartService.getCartById((Long) httpSession.getAttribute("cartId"));
            shoppingCartDTO.getCartItems().clear();
            cartService.updateCart(shoppingCartDTO);
        }
        List<Long> listOfBooksIdInOrder = new ArrayList<>();
        for (CartItemDTO cartItem : order.getItems()) {
            listOfBooksIdInOrder.add(cartItem.getBook().getId());
        }
        bookService.setLastOrderedBooks(listOfBooksIdInOrder);
        orderService.addOrder(order.getOder());
        httpSession.removeAttribute("shoppingcart");
    }

    @GetMapping("/order/getorders")
    private List<OrderDTO> getOrder(HttpSession httpSession) {
        Long userId = (Long) httpSession.getAttribute("userId");
        List<Order> orderList = orderService.getOrdersByUserId(userId);
        List<OrderDTO> orderDTOS = new ArrayList<>();
        for (Order order : orderList) {
            orderDTOS.add(order.getOrderDTO());
        }
        return orderDTOS;
    }

    @GetMapping("/order/size")
    private int getOrderSize(HttpSession httpSession) {
        Long userId = (Long) httpSession.getAttribute("userId");
        return orderService.getOrdersByUserId(userId).size();
    }

    @GetMapping("/api/admin/getAllOrders")
    private List<OrderDTO> getAllOrders(HttpSession session) {
        List<Order> orderList = orderService.getAllOrders();
        List<OrderDTO> orderDTOS = new ArrayList<>();
        for (Order order : orderList) {
            orderDTOS.add(order.getOrderDTOForAdmin());
        }

        DataEnterInAdminPanel data = (DataEnterInAdminPanel) session.getAttribute("data");
        data.setDataEnterInOrders(Instant.now().getEpochSecond());
        dataEnterInAdminPanelService.update(data);
        session.setAttribute("data", data);
        return orderDTOS;
    }

    @GetMapping("/api/admin/order-count")
    private long getOrdersCount(HttpSession session) {
        if (session.getAttribute("data") == null) {
            session.setAttribute("data", dataEnterInAdminPanelService.findById(1L));
            DataEnterInAdminPanel data = (DataEnterInAdminPanel) session.getAttribute("data");
            return orderService.getCountOfOrders(data.getDataEnterInOrders());
        } else {
            DataEnterInAdminPanel data = (DataEnterInAdminPanel) session.getAttribute("data");
            return orderService.getCountOfOrders(data.getDataEnterInOrders());
        }
    }

    @PatchMapping("/api/admin/completeOrder/{id}")
    private void orderComplete(@PathVariable Long id) {
        orderService.completeOrder(id);
    }

    @PostMapping("/api/admin/deleteOrder/{id}")
    private void orderDelete(@PathVariable Long id) {
        orderService.deleteOrder(orderService.getOrderById(id));
    }
}
