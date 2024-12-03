package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.config.PathConfig;
import com.example.demo.entity.Request;
import com.example.demo.entity.User;
import com.example.demo.entity.WorktimeSummary;
import com.example.demo.model.Attendance;
import com.example.demo.service.AttendanceHistoryService;
import com.example.demo.service.AttendanceService;
import com.example.demo.service.UserListService;
import com.example.demo.service.YearMonthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserListController {

	@Autowired
	private YearMonthService yearMonthService;

	@Autowired
	private UserListService userListService;

	@Autowired
	private AttendanceService attendanceService;

	@Autowired
	private AttendanceHistoryService attendanceHistoryService;

	@Autowired
	private MessageSource messageSource;

	@GetMapping(PathConfig.USER_LIST_URL)
	public String showUserListPage(HttpServletRequest request, RedirectAttributes redirectAttributes,
			HttpSession session, Model model) {

		model.addAttribute("cssPaths", new String[] { "/css/base.css", "/css/user-list.css" });

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

		// 現在のページの情報
		model.addAttribute("currentPage", "user-list");

		// サービスクラスを使用して現在の年と月を取得
		int currentYear = yearMonthService.getCurrentYear();
		int currentMonth = yearMonthService.getCurrentMonth();

		// サービスクラスを使用して年と月のリストを取得
		var years = yearMonthService.getYearsList();
		var months = yearMonthService.getMonthsList();

		// ユーザー情報の取得
		List<User> users = userListService.getUser();
		// userId とその勤務情報をペアにした Map を作成
		Map<Integer, WorktimeSummary> userWorktimeSummaryMap = new HashMap<>();

		// ユーザーごとの勤務履歴を取得し、Map に格納
		for (User eachUser : users) {
			List<WorktimeSummary> worktimeSummaryList = userListService.getWorktimeSummary(eachUser.getId(),
					currentYear,
					currentMonth);
			if (!worktimeSummaryList.isEmpty()) {
				// 勤務履歴があれば最初のものを格納
				userWorktimeSummaryMap.put(eachUser.getId(), worktimeSummaryList.get(0));
			} else {
				// 勤務履歴がない場合は null を格納
				userWorktimeSummaryMap.put(eachUser.getId(), null);
			}
		}

		model.addAttribute("years", years);
		model.addAttribute("months", months);
		model.addAttribute("currentYear", currentYear);
		model.addAttribute("currentMonth", currentMonth);
		// ユーザー情報
		model.addAttribute("users", users);
		// userId と 勤怠情報のマップ
		model.addAttribute("userWorktimeSummaryMap", userWorktimeSummaryMap);

		return PathConfig.USER_LIST_PAGE;
	}

	@PostMapping(PathConfig.LIST_UPDATE_URL)
	public String updateUserList(@RequestParam int year,
			@RequestParam int month, HttpSession session, Model model) {

		model.addAttribute("cssPaths", new String[] { "/css/base.css", "/css/user-list.css" });

		User user = (User) session.getAttribute("loggedInUser");
		model.addAttribute("employeeAdmin", user.getAdmin());

		// 現在のページの情報
		model.addAttribute("currentPage", "user-list");

		// ユーザー情報の取得
		List<User> users = userListService.getUser();
		// userId とその勤務情報をペアにした Map を作成
		Map<Integer, WorktimeSummary> userWorktimeSummaryMap = new HashMap<>();

		// ユーザーごとの勤務履歴を取得し、Map に格納
		for (User eachUser : users) {
			List<WorktimeSummary> worktimeSummaryList = userListService.getWorktimeSummary(eachUser.getId(), year,
					month);
			if (!worktimeSummaryList.isEmpty()) {
				// 勤務履歴があれば最初のものを格納
				userWorktimeSummaryMap.put(eachUser.getId(), worktimeSummaryList.get(0));
			} else {
				// 勤務履歴がない場合は null を格納
				userWorktimeSummaryMap.put(eachUser.getId(), null);
			}
		}

		model.addAttribute("currentYear", year);
		model.addAttribute("currentMonth", month);
		// 年と月のリストを再度渡す
		model.addAttribute("years", yearMonthService.getYearsList());
		model.addAttribute("months", yearMonthService.getMonthsList());
		// ユーザー情報
		model.addAttribute("users", users);
		// userId と 勤怠情報のマップ
		model.addAttribute("userWorktimeSummaryMap", userWorktimeSummaryMap);

		return PathConfig.USER_LIST_PAGE;
	}

	@PostMapping(PathConfig.DETAIL_YEAR_URL)
	public String showUserDetailYear(@RequestParam("id") Integer id,
			@RequestParam("year") int year,
			@RequestParam("month") int month,
			HttpSession session, Model model) {

		model.addAttribute("cssPaths", new String[] { "/css/base.css", "/css/user-detail.css" });

		User user = (User) session.getAttribute("loggedInUser");
		model.addAttribute("employeeAdmin", user.getAdmin());

		// 現在のページの情報
		model.addAttribute("currentPage", "user-list");

		// ユーザー情報の取得
		User selectUser = userListService.getUserById(id);

		// 勤怠履歴データの取得
		List<WorktimeSummary> history = attendanceHistoryService.getHistory(year, id);

		model.addAttribute("selectUser", selectUser);
		model.addAttribute("currentYear", year);
		model.addAttribute("currentMonth", month);
		model.addAttribute("history", history);
		// 年のリストを再度渡す
		model.addAttribute("years", yearMonthService.getYearsList());

		return PathConfig.USER_DETAIL_PAGE;
	}

	@PostMapping(PathConfig.DETAIL_MONTH_URL)
	public String showUserDetailMonth(@RequestParam("id") Integer id,
			@RequestParam("year") int year,
			@RequestParam("month") int month,
			HttpSession session, Model model) {

		model.addAttribute("cssPaths", new String[] { "/css/base.css", "/css/user-detail.css" });

		// 現在のページの情報
		model.addAttribute("currentPage", "user-list");

		User user = (User) session.getAttribute("loggedInUser");
		model.addAttribute("employeeAdmin", user.getAdmin());

		// ユーザー情報の取得
		User selectUser = userListService.getUserById(id);

		// 勤怠履歴データの取得
		List<WorktimeSummary> history = attendanceHistoryService.getHistory(year, id);

		// 勤怠データを取得
		List<Attendance> attendances = attendanceService.getAttendances(year, month, id);
		// 0 から 480 までの値を 15 分ごとにリストにしたものを取得
		List<String> timeOptions = attendanceService.getTimeOptions();
		// 時間と分の選択肢を取得
		List<String> hours = attendanceService.getHoursList();
		List<String> minutes = attendanceService.getMinutesList();

		List<Request> request = userListService.getRequest(year, month, id);

		model.addAttribute("currentYear", year);
		model.addAttribute("currentMonth", month);
		model.addAttribute("attendances", attendances);
		model.addAttribute("selectUser", selectUser);
		model.addAttribute("history", history);
		model.addAttribute("timeOptions", timeOptions);
		model.addAttribute("hours", hours);
		model.addAttribute("minutes", minutes);
		model.addAttribute("request", request);
		// 年のリストを再度渡す
		model.addAttribute("years", yearMonthService.getYearsList());

		return PathConfig.USER_DETAIL_PAGE;
	}

	@PostMapping(PathConfig.DETAIL_APPROVAL_URL)
	public String approvalAttendance(
			@RequestParam int currentYear,
			@RequestParam int currentMonth,
			@RequestParam("id") Integer id,
			@RequestParam("startTimeHour") List<String> startTimeHours,
			@RequestParam("startTimeMinute") List<String> startTimeMinutes,
			@RequestParam("endTimeHour") List<String> endTimeHours,
			@RequestParam("endTimeMinute") List<String> endTimeMinutes,
			@RequestParam("restTime") List<String> restTimes,
			@RequestParam("deductionTime") List<String> deductionTimes,
			@RequestParam("remarks") List<String> remarksList,
			HttpSession session, Model model) {

		model.addAttribute("cssPaths", new String[] { "/css/base.css", "/css/user-detail.css" });

		// 現在のページの情報
		model.addAttribute("currentPage", "user-list");

		// セッションからユーザー情報を取得
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		// ユーザーIDを取得
		Integer userId = loggedInUser.getId();

		// 勤怠データを保存または更新し、エラーメッセージを取得
		List<String> errorMessages = userListService.updateAttendances(currentYear, currentMonth,
				startTimeHours, startTimeMinutes, endTimeHours, endTimeMinutes, restTimes, deductionTimes,
				remarksList, id, userId);

		if (errorMessages.isEmpty()) {
			// サービスを使って申請を保存
			userListService.saveRequest(id, userId, currentYear, currentMonth);
			model.addAttribute("approvalSaved", messageSource.getMessage("approval.saved", null, null));
		} else if (!errorMessages.isEmpty()) {
			// エラーメッセージがあればモデルに追加
			model.addAttribute("errorMessages", errorMessages);
		}

		User user = (User) session.getAttribute("loggedInUser");
		model.addAttribute("employeeAdmin", user.getAdmin());

		// ユーザー情報の取得
		User selectUser = userListService.getUserById(id);

		// 勤怠履歴データの取得
		List<WorktimeSummary> history = attendanceHistoryService.getHistory(currentYear, id);

		// 勤怠データを取得
		List<Attendance> attendances = attendanceService.getAttendances(currentYear, currentMonth, id);
		// 0 から 480 までの値を 15 分ごとにリストにしたものを取得
		List<String> timeOptions = attendanceService.getTimeOptions();
		// 時間と分の選択肢を取得
		List<String> hours = attendanceService.getHoursList();
		List<String> minutes = attendanceService.getMinutesList();

		List<Request> request = userListService.getRequest(currentYear, currentMonth, id);

		model.addAttribute("currentYear", currentYear);
		model.addAttribute("currentMonth", currentMonth);
		model.addAttribute("attendances", attendances);
		model.addAttribute("selectUser", selectUser);
		model.addAttribute("history", history);
		model.addAttribute("timeOptions", timeOptions);
		model.addAttribute("hours", hours);
		model.addAttribute("minutes", minutes);
		model.addAttribute("request", request);
		// 年のリストを再度渡す
		model.addAttribute("years", yearMonthService.getYearsList());

		return PathConfig.USER_DETAIL_PAGE;
	}
}
