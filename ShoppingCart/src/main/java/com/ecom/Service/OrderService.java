package com.ecom.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.data.domain.Page;

import com.ecom.Model.OrderRequest;
import com.ecom.Model.ProductOrder;

import jakarta.mail.MessagingException;

public interface OrderService {

	public void saveOrder(Integer userId, OrderRequest request) throws UnsupportedEncodingException, MessagingException;
	
	public List<ProductOrder> getOrderByUser(Integer userId);
	
	public ProductOrder updateOrderStatus(Integer id , String st);

	public List<ProductOrder> getAllOrders();

	public ProductOrder getOrdersByOrderId(String orderId);
	
	public Page<ProductOrder> getAllOrdersWithPagination(Integer pageNo, Integer pageSize);

}
