package com.ecom.Service.impl;

import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.ecom.Model.Cart;
import com.ecom.Model.OrderAddress;
import com.ecom.Model.OrderRequest;
import com.ecom.Model.ProductOrder;
import com.ecom.Repository.CartRepo;
import com.ecom.Repository.ProductOrderRepo;
import com.ecom.Service.OrderService;
import com.ecom.Util.CommonUtil;
import com.ecom.Util.OrderStatus;

import jakarta.mail.MessagingException;

@Service
public class OrderServiceImpl implements OrderService {

	@Autowired
	private ProductOrderRepo productOrderRepo;

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private CommonUtil commonUtil;

	@Override
	public void saveOrder(Integer userId, OrderRequest orderRequest)
			throws UnsupportedEncodingException, MessagingException {

		List<Cart> carts = cartRepo.findByUserId(userId);

		for (Cart cart : carts) {

			ProductOrder order = new ProductOrder();
			order.setOrderId(UUID.randomUUID().toString());
			order.setOrderDate(LocalDate.now());

			order.setProduct(cart.getProduct());

			order.setPrice(cart.getProduct().getDiscountPrice());

			order.setQuantity(cart.getQuantity());

			order.setUser(cart.getUser());

			order.setStatus("In Progress");

			order.setStatus(OrderStatus.IN_PROGRESS.getName());

			order.setPaymentType(orderRequest.getPaymentType());

			OrderAddress address = new OrderAddress();

			address.setFirstName(orderRequest.getFirstName());
			address.setLastName(orderRequest.getLastName());
			address.setEmail(orderRequest.getEmail());
			address.setMobileNo(orderRequest.getMobileNo());
			address.setAddress(orderRequest.getAddress());
			address.setCity(orderRequest.getCity());
			address.setState(orderRequest.getState());
			address.setPincode(orderRequest.getPincode());

			order.setOrderAddress(address);

			ProductOrder saveOrder = productOrderRepo.save(order);

			commonUtil.sendMailProductOrder(saveOrder, "Successful");

		}

	}

	@Override
	public List<ProductOrder> getOrderByUser(Integer userId) {

		List<ProductOrder> orders = productOrderRepo.findByUserId(userId);

		return orders;
	}

	@Override
	public ProductOrder updateOrderStatus(Integer id, String st) {

		Optional<ProductOrder> findById = productOrderRepo.findById(id);

		if (findById.isPresent()) {

			ProductOrder productOrder = findById.get();

			productOrder.setStatus(st);

			ProductOrder updateOrder = productOrderRepo.save(productOrder);

			return updateOrder;

		}

		return null;

	}

	@Override
	public List<ProductOrder> getAllOrders() {

		return productOrderRepo.findAll();

	}

	@Override
	public Page<ProductOrder> getAllOrdersWithPagination(Integer pageNo, Integer pageSize) {
		
		Pageable pageable = PageRequest.of(pageNo,pageSize);
		
		return productOrderRepo.findAll(pageable);
	}

	@Override
	public ProductOrder getOrdersByOrderId(String orderId) {

		return productOrderRepo.findByOrderId(orderId);

	}

}
