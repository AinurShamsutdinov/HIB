package com.project.service;

import com.project.dao.abstraction.OrderDao;
import com.project.mail.MailService;
import com.project.model.*;
import com.project.service.abstraction.OrderService;
import com.project.service.abstraction.SendEmailService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
public class OrderServiceImpl implements OrderService {

    private OrderDao orderDAO;

    private SendEmailService sendEmailService;

    @Override
    public void addOrder(Order order) {
        orderDAO.add(order);
        try {
            sendEmailService.orderPresent(order);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateOrder(Order order) {
        orderDAO.update(order);
    }

    @Override
    public Order getOrderById(Long id) {
        return orderDAO.findById(id);
    }

    @Override
    public void deleteOrder(Order order) {
        for (CartItem cartItem : order.getItems()) {
            Book book = cartItem.getBook();
            book.setLastBookOrdered(false);
        }
        orderDAO.update(order);
        orderDAO.delete(order);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderDAO.findAll();
    }

    @Override
    public List<Order> getOrdersByUserId(Long id) {
        return orderDAO.getOrdersByUserId(id);
    }

    @Override
    public void completeOrder(Long id) {
        Order order = getOrderById(id);
        order.setStatus(Status.COMPLETED);
        for (CartItem cartItem : order.getItems()) {
            Book book = cartItem.getBook();
            book.setShow(false);
            book.setLastBookOrdered(false);
        }
        orderDAO.update(order);
    }

    @Override
    public void unCompleteOrder(Long id) {
        Order order = getOrderById(id);
        order.setStatus(Status.PROCESSING);
        for (CartItem cartItem : order.getItems()) {
            Book book = cartItem.getBook();
            book.setShow(true);
            book.setLastBookOrdered(true);
        }
        orderDAO.update(order);
    }

    @Override
    public void processOrder(Long id) {
        Order order = getOrderById(id);
        order.setStatus(Status.PROCESSING);
        for (CartItem cartItem : order.getItems()) {
            Book book = cartItem.getBook();
            book.setLastBookOrdered(true);
        }
        orderDAO.update(order);
    }

    @Override
    public int getCountOfOrders(long lastAuthDate) {
        return orderDAO.getCountOfOrders(lastAuthDate);
    }
}
