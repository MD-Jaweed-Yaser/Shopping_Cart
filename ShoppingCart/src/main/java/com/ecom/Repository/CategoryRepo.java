package com.ecom.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.Model.Category;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Integer> {

	public boolean existsByName(String name);

	public List<Category> findByIsActiveTrue();

	
}
