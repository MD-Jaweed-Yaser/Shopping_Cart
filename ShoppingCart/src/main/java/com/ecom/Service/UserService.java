package com.ecom.Service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.Model.Product;
import com.ecom.Model.UserDetails;

public interface UserService {

	public UserDetails saveUserDetails(UserDetails user);

	public UserDetails saveAdminDetails(UserDetails user);

	public UserDetails getUserByEmail(String email);

	public List<UserDetails> getAllUsers(String role);

	public Boolean updateAccountStatus(int id, Boolean status);

	public void IncreaseFailedAttempt(UserDetails user);

	public void UserAccountLock(UserDetails user);

	public boolean unlockAccountTimeExpired(UserDetails user);

	public void resetAttempt(int userId);

	public void updateUserResetToken(String email, String resetToken);

	public UserDetails getUserByToken(String token);

	public UserDetails updateUser(UserDetails user);

	public UserDetails updateUserProfile(UserDetails user, MultipartFile img);

	public Page<UserDetails> getAllUsersWithPagination(Integer pageNo, Integer pageSize, String role);

	public	Page<UserDetails> searchUsers(String ch, int pageNo, int pageSize);

	public Boolean existsEmail(String email);

}
