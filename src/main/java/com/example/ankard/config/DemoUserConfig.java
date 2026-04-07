package com.example.ankard.config;

import com.example.ankard.controller.AuthController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

/**
 * Giả lập user đang đăng nhập (bỏ qua phần Auth).
 * Để dùng thật sau này, thay thế bằng Spring Security + Session.
 */
@Component
public class DemoUserConfig {

    private final ObjectProvider<HttpServletRequest> requestProvider;

    public DemoUserConfig(ObjectProvider<HttpServletRequest> requestProvider) {
        this.requestProvider = requestProvider;
    }

    // Nếu đã login thì dùng userId trong session; fallback demo = 1
    public Integer getCurrentUserId() {
        HttpServletRequest request = requestProvider.getIfAvailable();
        if (request != null) {
            Object userId = request.getSession(false) != null
                    ? request.getSession(false).getAttribute(AuthController.SESSION_USER_ID)
                    : null;
            if (userId instanceof Integer id) return id;
        }
        return 1;
    }
}
