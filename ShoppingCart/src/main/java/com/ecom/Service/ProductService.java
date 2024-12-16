package com.ecom.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.Model.Product;

public interface ProductService {

	public Product saveProduct(Product product);

	public List<Product> getAllProducts();

	public Boolean deleteProduct(int id);

	public Product getProductById(int id);

	public Product updateProduct(Product product, MultipartFile file);

	public List<Product> getAllActiveProducts(String category);

	public List<Product> searchProduct(String ch);

	public Page<Product> getAllActiveProductsWithPagination(Integer pageNo, Integer pageSize, String category);

	public Page<Product> searchProductWithPagination(Integer pageNo , Integer pageSize , String ch);

	public Page<Product> getAllProductsWithPagination(Integer pageNo, Integer pageSize);

	public Page<Product> searchActiveProductWithPagination(Integer pageNo, Integer pageSize, String category, String ch);

	
	
}
