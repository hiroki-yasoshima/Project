package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.config.PathConfig;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class LogoutController {

	@GetMapping(PathConfig.LOGOUT_URL)
	public String logout(HttpServletRequest request, RedirectAttributes redirectAttributes) {
		// セッションを無効にする
		HttpSession session = request.getSession(false);
		if (session != null) {
			// ユーザー情報の破棄
			session.removeAttribute("loggedInUser");
		}
		return PathConfig.LOGIN_REDIRECT_URL;
	}

}
