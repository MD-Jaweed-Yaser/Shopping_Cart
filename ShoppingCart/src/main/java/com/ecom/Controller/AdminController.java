package com.ecom.Controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.Model.Category;
import com.ecom.Model.Product;
import com.ecom.Model.ProductOrder;
import com.ecom.Model.UserDetails;
import com.ecom.Service.CartService;
import com.ecom.Service.CategoryService;
import com.ecom.Service.OrderService;
import com.ecom.Service.ProductService;
import com.ecom.Service.UserService;
import com.ecom.Util.CommonUtil;
import com.ecom.Util.OrderStatus;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminController {

	@Autowired
	private CategoryService categoryService;

	@Autowired
	private ProductService productService;

	@Autowired
	UserService userService;

	@Autowired
	private CartService cartService;

	@Autowired
	private CommonUtil commonUtil;

	@Autowired
	private OrderService orderService;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@ModelAttribute
	public void getUserDetails(Principal p, Model m) {
		if (p != null) {
			String email = p.getName();
			UserDetails userDtls = userService.getUserByEmail(email);
			m.addAttribute("user", userDtls);
			Integer countCart = cartService.getCountCart(userDtls.getId());
			m.addAttribute("countCart", countCart);
		}

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();
		m.addAttribute("categorys", allActiveCategory);
	}

	@GetMapping("/")
	public String index() {

		return "admin/index";
	}

	@GetMapping("/loadAddProduct")
	public String addproduct(Model m) {

		m.addAttribute("categories", categoryService.getAllCategory());
		return "admin/add_product";
	}

	@GetMapping("/addCategory") // with pagination
	public String addCategory(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

		// m.addAttribute("categorys", categoryService.getAllCategory());

//		   if (pageNo < 0) pageNo = 0; // Default to first page if invalid
//		    if (pageSize < 1) pageSize = 2; // Default page size

		Page<Category> page = categoryService.getAllCategoryWithPagination(pageNo, pageSize);

		List<Category> categories = page.getContent();
		m.addAttribute("categories", categories);

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/add_category";
	}

	@PostMapping("/saveCategory")
	public String saveCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) throws IOException {
		// Log to debug
		System.out.println("Received category: " + category); // Debugging

		if (category == null) {
			redirectAttributes.addAttribute("errorMsg", "Category is not being bound to the model");
			return "redirect:/admin/addCategory";
		}

		// Your logic here
		String imageName = file != null ? file.getOriginalFilename() : "default.jpg";
		category.setImageName(imageName);

		Boolean existCategory = categoryService.existCategory(category.getName());
		if (existCategory) {
			redirectAttributes.addFlashAttribute("errorMsg", "Category Name already exists");
		} else {
			Category saveCategory = categoryService.saveCategory(category);
			if (saveCategory == null) {
				redirectAttributes.addFlashAttribute("errorMsg", "Not saved! Internal server error");
			} else {
				File saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				redirectAttributes.addFlashAttribute("successMsg", "Saved successfully");
			}
		}
		return "redirect:/admin/addCategory";
	}

	@GetMapping("/deleteCategory/{id}")
	public String deleteCategory(@PathVariable int id, HttpSession session) {
		Boolean deleteCategory = categoryService.deleteCategory(id);

		if (deleteCategory) {
			session.setAttribute("succMsg", "category delete success");
		} else {
			session.setAttribute("errorMsg", "something wrong on server");
		}

		return "redirect:/admin/addCategory";
	}

	@GetMapping("/loadEditCategory/{id}")
	public String loadEditCategory(@PathVariable int id, Model m) {
		m.addAttribute("category", categoryService.getCategoryById(id));
		return "admin/edit_category";
	}

	@PostMapping("/updateCategory")
	public String updateCategory(@ModelAttribute Category category, @RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) throws IOException {

		Category oldCategory = categoryService.getCategoryById(category.getId());
		String imageName = file.isEmpty() ? oldCategory.getImageName() : file.getOriginalFilename();

		if (!ObjectUtils.isEmpty(category)) {

			oldCategory.setName(category.getName());
			oldCategory.setIsActive(category.getIsActive());
			oldCategory.setImageName(imageName);
		}

		Category updateCategory = categoryService.saveCategory(oldCategory);

		if (!ObjectUtils.isEmpty(updateCategory)) {

			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "category_img" + File.separator
						+ file.getOriginalFilename());

				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			redirectAttributes.addFlashAttribute("successMsg", "Category update success");
		} else {
			redirectAttributes.addFlashAttribute("errorMsg", "Something went wrong on the server");
		}

		return "redirect:/admin/loadEditCategory/" + category.getId();
	}

	@PostMapping("/addProduct")
	public String saveProduct(@ModelAttribute Product product, @RequestParam("file") MultipartFile file,
			RedirectAttributes redirectAttributes) throws IOException {

		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		product.setImageName(imageName);

		product.setDiscount(0);
		product.setDiscountPrice(product.getPrice());

		Product saveProduct = productService.saveProduct(product);

		if (!ObjectUtils.isEmpty(saveProduct)) {
			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
						+ file.getOriginalFilename());

				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}
			redirectAttributes.addFlashAttribute("successMsg", "Product Saved Successfully");
		} else {
			redirectAttributes.addFlashAttribute("errorMsg", "Something on server");

		}

		return "redirect:/admin/loadAddProduct";
	}

	@GetMapping("/products") // also for search products // also with pagination
	public String loadViewProduct(Model m, @RequestParam(defaultValue = "") String ch,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

//		List<Product> products = null;
//		if (ch != null && ch.length() > 0) {
//			products = productService.searchProduct(ch);
//		} else {
//			products = productService.getAllProducts();
//		}
//		m.addAttribute("products", products);

		Page<Product> page = null;
		if (ch != null && ch.length() > 0) {
			page = productService.searchProductWithPagination(pageNo, pageSize, ch);
		} else {
			page = productService.getAllProductsWithPagination(pageNo, pageSize);
		}
		m.addAttribute("products", page.getContent());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "admin/products";
	}

	@GetMapping("/deleteProduct/{id}")
	public String loadViewProduct(@PathVariable int id, RedirectAttributes redirectAttributes) {

		Boolean deleteProduct = productService.deleteProduct(id);

		if (deleteProduct) {
			redirectAttributes.addFlashAttribute("successMsg", "product deleted");
		} else {
			redirectAttributes.addFlashAttribute("errorMsg", "something error on server");

		}

		return "redirect:/admin/products";
	}

	@GetMapping("/editProduct/{id}")
	public String editProduct(@PathVariable int id, Model m, RedirectAttributes redirectAttributes) {

		m.addAttribute("product", productService.getProductById(id));
		m.addAttribute("categories", categoryService.getAllCategory());

		return "admin/edit_product";
	}

	@PostMapping("/updateProduct")
	public String editProduct(@ModelAttribute Product product, Model m, RedirectAttributes redirectAttributes,
			@RequestParam("file") MultipartFile imageName) {

		if (product.getDiscount() < 0 || product.getDiscount() > 100) {
			redirectAttributes.addFlashAttribute("errorMsg", "Invalid Discount");
		} else {
			Product updatedProduct = productService.updateProduct(product, imageName);

			if (!ObjectUtils.isEmpty(updatedProduct)) {
				redirectAttributes.addFlashAttribute("successMsg", "Product updated Successfully");
			} else {
				redirectAttributes.addFlashAttribute("errorMsg", "Something on server");
			}
		}
		return "redirect:/admin/editProduct/" + product.getId();
	}

	@GetMapping("/users") // search Users
	public String getAllUsers(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
			@RequestParam(name = "type", defaultValue = "1") Integer type, @RequestParam(defaultValue = "") String ch,
			RedirectAttributes redirectAttributes) {

		Page<UserDetails> page = null;

		if (type == 1) {

			if (ch != null && !ch.isEmpty()) {
				page = userService.searchUsers(ch, pageNo, pageSize);
				m.addAttribute("search", ch); // Add search term to model
			} else {
				page = userService.getAllUsersWithPagination(pageNo, pageSize, "ROLE_USER");
			}

		} else {

			if (ch != null && !ch.isEmpty()) {
				page = userService.searchUsers(ch, pageNo, pageSize);
				m.addAttribute("search", ch); // Add search term to model
			} else {
				page = userService.getAllUsersWithPagination(pageNo, pageSize, "ROLE_ADMIN");
			}

		}

		m.addAttribute("userType", type);

		m.addAttribute("users", page.getContent());
		m.addAttribute("search", false);

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "/admin/users";
	}

	@GetMapping("/updateStatus")
	public String updateUserAccountStatus(@RequestParam Boolean status, @RequestParam int id,
			@RequestParam Integer type, RedirectAttributes redirectAttributes) {

		Boolean f = userService.updateAccountStatus(id, status);

		if (f) {

			redirectAttributes.addFlashAttribute("successMsg", "Account status updated");

		} else {
			redirectAttributes.addFlashAttribute("errorMsg", "Something wring on server");

		}

		return "redirect:/admin/users?type=" + type;
	}

	@GetMapping("/orders")
	public String getAllOrders(Model m, @RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {

//		List<ProductOrder> allOrders = orderService.getAllOrders();
//		m.addAttribute("orders", allOrders);
//		m.addAttribute("search", false); // default false for search order id

		Page<ProductOrder> page = orderService.getAllOrdersWithPagination(pageNo, pageSize);
		m.addAttribute("orders", page.getContent());
		m.addAttribute("search", false); // default false for search order id

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "/admin/orders";
	}

	@PostMapping("/update-order-status")
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

		return "redirect:/admin/orders";

	}

	@GetMapping("/search-order") // search orders
	public String searchProduct(@RequestParam String orderId, Model m, HttpSession session,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "2") Integer pageSize) {

		if (orderId != null && orderId.length() > 0) {
			ProductOrder order = orderService.getOrdersByOrderId(orderId.trim());
			if (ObjectUtils.isEmpty(order)) {
				session.setAttribute("errorMsg", "Order not found for the given ID.");
				m.addAttribute("orderDetails", null);
			} else {
				m.addAttribute("orderDetails", order);
			}
			m.addAttribute("search", true);

		} else {
//			List<ProductOrder> allOrders = orderService.getAllOrders();
//			m.addAttribute("orders", allOrders);
//			m.addAttribute("search", false);

			Page<ProductOrder> page = orderService.getAllOrdersWithPagination(pageNo, pageSize);
			m.addAttribute("orders", page.getContent());
			m.addAttribute("search", false);

			m.addAttribute("pageNo", page.getNumber());
			m.addAttribute("pageSize", pageSize);
			m.addAttribute("totalElements", page.getTotalElements());
			m.addAttribute("totalPages", page.getTotalPages());
			m.addAttribute("isFirst", page.isFirst());
			m.addAttribute("isLast", page.isLast());

		}

		return "/admin/orders";
	}

	@GetMapping("/addAdmin")
	public String adminAdd() {

		return "/admin/add_admin";
	}

	@PostMapping("/save-admin")
	private String saveUser(UserDetails user, @RequestParam("img") MultipartFile file,
			RedirectAttributes redirectAttributes) throws IOException {

		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();

		user.setProfileImage(imageName);
		UserDetails saveUser = userService.saveAdminDetails(user);

		if (!ObjectUtils.isEmpty(saveUser)) {

			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ file.getOriginalFilename());

				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			redirectAttributes.addFlashAttribute("successMsg", "Admin Details Saved Successfully");

		} else {
			redirectAttributes.addFlashAttribute("errorMsg", "Something wrong on server");

		}

		return "redirect:/admin/addAdmin";
	}

	@GetMapping("/profile")
	public String profile(Model model, Principal principal) {

		if (principal != null) {
			String username = principal.getName(); // Assuming you're using Spring Security
			UserDetails user = userService.getUserByEmail((username)); // Fetch user details
			model.addAttribute("user", user);
		} else {
			model.addAttribute("user", null); // For non-authenticated cases
		}

		return "/admin/profile";
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
		return "redirect:/admin/profile";
	}

	@PostMapping("/change-password")
	public String changePassword(@RequestParam String newPassword, @RequestParam String currentPassword, Principal p,
			RedirectAttributes redirectAttributes) {

		UserDetails loggedInUserDetails = commonUtil.getLoggedInUserDetails(p);

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

		return "redirect:/admin/profile";

	}

}
