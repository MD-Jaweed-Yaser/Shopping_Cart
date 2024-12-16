package com.ecom.Service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.Model.Product;
import com.ecom.Model.UserDetails;
import com.ecom.Repository.UserRepo;
import com.ecom.Service.UserService;
import com.ecom.Util.AppConstant;

@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private UserRepo repo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public UserDetails saveUserDetails(UserDetails user) {

		user.setRole("ROLE_USER");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);

		String encodepassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodepassword);
		UserDetails saveUser = repo.save(user);

		return saveUser;
	}
	
	public UserDetails saveAdminDetails(UserDetails user) {
		
		user.setRole("ROLE_ADMIN");
		user.setIsEnable(true);
		user.setAccountNonLocked(true);
		user.setFailedAttempt(0);
		
		String encodepassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodepassword);
		UserDetails saveUser = repo.save(user);
		
		return saveUser;
	}

	@Override
	public UserDetails getUserByEmail(String email) {

		return repo.findByEmail(email);

	}

	@Override
	public List<UserDetails> getAllUsers(String role) {

		return repo.findByRole(role);

	}

	@Override
	public Page<UserDetails> getAllUsersWithPagination(Integer pageNo, Integer pageSize, String role) {

		Pageable pageable = PageRequest.of(pageNo, pageSize);

		return repo.findByRole(role, pageable);
	}
	
	

	@Override
	public Page<UserDetails> searchUsers(String ch, int pageNo, int pageSize) {
	    Pageable pageable = PageRequest.of(pageNo, pageSize);
	    return repo.findByNameContainingIgnoreCase(ch, pageable);
	}




	@Override
	public Boolean updateAccountStatus(int id, Boolean status) {

		Optional<UserDetails> findByUser = repo.findById(id);

		if (findByUser.isPresent()) {

			UserDetails userDetails = findByUser.get();
			userDetails.setIsEnable(status);

			repo.save(userDetails);

			return true;

		}

		return false;
	}

	@Override
	public void IncreaseFailedAttempt(UserDetails user) {

		int attempt = user.getFailedAttempt() + 1;
		user.setFailedAttempt(attempt);
		repo.save(user);

	}

	@Override
	public void UserAccountLock(UserDetails user) {

		user.setAccountNonLocked(false);
		user.setLockTime(new Date());
		repo.save(user);

	}

	@Override
	public boolean unlockAccountTimeExpired(UserDetails user) {

		long lockTime = user.getLockTime().getTime();

		long unlockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;

		long currentTime = System.currentTimeMillis();

		if (unlockTime < currentTime) {

			user.setAccountNonLocked(true);
			user.setFailedAttempt(0);
			user.setLockTime(null);
			repo.save(user);
			return true;
		}

		return false;
	}

	@Override
	public void resetAttempt(int userId) {

	}

	@Override
	public void updateUserResetToken(String email, String resetToken) {

		UserDetails finByEmail = repo.findByEmail(email);

		finByEmail.setResetToken(resetToken);

		repo.save(finByEmail);

	}

	@Override
	public UserDetails getUserByToken(String token) {
		return repo.findByResetToken(token);

	}

	@Override
	public UserDetails updateUser(UserDetails user) {

		return repo.save(user);
	}

	@Override
	public UserDetails updateUserProfile(UserDetails user, MultipartFile img) {

		UserDetails dbUser = repo.findById(user.getId()).get();

		if (!img.isEmpty()) {

			dbUser.setProfileImage(img.getOriginalFilename());

		}

		if (!ObjectUtils.isEmpty(dbUser)) {

			dbUser.setName(user.getName());
			dbUser.setMobileNumber(user.getMobileNumber());
			dbUser.setAddress(user.getAddress());
			dbUser.setCity(user.getCity());
			dbUser.setState(user.getState());
			dbUser.setPincode(user.getPincode());
			dbUser = repo.save(dbUser);

		}

		if (!img.isEmpty()) {

			File saveFile;
			try {
				saveFile = new ClassPathResource("static/img").getFile();
				Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + "profile_img" + File.separator
						+ img.getOriginalFilename());

				System.out.println(path);
				Files.copy(img.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return dbUser;
	}

	@Override
	public Boolean existsEmail(String email) {
		
		return repo.existsByEmail(email);
	}

	
	
}
