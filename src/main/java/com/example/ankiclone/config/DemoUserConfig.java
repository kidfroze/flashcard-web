package com.example.ankiclone.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Giả lập user đang đăng nhập (bỏ qua phần Auth).
 * Để dùng thật sau này, thay thế bằng Spring Security + Session.
 */
@Component
public class DemoUserConfig {

    /*
     * @Value("${app.demo.user-id:1}")
     * private Integer demoUserId;
     * 
     * public Integer getCurrentUserId() {
     * return demoUserId;
     * }
     */
    // Hardcode user_id = 1, không cần đọc từ properties
    public Integer getCurrentUserId() {
        return 1;
    }
}
