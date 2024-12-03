package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.config.PathConfig;
import com.example.demo.entity.User;
import com.example.demo.form.LoginForm;
import com.example.demo.service.LoginService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class LoginController {

	@Autowired
	private LoginService loginService;

	@Autowired
	private MessageSource messageSource;

	@ModelAttribute
	public void addCssPath(Model model) {
		model.addAttribute("cssPaths", "/css/login.css" );
	}

	@GetMapping(PathConfig.LOGIN_URL)
	public String showLoginPage(@RequestParam(value = "timeout", required = false) String timeout, Model model) {
		// タイムアウトメッセージを表示
		if (timeout != null) {
			model.addAttribute("timeoutMessage",
					messageSource.getMessage("session.timeout", null, null));
		}
		if (model.containsAttribute("accessMessage")) {
			model.addAttribute("accessMessage", model.getAttribute("accessMessage"));
		}
		// 空のログインフォームを生成
		model.addAttribute("loginForm", new LoginForm());

		return PathConfig.LOGIN_PAGE;
	}

	@PostMapping(PathConfig.LOGIN_URL)
	public String login(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
			BindingResult bindingResult, Model model, HttpSession session) {

		// バリデーションエラーがある場合
		if (bindingResult.hasErrors()) {
			// 最初のエラーメッセージを取得
			String firstErrorMessage = bindingResult.getAllErrors().get(0).getDefaultMessage();
			model.addAttribute("errorMessage", firstErrorMessage);

			return PathConfig.LOGIN_PAGE;
		}

		// LoginServiceを使用してユーザーを取得
		User user = loginService.login(loginForm.getEmail(), loginForm.getPassword());

		// ユーザーが存在するか確認
		if (user != null) {
			// セッションにユーザー情報を保存
			session.setAttribute("loggedInUser", user);
			// ログイン成功時の処理
			return PathConfig.HOME_REDIRECT_URL;

		}
		// エラーメッセージをプロパティファイルから取得
		model.addAttribute("errorMessage", messageSource.getMessage("error.login.failed", null, null));
		return PathConfig.LOGIN_PAGE;

	}
}
