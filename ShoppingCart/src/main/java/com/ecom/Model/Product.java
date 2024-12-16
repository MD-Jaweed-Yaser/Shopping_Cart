package com.ecom.Model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
public class Product {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(length = 500)
	private String title;
	
	@Column(length = 5000)
	private String description;
	
	private String category; 
	
	private double price;
	
	private int stock;
	
	private String imageName;
	
	private int discount;
	
	private double discountPrice;
	
	private Boolean isActive;
	
}
