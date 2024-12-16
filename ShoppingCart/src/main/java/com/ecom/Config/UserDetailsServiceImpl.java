package com.ecom.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.ecom.Repository.UserRepo;

public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	private UserRepo repo;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		com.ecom.Model.UserDetails user = repo.findByEmail(username);

		if (user == null) {

			throw new UsernameNotFoundException("user not found");
		}

		return new CustomeUser(user);
	}

}
