package com.ecom.Service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.Model.Cart;
import com.ecom.Model.Category;
import com.ecom.Model.Product;
import com.ecom.Model.UserDetails;
import com.ecom.Repository.CartRepo;
import com.ecom.Repository.CategoryRepo;
import com.ecom.Repository.UserRepo;
import com.ecom.Repository.productRepo;
import com.ecom.Service.CartService;
import com.ecom.Service.CategoryService;

@Service
public class CartServiceImpl implements CartService {

	@Autowired
	private CartRepo cartRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private productRepo productRepo;

	@Override
	public Cart saveCart(Integer productId, Integer userId) {

		UserDetails userDetails = userRepo.findById(userId).get();
		Product product = productRepo.findById(productId).get();

		Cart cartStatus = cartRepo.findByProductIdAndUserId(productId,
				userId);/*
						 * What it does: Queries the cartRepo to check if a cart entry already exists
						 * for the specified product and user. Why: To decide whether to create a new
						 * cart entry or update the existing one.
						 */

		Cart cart = null;

		if (ObjectUtils.isEmpty(cartStatus)) {

			cart = new Cart();
			cart.setProduct(product);
			cart.setUser(userDetails);
			cart.setQuantity(1);
			cart.setTotalPrice(1 * product.getDiscountPrice());
		} else {

			cart = cartStatus;
			cart.setQuantity(cart.getQuantity() + 1);
			cart.setTotalPrice(cart.getQuantity() * cart.getProduct().getDiscountPrice());

		}

		Cart saveCart = cartRepo.save(cart);

		return saveCart;
	}

	@Override
	public List<Cart> getCartsByUser(Integer userId) {

		List<Cart> carts = cartRepo.findByUserId(userId);

		Double totalOrderPrice = 0.0;
		List<Cart> updateCarts = new ArrayList<>();

		for (Cart c : carts) {

			Double totalPrice = (c.getProduct().getDiscountPrice() * c.getQuantity());
			c.setTotalPrice(totalPrice);

			totalOrderPrice += totalPrice;
			c.setTotalOrderPrice(totalOrderPrice);
			updateCarts.add(c);
		}

		return updateCarts;
	}

	@Override
	public Integer getCountCart(Integer userId) {

		Integer countByUserId = cartRepo.countByUserId(userId);

		return countByUserId;
	}

	@Override
	public void updateQuantity(String sy, Integer cid) {

		Cart cart = cartRepo.findById(cid).get();

		Integer updateQuantity;

		if (sy.equalsIgnoreCase("de")) {

			updateQuantity = cart.getQuantity() - 1;

			if (updateQuantity <= 0) {

				cartRepo.delete(cart);

			} else {
				cart.setQuantity(updateQuantity);
				Cart update = cartRepo.save(cart);
			}

		} else {
			updateQuantity = cart.getQuantity() + 1;
			cart.setQuantity(updateQuantity);
			Cart update = cartRepo.save(cart);
		}

	}

}
