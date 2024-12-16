package com.ecom.Service;

import java.util.List;

import com.ecom.Model.Cart;

public interface CartService {

	public Cart saveCart(Integer productId,Integer userId);
	
	public List<Cart> getCartsByUser(Integer userId);

	public Integer getCountCart(Integer userId);

	public void updateQuantity(String sy, Integer cid);
}
