package com.ecom.Util;

import java.io.UnsupportedEncodingException;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.ecom.Model.ProductOrder;
import com.ecom.Model.UserDetails;
import com.ecom.Service.UserService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;

@Component
public class CommonUtil {

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private UserService userService;

	public Boolean sendMail(String url, String reciepentEmail) throws UnsupportedEncodingException, MessagingException {

		MimeMessage message = mailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom("ecomshoppingcart31gmail.com", "Shopping cart");

		helper.setTo(reciepentEmail);

		String content = "<p>Hello,</p>" + "<p> You have requested to reset your password</p>"
				+ "<p>Click the link below to change your password:</p>" + "<p><a href=\"" + url
				+ "\">Change my password</a></p>";

		helper.setSubject("Password Reset");
		helper.setText(content, true);

		mailSender.send(message);

		return true;

	}

	public static String generateUrl(HttpServletRequest request) {

		// http://localhost:8080/forget-password
		String siteUrl = request.getRequestURL().toString();

		return siteUrl.replace(request.getServletPath(), "");

	}

	String msg = null;

	public Boolean sendMailProductOrder(ProductOrder order, String status)
			throws MessagingException, UnsupportedEncodingException {

		msg = "<p>Hello [[name]] ,</p>"+"<p>Thankyou Order <b>[[orderStatus]]</b>.</p>" + "<p><b>Product Details : </b></p>"
				+ "<p>Name : [[productName]] </p>" + "<p>Category : [[category]] </p>" + "<p>Quanity :[[quantity]] </p>"
				+ "<p>Price : [[price]]</p>" + "<p>Payment Type : [[paymentType]]</p>";
		
		MimeMessage message = mailSender.createMimeMessage();

		MimeMessageHelper helper = new MimeMessageHelper(message);

		helper.setFrom("ecomshoppingcart31gmail.com", "Shopping cart");

		helper.setTo(order.getOrderAddress().getEmail());

		msg = msg.replace("[[name]]", order.getOrderAddress().getFirstName());
		msg = msg.replace("[[orderStatus]]", status);
		msg = msg.replace("[[productName]]", order.getProduct().getTitle());
		msg = msg.replace("[[category]]", order.getProduct().getCategory());
		msg = msg.replace("[[quantity]]", order.getQuantity().toString());
		msg = msg.replace("[[price]]", order.getPrice().toString());
		msg = msg.replace("[[paymentType]]", order.getPaymentType());

		helper.setSubject("Product Ordered Status");
		helper.setText(msg, true);

		mailSender.send(message);

		return true;

	}

	
	public UserDetails getLoggedInUserDetails(Principal p) {

		String email = p.getName();
		UserDetails userDetails = userService.getUserByEmail(email);

		return userDetails;
	}
	
}
