package com.campusvoice.dao;

import com.campusvoice.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    boolean createUser(User user);
    Optional<User> findByRegNo(String regNo);
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    List<User> findByRole(int roleId);
    boolean updateUser(User user);
    boolean updatePassword(String userId, String newPasswordHash);
    boolean deactivateUser(String userId);
}
