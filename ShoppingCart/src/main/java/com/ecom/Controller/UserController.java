package com.ecom.Controller;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.Model.Cart;
import com.ecom.Model.Category;
import com.ecom.Model.OrderRequest;
import com.ecom.Model.ProductOrder;
import com.ecom.Model.UserDetails;
import com.ecom.Service.CartService;
import com.ecom.Service.CategoryService;
import com.ecom.Service.OrderService;
import com.ecom.Service.UserService;
import com.ecom.Util.CommonUtil;
import com.ecom.Util.OrderStatus;

import jakarta.mail.MessagingException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private CartService cartService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private OrderService orderService;

	@ModelAttribute
	private void getUserDetails(Principal p, Model m) {

		if (p != null) {

			String email = p.getName();
			UserDetails userDetails = userService.getUserByEmail(email);
			m.addAttribute("user", userDetails);
			System.out.println("User logged in: " + userDetails);
			Integer countCart = cartService.getCountCart(userDetails.getId());
			m.addAttribute("countCart", countCart);

		}
		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("category", allActiveCategory);

	}

	@GetMapping("/")
	public String home() {

		return "user/index";

	}

	@GetMapping("/addCart")
	public String addToCart(@RequestParam int pid, @RequestParam int uid, RedirectAttributes redirectAttributes) {

		Cart saveCart = cartService.saveCart(pid, uid);

		if (ObjectUtils.isEmpty(saveCart)) {

			redirectAttributes.addFlashAttribute("errorMsg", "Prduct to cart failed");

		} else {
			redirectAttributes.addFlashAttribute("successMsg", "Prduct added to cart");

		}

		return "redirect:/product/" + pid;

	}

	@GetMapping("/cart")
	public String loadCartPage(Principal p, Model m) {

		UserDetails userDetails = getLoggedInUserDetails(p);

		List<Cart> carts = cartService.getCartsByUser(userDetails.getId());

		m.addAttribute("carts", carts);
		if (carts.size() > 0) {
			Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			m.addAttribute("totalOrderPrice", totalOrderPrice); // last index product // the total order price
			// to get that we do
			// size - 1 (for last
			// index)
			// have

		}

		return "/user/cart";

	}

	private UserDetails getLoggedInUserDetails(Principal p) {

		String email = p.getName();
		UserDetails userDetails = userService.getUserByEmail(email);

		return userDetails;
	}

	@GetMapping("/cartQuantityUpdate")
	public String cartQuantityUpdate(@RequestParam String sy, @RequestParam Integer cid) {

		cartService.updateQuantity(sy, cid);

		return "redirect:/user/cart";
	}

	@GetMapping("/orders")
	public String orderPage(Principal p, Model m) {

		UserDetails userDetails = getLoggedInUserDetails(p);

		List<Cart> carts = cartService.getCartsByUser(userDetails.getId());

		m.addAttribute("carts", carts);
		if (carts.size() > 0) {
			Double orderPrice = carts.get(carts.size() - 1).getTotalOrderPrice();
			Double totalOrderPrice = carts.get(carts.size() - 1).getTotalOrderPrice() + 250 + 100;
			m.addAttribute("orderPrice", orderPrice);
			m.addAttribute("totalOrderPrice", totalOrderPrice);

		}

		return "/user/order";

	}

	@PostMapping("/save-order")
	public String savOorder(@ModelAttribute OrderRequest request, Principal p)
			throws UnsupportedEncodingException, MessagingException {

		// System.out.println(request);

		UserDetails user = getLoggedInUserDetails(p);

		orderService.saveOrder(user.getId(), request);

		return "redirect:/user/success";
	}

	@GetMapping("/success")
	public String loadSuccess() {

		return "/user/success";

	}

	@GetMapping("/user-orders")
	public String myOrders(Model m, Principal p) {

		UserDetails loggedUser = getLoggedInUserDetails(p);

		List<ProductOrder> orders = orderService.getOrderByUser(loggedUser.getId());

		m.addAttribute("orders", orders);

		return "/user/my_orders";

	}

	@GetMapping("/update-status")
	public String updateOrderStatus(@RequestParam Integer id, @RequestParam Integer st,
			RedirectAttributes redirectAttributes) throws UnsupportedEncodingException, MessagingException {

		OrderStatus[] values = OrderStatus.values();
		System.out.println("valuesssssss:" + values);
		String status = null;

		for (OrderStatus orderst : values) {

			if (orderst.getId().equals(st)) {

				status = orderst.getName();
			}

		}

		ProductOrder updateOrderStatus = orderService.updateOrderStatus(id, status);

		commonUtil.sendMailProductOrder(updateOrderStatus, status);

		if (updateOrderStatus != null) {

			redirectAttributes.addFlashAttribute("successMsg", "Status update");

		} else {
			redirectAttributes.addFlashAttribute("errorMsg", "status not updated");

		}

		return "redirect:/user/user-orders";

	}

	@GetMapping("/profile")
	public String profile() {
		return "/user/profile";
	}

	@PostMapping("/update-profile")
	public String updateProfile(@ModelAttribute UserDetails user, @RequestParam MultipartFile img,
			RedirectAttributes redirectAttributes) {
		UserDetails updateUserProfile = userService.updateUserProfile(user, img);
		if (ObjectUtils.isEmpty(updateUserProfile)) {
			redirectAttributes.addFlashAttribute("errorMsg", "Profile not updated");
		} else {
			redirectAttributes.addFlashAttribute("successMsg", "Profile Updated");
		}
		return "redirect:/user/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
			RedirectAttributes redirectAttributes) {

		UserDetails loggedInUserDetails = getLoggedInUserDetails(p);

		boolean matches = passwordEncoder.matches(currentPassword, loggedInUserDetails.getPassword());

		if (matches) {

			String encodePassword = passwordEncoder.encode(newPassword);

			loggedInUserDetails.setPassword(encodePassword);

			UserDetails updateUser = userService.updateUser(loggedInUserDetails);

			if (ObjectUtils.isEmpty(updateUser)) {
				redirectAttributes.addFlashAttribute("errorMsg", "Password not Updated !! Something wrong on server");

			} else {
				redirectAttributes.addFlashAttribute("successMsg", "Password Updated Successfully");

			}

		} else {
			redirectAttributes.addFlashAttribute("errorMsg", "Current Password incorrect");

		}

		return "redirect:/user/profile";

	}

}
