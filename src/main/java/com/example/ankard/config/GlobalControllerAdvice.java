package com.example.ankard.config;

import com.example.ankard.controller.AuthController;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute
    public void addUserInfoToModel(Model model, HttpSession session) {
        if (session != null) {
            Object username = session.getAttribute(AuthController.SESSION_USERNAME);
            Object role = session.getAttribute(AuthController.SESSION_ROLE);
            model.addAttribute("username", username);
            model.addAttribute("role", role);
        } else {
            model.addAttribute("username", null);
            model.addAttribute("role", null);
        }
    }
}
