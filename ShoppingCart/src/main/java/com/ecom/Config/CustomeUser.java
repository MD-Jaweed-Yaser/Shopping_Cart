package com.ecom.Config;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.ecom.Model.UserDetails;

public class CustomeUser implements org.springframework.security.core.userdetails.UserDetails {

	@Autowired
	private UserDetails user;

	
	
	public CustomeUser(UserDetails user) {
		super();
		this.user = user;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		
		SimpleGrantedAuthority authority = new SimpleGrantedAuthority(user.getRole());
		
		return Arrays.asList(authority);
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return user.getPassword();
	}

	@Override
	public String getUsername() {
		// TODO Auto-generated method stub
		return user.getEmail();
	}

	@Override
	public boolean isAccountNonExpired() {
		// TODO Auto-generated method stub
		return org.springframework.security.core.userdetails.UserDetails.super.isAccountNonExpired();
	}

	@Override
	public boolean isAccountNonLocked() {
	
		return user.getAccountNonLocked();
		
		
	}
	@Override
	public boolean isCredentialsNonExpired() {
		// TODO Auto-generated method stub
		return org.springframework.security.core.userdetails.UserDetails.super.isCredentialsNonExpired();
	}

	@Override
	public boolean isEnabled() {
		
		return user.getIsEnable();
	}
	
	
	
}
