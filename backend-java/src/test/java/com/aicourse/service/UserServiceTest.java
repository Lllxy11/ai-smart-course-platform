package com.aicourse.service;

import com.aicourse.dto.*;
import com.aicourse.entity.User;
import com.aicourse.entity.UserRole;
import com.aicourse.exception.BusinessException;
import com.aicourse.exception.ResourceNotFoundException;
import com.aicourse.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getUsers_adminRole() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(UserRole.ADMIN);

        User user1 = new User();
        user1.setId(2L);
        user1.setRole(UserRole.STUDENT);
        User user2 = new User();
        user2.setId(3L);
        user2.setRole(UserRole.TEACHER);
        List<User> users = Arrays.asList(user1, user2);

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        PagedResponse<UserResponse> result = userService.getUsers(1, 10, null, null, 1L, UserRole.ADMIN);

        assertEquals(2, result.getContent().size());
    }

    @Test
    void getUserStatistics_adminRole() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(UserRole.ADMIN);

        when(userRepository.count()).thenReturn(10L);
        when(userRepository.countByIsActive(true)).thenReturn(5L);
        when(userRepository.countByCreatedAtAfter(any(LocalDateTime.class))).thenReturn(2L);
        when(userRepository.countByRole(UserRole.STUDENT)).thenReturn(6L);
        when(userRepository.countByRole(UserRole.TEACHER)).thenReturn(3L);

        Map<String, Object> result = userService.getUserStatistics(1L, UserRole.ADMIN);

        assertNotNull(result);
    }

    @Test
    void getUserById_adminRole() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(UserRole.ADMIN);

        User user = new User();
        user.setId(2L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        UserResponse result = userService.getUserById(2L, 1L, UserRole.ADMIN);

        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
    }

    @Test
    void createUser_adminRole() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(UserRole.ADMIN);

        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("newuser");
        request.setEmail("newuser@example.com");
        request.setPassword("password");
        request.setRole(UserRole.STUDENT);

        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("newuser@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        UserResponse result = userService.createUser(request, 1L, UserRole.ADMIN);

        assertEquals("newuser", result.getUsername());
        assertEquals("newuser@example.com", result.getEmail());
    }

    @Test
    void updateUser_adminRole() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(UserRole.ADMIN);

        User user = new User();
        user.setId(2L);
        user.setUsername("olduser");
        user.setEmail("old@example.com");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("newuser");
        request.setEmail("new@example.com");

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(userRepository.findByUsernameAndIdNot("newuser", 2L)).thenReturn(Optional.empty());
        when(userRepository.findByEmailAndIdNot("new@example.com", 2L)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        UserResponse result = userService.updateUser(2L, request, 1L, UserRole.ADMIN);

        assertEquals("newuser", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    void deleteUser_adminRole() {
        User admin = new User();
        admin.setId(1L);
        admin.setRole(UserRole.ADMIN);

        User user = new User();
        user.setId(2L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertDoesNotThrow(() -> userService.deleteUser(2L, 1L, UserRole.ADMIN));
        verify(userRepository, times(1)).delete(user);
}
}

