package com.ecom.Repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecom.Model.Product;
import com.ecom.Model.UserDetails;

@Repository
public interface UserRepo extends JpaRepository<UserDetails, Integer> {

	UserDetails findByEmail(String username);

	List<UserDetails> findByRole(String role);

	UserDetails findByResetToken(String token);
	
    Page<UserDetails> findByRole(String role, Pageable pageable);

	Boolean existsByEmail(String email);

    Page<UserDetails> findByNameContainingIgnoreCase(String ch, Pageable pageable);


}
