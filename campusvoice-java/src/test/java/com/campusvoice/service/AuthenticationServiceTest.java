package com.campusvoice.service;

import com.campusvoice.dao.UserDAO;
import com.campusvoice.model.User;
import com.campusvoice.security.PasswordHasher;
import com.campusvoice.security.SessionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AuthenticationServiceTest {
    private AuthenticationService authService;
    private MockUserDAO mockUserDAO;

    @BeforeEach
    public void setup() {
        mockUserDAO = new MockUserDAO();
        authService = new AuthenticationService(mockUserDAO);
        SessionManager.getInstance().logout();
    }

    @Test
    public void testSuccessfulLogin() {
        User testUser = new User("1", "STU202501", "John Doe", "john@student.edu", "9876543210", 1);
        testUser.setPasswordHash(PasswordHasher.hashPassword("Password123!"));
        testUser.setActive(true);

        mockUserDAO.addUser(testUser);

        boolean result = authService.login("STU202501", "Password123!");
        assertTrue(result);
        assertTrue(SessionManager.getInstance().isLoggedIn());
        assertEquals("John Doe", SessionManager.getInstance().getCurrentUser().getFullName());
    }

    @Test
    public void testInvalidPasswordLogin() {
        User testUser = new User("1", "STU202501", "John Doe", "john@student.edu", "9876543210", 1);
        testUser.setPasswordHash(PasswordHasher.hashPassword("Password123!"));
        testUser.setActive(true);

        mockUserDAO.addUser(testUser);

        boolean result = authService.login("STU202501", "WrongPass");
        assertFalse(result);
        assertFalse(SessionManager.getInstance().isLoggedIn());
    }

    // Simple in-memory mock UserDAO for fast test execution
    private static class MockUserDAO implements UserDAO {
        private java.util.Map<String, User> userMap = new java.util.HashMap<>();

        public void addUser(User u) {
            userMap.put(u.getRegNo(), u);
        }

        @Override
        public boolean createUser(User user) {
            userMap.put(user.getRegNo(), user);
            return true;
        }

        @Override
        public Optional<User> findByRegNo(String regNo) {
            return Optional.ofNullable(userMap.get(regNo));
        }

        @Override
        public Optional<User> findById(String id) { return Optional.empty(); }
        @Override
        public Optional<User> findByEmail(String email) { return Optional.empty(); }
        @Override
        public java.util.List<User> findAll() { return new java.util.ArrayList<>(userMap.values()); }
        @Override
        public java.util.List<User> findByRole(int roleId) { return new java.util.ArrayList<>(); }
        @Override
        public boolean updateUser(User user) { return true; }
        @Override
        public boolean updatePassword(String userId, String newPasswordHash) { return true; }
        @Override
        public boolean deactivateUser(String userId) { return true; }
    }
}
