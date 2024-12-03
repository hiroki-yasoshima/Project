package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.config.PathConfig;
import com.example.demo.entity.User;
import com.example.demo.form.RegisterForm;
import com.example.demo.form.SearchForm;
import com.example.demo.service.UserManagementService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserManagementController {

	@Autowired
	private UserManagementService userManagementService;

	@Autowired
	private MessageSource messageSource;

	@ModelAttribute
	public void addCssPath(Model model) {
		model.addAttribute("cssPaths", new String[] { "/css/base.css", "/css/user-management.css" });
	}

	@GetMapping(PathConfig.USER_MANAGEMENT_URL)
	public String showManagementPage(HttpServletRequest request, RedirectAttributes redirectAttributes,
			HttpSession session, Model model) {

		User user = (User) session.getAttribute("loggedInUser");
		model.addAttribute("employeeAdmin", user.getAdmin());

		// 不正アクセス時の処理
		if (user == null || user.getAdmin() != 2) {
			redirectAttributes.addFlashAttribute("accessMessage",
					messageSource.getMessage("accessMessage", null, null));
			HttpSession session1 = request.getSession(false);
			if (session1 != null) {
				// ユーザー情報の破棄
				session1.removeAttribute("loggedInUser");
			}
			return PathConfig.LOGIN_REDIRECT_URL;
		}

		// 空のユーザー登録フォームを生成
		model.addAttribute("registerForm", new RegisterForm());

		// 空のユーザー検索フォームを生成
		model.addAttribute("searchForm", new SearchForm());

		// 初期タブを「登録」タブに設定
		model.addAttribute("currentTab", "register");

		// 現在のページの情報
		model.addAttribute("currentPage", "user-management");

		return PathConfig.USER_MANAGEMENT_PAGE;
	}

	// ユーザー登録
	@PostMapping(PathConfig.USER_MANAGEMENT_URL)
	public String registerUser(@Validated @ModelAttribute("registerForm") RegisterForm form, BindingResult result,
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {
		User user = (User) session.getAttribute("loggedInUser");
		model.addAttribute("employeeAdmin", user.getAdmin());

		// 空のユーザー検索フォームを生成
		model.addAttribute("searchForm", new SearchForm());

		// 初期タブを「登録」タブに設定
		model.addAttribute("currentTab", "register");

		// 現在のページの情報
		model.addAttribute("currentPage", "user-management");
		
		// 社員番号とメールアドレスの存在チェック
		boolean isUserExists = userManagementService.isUserAlreadyExists(form.getId(), form.getMail());
		
		if (isUserExists) {
			model.addAttribute("errorMatching",
					messageSource.getMessage("error.matching", null, null));
			return PathConfig.USER_MANAGEMENT_PAGE;
		}

		// エラーがある場合、エラーメッセージをモデルに追加
		if (result.hasErrors()) {
			model.addAttribute("idSize", messageSource.getMessage("id.size", null, null));
			model.addAttribute("nameSize", messageSource.getMessage("name.size", null, null));
			model.addAttribute("errorMail", messageSource.getMessage("error.mail", null, null));
			model.addAttribute("errorReemail", messageSource.getMessage("error.reemail", null, null));
			model.addAttribute("passworSize", messageSource.getMessage("password.size", null, null));
			model.addAttribute("passwordPattern",
					messageSource.getMessage("password.pattern", null, null));

			// メールアドレスの同一チェック
			if (!form.getMail().equals(form.getReemail())) {
				model.addAttribute("emailMismatch",
						messageSource.getMessage("error.email.mismatch", null, null));
			}

			// エラーがあれば、再度フォームを表示
			return PathConfig.USER_MANAGEMENT_PAGE;
		}

		// ユーザー登録
		boolean registerResult = userManagementService.registerUser(form);

		if (registerResult) {
			// 登録成功時のメッセージ
			redirectAttributes.addFlashAttribute("successMessage",
					messageSource.getMessage("successMessage", null, null));
		} else {
			// 登録失敗の時のメッセージ
			redirectAttributes.addFlashAttribute("errorMessage",
					messageSource.getMessage("errorMessage", null, null));
		}

		return PathConfig.USER_MANAGEMENT_REDIRECT_URL;

	}

	// ユーザー検索
	@PostMapping("/user-management/search")
	public String searchUser(@Validated @ModelAttribute("searchForm") SearchForm searchForm,
			BindingResult result, @RequestParam(required = false) Integer searchId,
			@RequestParam(required = false) String searchName,
			@RequestParam(required = false) String searchMail, HttpSession session, Model model) {

		User user = (User) session.getAttribute("loggedInUser");
		model.addAttribute("employeeAdmin", user.getAdmin());

		// 検索タブに切り替え
		model.addAttribute("currentTab", "search");

		// 現在のページの情報
		model.addAttribute("currentPage", "user-management");

		// エラーがある場合、エラーメッセージをモデルに追加
		if (result.hasErrors()) {
			model.addAttribute("idSize", messageSource.getMessage("id.size", null, null));
			model.addAttribute("nameSize", messageSource.getMessage("name.size", null, null));
			model.addAttribute("errorMail", messageSource.getMessage("error.mail", null, null));
	
			// 空のユーザー登録フォームを生成
			model.addAttribute("registerForm", new RegisterForm());
			// エラーがあれば、再度フォームを表示
			return PathConfig.USER_MANAGEMENT_PAGE;
		}

		// あいまい検索をサービスを通じて実行
		List<User> userList = userManagementService.searchUsers(searchId, searchName, searchMail);

		// 検索結果をビューに追加
		model.addAttribute("userList", userList);

		// 空の登録フォームと検索フォームをビューに渡す
		model.addAttribute("registerForm", new RegisterForm());
		model.addAttribute("searchForm", new SearchForm());

		// 検索結果と削除フォームを表示
		return PathConfig.USER_MANAGEMENT_PAGE;
	}

	// ユーザー削除
	@PostMapping("/user-management/delete")
	public String deleteUser(@RequestParam("userId") Integer userId,
			RedirectAttributes redirectAttributes, Model model) {

		// 現在のページの情報
		model.addAttribute("currentPage", "user-management");

		// ユーザーIDが無効ならエラーメッセージを設定してリダイレクト
		if (userId == null) {
			redirectAttributes.addFlashAttribute("deleteErrorMessage", messageSource.getMessage("delete.error.message", null, null));
			return PathConfig.USER_MANAGEMENT_REDIRECT_URL;
		}

		// deleteUserByIdメソッドを呼び出し、結果に応じて処理
		boolean success = userManagementService.deleteUserById(userId);

		if (success) {
			redirectAttributes.addFlashAttribute("deleteMessage", messageSource.getMessage("delete.message", null, null));
		} else {
			redirectAttributes.addFlashAttribute("undeleteMessage", messageSource.getMessage("undelete.message", null, null));
		}

		// リダイレクト
		return PathConfig.USER_MANAGEMENT_REDIRECT_URL;
	}
}