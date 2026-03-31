package com.example.ankiclone.service;

import com.example.ankiclone.dto.SignupFormDTO;
import com.example.ankiclone.model.User;
import com.example.ankiclone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User authenticate(String username, String rawPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Sai username hoặc mật khẩu"));
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new RuntimeException("Sai username hoặc mật khẩu");
        }
        return user;
    }

    @Transactional
    public User signup(SignupFormDTO form) {
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu nhập lại không khớp");
        }
        if (userRepository.findByUsername(form.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.findByEmail(form.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại");
        }

        User user = User.builder()
                .username(form.getUsername())
                .email(form.getEmail())
                .passwordHash(passwordEncoder.encode(form.getPassword()))
                .role(User.Role.user) // default
                .build();
        return userRepository.save(user);
    }

    public User getUserById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Người dùng không tồn tại."));
    }

    @Transactional
    public User updateEmail(Integer userId, String currentPassword, String email) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new RuntimeException("Mật khẩu hiện tại không được để trống.");
        }
        if (email == null || email.isBlank()) {
            throw new RuntimeException("Email không được để trống.");
        }
        User user = getUserById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng.");
        }
        if (userRepository.findByEmail(email).filter(u -> !u.getUserId().equals(userId)).isPresent()) {
            throw new RuntimeException("Email đã được sử dụng bởi tài khoản khác.");
        }
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Transactional
    public void updatePassword(Integer userId, String currentPassword, String newPassword, String confirmPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new RuntimeException("Mật khẩu hiện tại không được để trống.");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new RuntimeException("Mật khẩu mới không được để trống.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new RuntimeException("Mật khẩu mới và nhập lại không khớp.");
        }
        User user = getUserById(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng.");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(Integer userId) {
        userRepository.deleteById(userId);
    }
}
