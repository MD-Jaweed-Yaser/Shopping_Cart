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
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.Model.Category;
import com.ecom.Model.Product;
import com.ecom.Model.UserDetails;
import com.ecom.Service.CartService;
import com.ecom.Service.CategoryService;
import com.ecom.Service.ProductService;
import com.ecom.Service.UserService;
import com.ecom.Util.CommonUtil;

import io.micrometer.common.util.StringUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

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
	private BCryptPasswordEncoder passwordEncoder;

	@ModelAttribute
	private void getUserDetails(Principal p, Model m) {

		if (p != null) {

			String email = p.getName();
			UserDetails userDetails = userService.getUserByEmail(email);
			m.addAttribute("user", userDetails);
			Integer countCart = cartService.getCountCart(userDetails.getId());
			m.addAttribute("countCart", countCart);
		}

		List<Category> allActiveCategory = categoryService.getAllActiveCategory();

		m.addAttribute("category", allActiveCategory);
	}

	@GetMapping("/")
	public String home(Model m) {

		List<Category> allActiveCategory = categoryService.getAllActiveCategory().stream()
				.sorted((c1, c2) -> c2.getId().compareTo(c1.getId())).limit(6).toList();

		List<Product> allActiveProducts = productService.getAllActiveProducts("").stream()
				.sorted((p1, p2) -> p2.getId().compareTo(p1.getId())).limit(8).toList();

		m.addAttribute("category", allActiveCategory);
		m.addAttribute("products", allActiveProducts);

		return "index";
	}

	@GetMapping("/signin")
	public String login() {
		return "login";
	}

	@GetMapping("/register")
	public String register() {
		return "register";
	}

	@GetMapping("/products") // PAGINATION and search
	public String products(Model m, @RequestParam(value = "category", defaultValue = "") String category,
			@RequestParam(name = "pageNo", defaultValue = "0") Integer pageNo,
			@RequestParam(name = "pageSize", defaultValue = "9") Integer pageSize,
			@RequestParam(defaultValue = "") String ch) {

		// System.out.println("category=" + category);

		List<Category> categories = categoryService.getAllActiveCategory();
		m.addAttribute("paramValue", category);
		m.addAttribute("categories", categories);

//		List<Product> products = productService.getAllActiveProducts(category);
//		m.addAttribute("products", products);

		Page<Product> page = null;
		if (StringUtils.isEmpty(ch)) {

			page = productService.getAllActiveProductsWithPagination(pageNo, pageSize, category);

		} else {
			page = productService.searchActiveProductWithPagination(pageNo, pageSize, category, ch);
		}

		List<Product> products = page.getContent();
		m.addAttribute("products", products);
		m.addAttribute("productsSize", products.size());

		m.addAttribute("pageNo", page.getNumber());
		m.addAttribute("pageSize", pageSize);
		m.addAttribute("totalElements", page.getTotalElements());
		m.addAttribute("totalPages", page.getTotalPages());
		m.addAttribute("isFirst", page.isFirst());
		m.addAttribute("isLast", page.isLast());

		return "product";
	}

	@GetMapping("/product/{id}")
	public String product(@PathVariable int id, Model m) {

		Product productById = productService.getProductById(id);

		m.addAttribute("product", productById);

		return "view_product";
	}

	@PostMapping("/saveUser")
	private String saveUser(UserDetails user, @RequestParam("img") MultipartFile file,
			RedirectAttributes redirectAttributes) throws IOException {

		Boolean existsEmail = userService.existsEmail(user.getEmail());

		if (existsEmail) {
			redirectAttributes.addFlashAttribute("errorMsg", "Email already exists");
		}else {

		

		String imageName = file.isEmpty() ? "default.jpg" : file.getOriginalFilename();
		user.setProfileImage(imageName);
		UserDetails saveUser = userService.saveUserDetails(user);

		if (!ObjectUtils.isEmpty(saveUser)) {

			if (!file.isEmpty()) {
				File saveFile = new ClassPathResource("static/img").getFile();

				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ file.getOriginalFilename());

				System.out.println(path);
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			}

			redirectAttributes.addFlashAttribute("successMsg", "Register Successfully");

		} else {
			redirectAttributes.addFlashAttribute("errorMsg", "Something wrong on server");

		}}

		return "redirect:/register";
	}

	// Forget Password code

	@GetMapping("/forget-password")
	public String showForgetPasswordPage() {

		return "forget_password";

	}

	@PostMapping("/forget-password")
	public String processForgetPasswordPage(@RequestParam String email, RedirectAttributes redirectAttributes,
			HttpServletRequest request) throws UnsupportedEncodingException, MessagingException {

		UserDetails userByEmail = userService.getUserByEmail(email);

		if (ObjectUtils.isEmpty(userByEmail)) {

			redirectAttributes.addFlashAttribute("errorMsg", "Invalid email");

		} else {

			String resetToken = UUID.randomUUID().toString();
			userService.updateUserResetToken(email, resetToken);

			// Generate url http://localhost:8080/reset-password?token=ncjwnecininci3ej3e

			String url = CommonUtil.generateUrl(request) + "/reset-password?token=" + resetToken;

			Boolean sendMail = commonUtil.sendMail(url, email);

			if (sendMail) {

				redirectAttributes.addFlashAttribute("successMsg", "Please Check your mail for password reset link");
			} else {
				redirectAttributes.addFlashAttribute("errorMsg", "Something wrong on server,mail not sent");

			}
		}

		return "redirect:/forget-password";

	}

	@GetMapping("/reset-password")
	public String showResetPassword(@RequestParam String token, HttpSession session, Model m) {

		UserDetails userByToken = userService.getUserByToken(token);

		if (userByToken == null) {
			m.addAttribute("msg", "Your link is invalid or expired !!");
			return "message";
		}
		m.addAttribute("token", token);
		return "reset_password";
	}

	@PostMapping("/reset-password")
	public String resetPassword(@RequestParam String token, @RequestParam String password,
			@RequestParam String confirmPassword, HttpSession session, Model m) {

		UserDetails userByToken = userService.getUserByToken(token);

		if (userByToken == null) {

			m.addAttribute("errorMsg", "Your link is invalid or expired !!");
			System.out.println("pasword notttttttttttttttttt updateddd");

			return "message";
		}

		if (!password.equals(confirmPassword)) {
			m.addAttribute("errorMsg", "Passwords do not match!");
			return "reset_password";
		}

		System.out.println("Token: " + token);
		System.out.println("Password: " + password);

		userByToken.setPassword(passwordEncoder.encode(password));
		userByToken.setResetToken(null);
		userService.updateUser(userByToken);
		// session.setAttribute("succMsg", "Password change successfully");
		m.addAttribute("msg", "Password change successfully");
		System.out.println("pasword updatedddddddddddddddddddd");
		return "message";

	}

	@GetMapping("/search")
	public String searchProduct(@RequestParam String ch, Model m) {

		List<Product> searchProduct = productService.searchProduct(ch);

		m.addAttribute("products", searchProduct);

		List<Category> categories = categoryService.getAllActiveCategory();

		m.addAttribute("categories", categories);

		return "product";
	}
}
