package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.config.PathConfig;
import com.example.demo.entity.User;
import com.example.demo.form.MessageForm;
import com.example.demo.service.HomeService;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

	@Autowired
	private HomeService homeService;

	@ModelAttribute
	public void addCssPath(Model model) {
		model.addAttribute("cssPaths", new String[] { "/css/base.css", "/css/home.css" });
	}

	@GetMapping(PathConfig.HOME_URL)
	public String showHomePage(HttpSession session, Model model) {

		User user = (User) session.getAttribute("loggedInUser");

		// モデルにユーザー情報を追加
		model.addAttribute("employeeId", user.getId());
		model.addAttribute("employeeName", user.getName());
		model.addAttribute("employeeAdmin", user.getAdmin());

		// 全体お知らせを取得
		List<MessageForm> Allmessages = homeService.getMessages();
		// ユーザーIDを基に個別メッセージを取得
		List<MessageForm> messages = homeService.getMessagesForUser(user.getId());
		// 勤怠申請と有給休暇申請を統合して取得
		List<String> allRequestMessages = homeService.getAllRequestMessages(user.getId());

		// メッセージをモデルに追加
		model.addAttribute("allmessages", Allmessages);
		model.addAttribute("messages", messages);
		model.addAttribute("allRequestMessages", allRequestMessages);
		
		// 現在のページの情報
	    model.addAttribute("currentPage", "home");

		return PathConfig.HOME_PAGE;
	}
}
