package com.example.parking.controller;

import com.example.parking.dto.UserRegisterDto;
import com.example.parking.exception.DuplicateResourceException;
import com.example.parking.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller handling user authentication routes: Login & Registration.
 */
@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Display Custom Login Page.
     */
    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                @RequestParam(value = "registered", required = false) String registered,
                                Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email address or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("infoMessage", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Registration successful! Please sign in with your credentials.");
        }
        return "login";
    }

    /**
     * Display Registration Page.
     */
    @GetMapping("/register")
    public String showRegistrationPage(Model model) {
        model.addAttribute("userDto", new UserRegisterDto());
        return "register";
    }

    /**
     * Process User Registration Form Submission with Validation.
     */
    @PostMapping("/register")
    public String handleUserRegistration(@Valid @ModelAttribute("userDto") UserRegisterDto registerDto,
                                         BindingResult bindingResult,
                                         Model model) {
        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerUser(registerDto);
            return "redirect:/login?registered=true";
        } catch (DuplicateResourceException | IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
            return "register";
        }
    }
}
