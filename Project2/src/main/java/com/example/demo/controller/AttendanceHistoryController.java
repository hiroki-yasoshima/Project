package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.config.PathConfig;
import com.example.demo.entity.User;
import com.example.demo.entity.WorktimeSummary;
import com.example.demo.service.AttendanceHistoryService;
import com.example.demo.service.YearMonthService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AttendanceHistoryController {

	@Autowired
	private YearMonthService yearMonthService;

	@Autowired
	private AttendanceHistoryService attendanceHistoryService;
	
	@ModelAttribute
	public void addCssPath(Model model) {
		model.addAttribute("cssPaths", new String[] { "/css/base.css", "/css/attendance-history.css" });
	}

	@GetMapping(PathConfig.ATTENDANCE_HISTORY_URL)
	public String showHistoryPage(HttpSession session, Model model) {

		// セッションからユーザー情報を取得
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		model.addAttribute("employeeAdmin", loggedInUser.getAdmin());
		// ユーザーIDを取得
		Integer userId = loggedInUser.getId();

		// サービスクラスを使用して現在の年を取得
		int currentYear = yearMonthService.getCurrentYear();
		// サービスクラスを使用して年のリストを取得
		var years = yearMonthService.getYearsList();

		// 勤怠履歴データの取得
		List<WorktimeSummary> history = attendanceHistoryService.getHistory(currentYear, userId);

		model.addAttribute("history", history);
		model.addAttribute("years", years);
		model.addAttribute("currentYear", currentYear);
		
		// 現在のページの情報
	    model.addAttribute("currentPage", "attendance-history");

		return PathConfig.ATTENDANCE_HISTORY_PAGE;

	}

	@PostMapping(PathConfig.ATTENDANCE_HISTORY_UPDATE_URL)
	public String updateHistory(@RequestParam int year,
			HttpSession session, Model model) {

		// セッションからユーザー情報を取得
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		model.addAttribute("employeeAdmin", loggedInUser.getAdmin());
		// ユーザーIDを取得
		Integer userId = loggedInUser.getId();

		// サービスクラスを使用して年のリストを取得
		var years = yearMonthService.getYearsList();

		// 勤怠履歴データの取得
		List<WorktimeSummary> history = attendanceHistoryService.getHistory(year, userId);

		model.addAttribute("history", history);
		model.addAttribute("currentYear", year);
		model.addAttribute("years", years);
		
		// 現在のページの情報
	    model.addAttribute("currentPage", "attendance-history");

		return PathConfig.ATTENDANCE_HISTORY_PAGE;
	}

}
