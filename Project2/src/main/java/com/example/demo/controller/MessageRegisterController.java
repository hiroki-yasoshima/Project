package com.example.demo.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.config.PathConfig;
import com.example.demo.entity.User;
import com.example.demo.form.MessageForm;
import com.example.demo.form.SearchForm;
import com.example.demo.service.MessageRegisterService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class MessageRegisterController {

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private MessageRegisterService messageRegisterService;
	
	@ModelAttribute
	public void addCssPath(Model model) {
		model.addAttribute("cssPaths", new String[] { "/css/base.css", "/css/message-register.css" });
	}

	@GetMapping(PathConfig.MESSAGE_URL)
	public String showMessageRegisterPage(HttpServletRequest request, RedirectAttributes redirectAttributes,
			HttpSession session, Model model) {

		// 空のフォームを生成
		model.addAttribute("messageForm", new MessageForm());
		model.addAttribute("searchForm", new SearchForm());
		
		model.addAttribute("selectedIds", new ArrayList<String>());
		
		// 現在のページの情報
	    model.addAttribute("currentPage", "message");

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

		return PathConfig.MESSAGE_REGISTER_PAGE;
	}

	// 全体メッセージ登録の処理
	@PostMapping(PathConfig.MESSAGE_REGISTER_URL)
	public String sendMessage(@ModelAttribute MessageForm messageForm,
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {

		User user = (User) session.getAttribute("loggedInUser");
		
		// 現在のページの情報
	    model.addAttribute("currentPage", "message");

		// メッセージが未入力の場合のチェック
		if (messageForm.getMessage() == null || messageForm.getMessage().isEmpty()) {
			// メッセージが未入力の場合にエラーメッセージを追加
			model.addAttribute("nullErrorAll",
					messageSource.getMessage("null.error", null, Locale.getDefault()));

			// 空のフォームを生成
			model.addAttribute("messageForm", new MessageForm());
			model.addAttribute("searchForm", new SearchForm());
			
			model.addAttribute("selectedIds", new ArrayList<String>());

			model.addAttribute("employeeAdmin", user.getAdmin());

			return PathConfig.MESSAGE_REGISTER_PAGE; // 再度フォーム画面を表示
		}

		// 開始日と終了日がどちらも入力されている場合、開始日が終了日より後か確認
		if (messageForm.getStartDay() != null && messageForm.getEndDay() != null) {
			if (messageForm.getStartDay().isAfter(messageForm.getEndDay())) {
				// 開始日が終了日より後の場合にエラーメッセージを追加
				model.addAttribute("matchAll",
						messageSource.getMessage("endDate.before.startDate", null, null));

				// 空のフォームを生成
				model.addAttribute("messageForm", new MessageForm());
				model.addAttribute("searchForm", new SearchForm());

				model.addAttribute("employeeAdmin", user.getAdmin());

				return PathConfig.MESSAGE_REGISTER_PAGE;

			} else {
				// 日付が正しい場合、全体メッセージの登録
				messageRegisterService.saveMessage(messageForm, user.getId());

				// 成功メッセージをリダイレクト先に渡す
				redirectAttributes.addFlashAttribute("successMessageAll",
						messageSource.getMessage("success.message.all", null, null));

				// メッセージ送信後、リダイレクト
				return PathConfig.MESSAGE_REDIRECT_URL;
			}
		}
		// 未入力エラーメッセージ
		model.addAttribute("nullErrorAll",
				messageSource.getMessage("null.error", null, null));

		// 空のフォームを生成
		model.addAttribute("messageForm", new MessageForm());
		model.addAttribute("searchForm", new SearchForm());

		model.addAttribute("employeeAdmin", user.getAdmin());

		return PathConfig.MESSAGE_REGISTER_PAGE;

	}

	// 検索の処理
	@PostMapping(PathConfig.MESSAGE_SEARCH_URL)
	public String sendMessage(@RequestParam("selectedUserNames") String selectedUserNames,
			@RequestParam("selectedUserIds") String selectedUserIds,
			@ModelAttribute SearchForm searchForm,
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {
		
		// 現在のページの情報
	    model.addAttribute("currentPage", "message");

		// 受け取ったselectedNamesをカンマ区切りで分割してリストに変換
		List<String> selectedNames = new ArrayList<>();
		if (selectedUserNames != null && !selectedUserNames.isEmpty()) {
			selectedNames = Arrays.asList(selectedUserNames.split(","));
		}
		// 受け取ったselectedUserIdsをカンマ区切りで分割してリストに変換
		List<String> selectedIds = new ArrayList<>();
		if (selectedUserIds != null && !selectedUserIds.isEmpty()) {
			selectedIds = Arrays.asList(selectedUserIds.split(","));
		}

		// 再度選択されたユーザーIDをモデルに追加
		model.addAttribute("selectedIds", selectedIds);
		// 再度選択されたユーザー名をモデルに追加
		model.addAttribute("selectedNames", selectedNames);
		
		if (selectedIds == null) {
			model.addAttribute("selectedIds", new ArrayList<String>());
		}

		User user = (User) session.getAttribute("loggedInUser");

		// 社員名の入力チェック
		if (searchForm.getSearchName() != null && !searchForm.getSearchName().isEmpty() &&
				searchForm.getSearchName().length() > 30) {
			// 未入力エラーメッセージ
			model.addAttribute("nameLength",
					messageSource.getMessage("name.length", null, null));

			// 空のフォームを生成
			model.addAttribute("messageForm", new MessageForm());
			// 送信されたsearchFormをそのまま再表示
			model.addAttribute("searchForm", searchForm);

			model.addAttribute("employeeAdmin", user.getAdmin());

			return PathConfig.MESSAGE_REGISTER_PAGE;
		}
		// 社員番号の入力チェック
		if (searchForm.getSearchId() != null && searchForm.getSearchId() < 10000) {
			// 未入力エラーメッセージ
			model.addAttribute("idNumber",
					messageSource.getMessage("id.number", null, null));

			// 空のフォームを生成
			model.addAttribute("messageForm", new MessageForm());
			// 送信されたsearchFormをそのまま再表示
			model.addAttribute("searchForm", searchForm);

			model.addAttribute("employeeAdmin", user.getAdmin());

			return PathConfig.MESSAGE_REGISTER_PAGE;
		}

		// あいまい検索をサービスを通じて実行
		List<User> userList = messageRegisterService.searchUsers(searchForm);

		// 検索結果をモデルに追加
		model.addAttribute("userList", userList);

		// セッションに検索フォームを保存
		session.setAttribute("searchForm", searchForm);

		model.addAttribute("employeeAdmin", user.getAdmin());

		// 空のフォームを生成
		model.addAttribute("messageForm", new MessageForm());
		model.addAttribute("searchForm", new SearchForm());

		return PathConfig.MESSAGE_REGISTER_PAGE;
	}

	// 個別メッセージ送信の処理
	@PostMapping(PathConfig.INDIVIDUAL_MESSAGE_REGISTER_URL)
	public String sendIndividualMessage(
			@RequestParam("individualMessage") String individualMessage,
			@RequestParam("userIdsToSend") List<Integer> userIdsToSend,
			@RequestParam(value = "startDate", required = false) LocalDate startDate,
			@RequestParam(value = "endDate", required = false) LocalDate endDate,
			RedirectAttributes redirectAttributes, HttpSession session, Model model) {

		User user = (User) session.getAttribute("loggedInUser");
		
		// 現在のページの情報
	    model.addAttribute("currentPage", "message");

		// 送信先ユーザーIDが選択されていない場合のチェック
		if (userIdsToSend == null || userIdsToSend.isEmpty()) {
			// ユーザーが選択されていない場合にエラーメッセージを追加
			model.addAttribute("userIdsError",
					messageSource.getMessage("userIds.empty", null, null));

			// 検索条件に基づいたユーザーリストを再取得して表示
			List<User> userList = messageRegisterService.searchUsers((SearchForm) session.getAttribute("searchForm"));
			model.addAttribute("userList", userList);

			// 空のフォームを生成
			model.addAttribute("messageForm", new MessageForm());
			model.addAttribute("searchForm", new SearchForm());
			
			model.addAttribute("selectedIds", new ArrayList<String>());

			model.addAttribute("employeeAdmin", user.getAdmin());

			return PathConfig.MESSAGE_REGISTER_PAGE; // 再度フォーム画面を表示
		}

		// メッセージが未入力の場合のチェック
		if (individualMessage == null || individualMessage.isEmpty()) {
			// メッセージが未入力の場合にエラーメッセージを追加
			model.addAttribute("nullErrorMessage",
					messageSource.getMessage("null.error.message", null, null));

			// 検索条件に基づいたユーザーリストを再取得して表示
			List<User> userList = messageRegisterService.searchUsers((SearchForm) session.getAttribute("searchForm"));
			model.addAttribute("userList", userList);

			// 空のフォームを生成
			model.addAttribute("messageForm", new MessageForm());
			model.addAttribute("searchForm", new SearchForm());
			
			model.addAttribute("selectedIds", new ArrayList<String>());

			model.addAttribute("employeeAdmin", user.getAdmin());

			return PathConfig.MESSAGE_REGISTER_PAGE; // 再度フォーム画面を表示
		}

		// 開始日と終了日がどちらも入力されている場合、開始日が終了日より後か確認
		if (startDate != null && endDate != null) {
			if (startDate.isAfter(endDate)) {
				// 開始日が終了日より後の場合にエラーメッセージを追加
				model.addAttribute("match",
						messageSource.getMessage("endDate.before.startDate", null, null));

				// 検索条件に基づいたユーザーリストを再取得して表示
				List<User> userList = messageRegisterService
						.searchUsers((SearchForm) session.getAttribute("searchForm"));
				model.addAttribute("userList", userList);

				// 空のフォームを生成
				model.addAttribute("messageForm", new MessageForm());
				model.addAttribute("searchForm", new SearchForm());
				
				model.addAttribute("selectedIds", new ArrayList<String>());

				model.addAttribute("employeeAdmin", user.getAdmin());

				return PathConfig.MESSAGE_REGISTER_PAGE;

			} else {
				// メッセージ送信処理をサービスクラスで実行
				messageRegisterService.sendIndividualMessage(individualMessage, userIdsToSend, startDate, endDate);

				// 成功メッセージをリダイレクト先に渡す
				redirectAttributes.addFlashAttribute("successMessage",
						messageSource.getMessage("success.message", null, null));

				// 個別メッセージ送信後、searchFormをセッションから削除
				session.removeAttribute("searchForm");

				// メッセージ送信後、リダイレクト
				return PathConfig.MESSAGE_REDIRECT_URL;
			}
		}

		// 未入力エラーメッセージ
		model.addAttribute("nullError",
				messageSource.getMessage("null.error", null, null));

		// 検索条件に基づいたユーザーリストを再取得して表示
		List<User> userList = messageRegisterService.searchUsers((SearchForm) session.getAttribute("searchForm"));
		model.addAttribute("userList", userList);

		// 空のフォームを生成
		model.addAttribute("messageForm", new MessageForm());
		model.addAttribute("searchForm", new SearchForm());
		
		model.addAttribute("selectedIds", new ArrayList<String>());

		model.addAttribute("employeeAdmin", user.getAdmin());

		return PathConfig.MESSAGE_REGISTER_PAGE;

	}
}
