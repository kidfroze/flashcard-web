package com.example.ankiclone.controller;

import com.example.ankiclone.dto.AccountFormDTO;
import com.example.ankiclone.dto.LoginFormDTO;
import com.example.ankiclone.dto.SignupFormDTO;
import com.example.ankiclone.model.User;
import com.example.ankiclone.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    public static final String SESSION_USER_ID = "userId";
    public static final String SESSION_USERNAME = "username";
    public static final String SESSION_ROLE = "role";

    private final AuthService authService;

    @GetMapping("/login")
    public String loginForm(Model model) {
        model.addAttribute("loginForm", new LoginFormDTO());
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginFormDTO form,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/login";
        }
        try {
            User user = authService.authenticate(form.getUsername(), form.getPassword());
            session.setAttribute(SESSION_USER_ID, user.getUserId());
            session.setAttribute(SESSION_USERNAME, user.getUsername());
            // Save role as string to simplify template comparisons
            session.setAttribute(SESSION_ROLE, user.getRole() != null ? user.getRole().name() : null);
            redirectAttributes.addFlashAttribute("success", "Đăng nhập thành công!");
            return "redirect:/";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/login";
        }
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("signupForm", new SignupFormDTO());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("signupForm") SignupFormDTO form,
            BindingResult bindingResult,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }
        try {
            User user = authService.signup(form);
            session.setAttribute(SESSION_USER_ID, user.getUserId());
            session.setAttribute(SESSION_USERNAME, user.getUsername());
            redirectAttributes.addFlashAttribute("success", "Tạo tài khoản thành công!");
            return "redirect:/";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/signup";
        }
    }

    @GetMapping("/account")
    public String accountPage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để truy cập trang tài khoản.");
            return "redirect:/login";
        }
        User user = authService.getUserById(userId);
        model.addAttribute("user", user);
        model.addAttribute("accountForm", new AccountFormDTO(user.getEmail(), "", "", ""));
        return "auth/account";
    }

    @PostMapping("/account/update-email")
    public String updateEmail(@ModelAttribute("accountForm") AccountFormDTO accountForm,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để thực hiện hành động này.");
            return "redirect:/login";
        }
        try {
            authService.updateEmail(userId, accountForm.getCurrentPassword(), accountForm.getEmail());
            redirectAttributes.addFlashAttribute("success", "Email đã được cập nhật.");
            return "redirect:/account";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/account";
        }
    }

    @PostMapping("/account/change-password")
    public String changePassword(@ModelAttribute("accountForm") AccountFormDTO accountForm,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để thực hiện hành động này.");
            return "redirect:/login";
        }
        try {
            authService.updatePassword(userId, accountForm.getCurrentPassword(), accountForm.getNewPassword(),
                    accountForm.getConfirmNewPassword());
            redirectAttributes.addFlashAttribute("success", "Mật khẩu đã được cập nhật.");
            return "redirect:/account";
        } catch (RuntimeException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/account";
        }
    }

    @PostMapping("/account/delete")
    public String deleteAccount(HttpSession session, RedirectAttributes redirectAttributes) {
        Integer userId = (Integer) session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Bạn cần đăng nhập để thực hiện hành động này.");
            return "redirect:/login";
        }
        authService.deleteAccount(userId);
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Tài khoản đã được xóa.");
        return "redirect:/signup";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "Đã đăng xuất.");
        return "redirect:/login";
    }
}
