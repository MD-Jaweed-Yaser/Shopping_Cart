package com.ecom.Service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ecom.Model.Product;
import com.ecom.Repository.productRepo;
import com.ecom.Service.ProductService;

@Service
public class ProductServiceImpl implements ProductService {

	@Autowired
	private productRepo repo;

	@Override
	public Product saveProduct(Product product) {

		return repo.save(product);
	}

	@Override
	public List<Product> getAllProducts() {

		return repo.findAll();
	}

	@Override
	public Page<Product> getAllProductsWithPagination(Integer pageNo, Integer pageSize) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);

		return repo.findAll(pageable);
	}

	@Override
	public Boolean deleteProduct(int id) {

		Product product = repo.findById(id).orElse(null);

		if (!ObjectUtils.isEmpty(product)) {
			repo.delete(product);
			return true;
		}

		return false;
	}

	@Override
	public Product getProductById(int id) {

		Product product = repo.findById(id).orElse(null);

		return product;
	}

	@Override
	public Product updateProduct(Product product, MultipartFile file) {

		Product dbProduct = getProductById(product.getId());

		String imageName = file.isEmpty() ? dbProduct.getImageName() : file.getOriginalFilename();

		dbProduct.setTitle(product.getTitle());
		dbProduct.setDescription(product.getDescription());
		dbProduct.setCategory(product.getCategory());
		dbProduct.setPrice(product.getPrice());
		dbProduct.setStock(product.getStock());
		dbProduct.setImageName(imageName);

		dbProduct.setDiscount(product.getDiscount());
		dbProduct.setDiscountPrice(product.getDiscount());

		dbProduct.setIsActive(product.getIsActive());

		// 100*(5/100)= 5 . 100-5=95 for discount

		Double discount = product.getPrice() * (product.getDiscount() / 100.0);
		Double discountPrice = product.getPrice() - discount;
		dbProduct.setDiscountPrice(discountPrice);

		Product updateProduct = repo.save(dbProduct);

		try {
			if (!ObjectUtils.isEmpty(updateProduct)) {

				if (!file.isEmpty()) {
					File saveFile = new ClassPathResource("static/img").getFile();

					Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "product_img" + File.separator
							+ file.getOriginalFilename());

					System.out.println(path);

					Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

				}
				return updateProduct;

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public List<Product> getAllActiveProducts(String category) {

		List<Product> products = null;
		if (ObjectUtils.isEmpty(category)) {
			products = repo.findByIsActiveTrue();
		} else {
			products = repo.findByCategory(category);
		}

		return products;
	}

	@Override
	public List<Product> searchProduct(String ch) {

		return repo.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch);

	}

	@Override
	public Page<Product> searchProductWithPagination(Integer pageNo, Integer pageSize, String ch) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);

		return repo.findByTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);

	}

	@Override
	public Page<Product> getAllActiveProductsWithPagination(Integer pageNo, Integer pageSize, String category) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);

		Page<Product> pageProduct = null;

		if (ObjectUtils.isEmpty(category)) {
			pageProduct = repo.findByIsActiveTrue(pageable);
		} else {
			pageProduct = repo.findByCategory(pageable, category);
		}

		return pageProduct;
	}

	@Override
	public Page<Product> searchActiveProductWithPagination(Integer pageNo, Integer pageSize, String category,String ch) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);

		Page<Product> pageProduct = null;

		pageProduct = repo.findByisActiveTrueAndTitleContainingIgnoreCaseOrCategoryContainingIgnoreCase(ch, ch, pageable);

//		if (ObjectUtils.isEmpty(category)) {
//			pageProduct = repo.findByIsActiveTrue(pageable);
//		} else {
//			pageProduct = repo.findByCategory(pageable, category);
//		}

		return pageProduct;
	}

}
