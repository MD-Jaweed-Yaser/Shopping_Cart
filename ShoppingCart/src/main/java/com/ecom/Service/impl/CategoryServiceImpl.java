package com.ecom.Service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.Model.Category;
import com.ecom.Repository.CategoryRepo;
import com.ecom.Service.CategoryService;

@Service
public class CategoryServiceImpl implements CategoryService {

	@Autowired
	private CategoryRepo repo;

	@Override
	public Category saveCategory(Category category) {

		return repo.save(category);

	}

	@Override
	public List<Category> getAllCategory() {

		return repo.findAll();
	}

	@Override
	public Boolean existCategory(String name) {

		return repo.existsByName(name);
	}

	@Override
	public boolean deleteCategory(int id) {

		Category category = repo.findById(id).orElse(null);

		if (!ObjectUtils.isEmpty(category)) {

			repo.delete(category);
			return true;
		}

		return false;
	}

	@Override
	public Category getCategoryById(int id) {

		Category category = repo.findById(id).orElse(null);

		return category;
	}

	@Override
	public List<Category> getAllActiveCategory() {

		List<Category> categories = repo.findByIsActiveTrue();

		return categories;
	}

	@Override
	public Page<Category> getAllCategoryWithPagination(Integer pageNo, Integer pageSize) {

		
		
		Pageable pageable = PageRequest.of(pageNo, pageSize);

		return repo.findAll(pageable);

	}

}
