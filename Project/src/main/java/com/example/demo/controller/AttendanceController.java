package com.example.demo.controller;

import java.sql.Date;
import java.time.YearMonth;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.config.PathConfig;
import com.example.demo.entity.User;
import com.example.demo.model.Attendance;
import com.example.demo.repository.WorktimeSummaryMapper;
import com.example.demo.service.AttendanceService;
import com.example.demo.service.YearMonthService;

import jakarta.servlet.http.HttpSession;

@Controller
public class AttendanceController {

	@Autowired
	private YearMonthService yearMonthService;

	@Autowired
	private AttendanceService attendanceService;

	@Autowired
	private WorktimeSummaryMapper WorktimeSummaryMapper;
	
	@ModelAttribute
	public void addCssPath(Model model) {
		model.addAttribute("cssPaths", new String[] { "/css/base.css", "/css/attendance.css" });
	}

	@GetMapping(PathConfig.ATTENDANCE_URL)
	public String showAttendancePage(HttpSession session, Model model) {

		// セッションからユーザー情報を取得
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		model.addAttribute("employeeAdmin", loggedInUser.getAdmin());
		// ユーザーIDを取得
		Integer userId = loggedInUser.getId();

		// サービスクラスを使用して現在の年と月を取得
		int currentYear = yearMonthService.getCurrentYear();
		int currentMonth = yearMonthService.getCurrentMonth();

		// サービスクラスを使用して年と月のリストを取得
		var years = yearMonthService.getYearsList();
		var months = yearMonthService.getMonthsList();

		// 勤怠データを取得
		List<Attendance> attendances = attendanceService.getAttendances(currentYear, currentMonth, userId);
		// 0 から 480 までの値を 15 分ごとにリストにしたものを取得
		List<String> timeOptions = attendanceService.getTimeOptions();
		// 時間と分の選択肢を取得
		List<String> hours = attendanceService.getHoursList();
		List<String> minutes = attendanceService.getMinutesList();

		// モデルに渡す
		model.addAttribute("years", years);
		model.addAttribute("months", months);
		model.addAttribute("currentYear", currentYear);
		model.addAttribute("currentMonth", currentMonth);
		model.addAttribute("attendances", attendances);
		model.addAttribute("timeOptions", timeOptions);
		model.addAttribute("hours", hours);
		model.addAttribute("minutes", minutes);
		
		// 現在のページの情報
	    model.addAttribute("currentPage", "attendance");

		return PathConfig.ATTENDANCE_PAGE;
	}

	@PostMapping(PathConfig.ATTENDANCE_UPDATE_URL)
	public String updateAttendance(@RequestParam int year,
			@RequestParam int month, HttpSession session,
			Model model) {

		// セッションからユーザー情報を取得
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		model.addAttribute("employeeAdmin", loggedInUser.getAdmin());
		// ユーザーIDを取得
		Integer userId = loggedInUser.getId();
		
		// 月が変更されたので、年と月を設定
		int currentYear = year;
		int currentMonth = month;

		// １か月前
		YearMonth oneMonthAgo = YearMonth.now().minusMonths(1);
		// 現在の年月
		YearMonth nowMonth = YearMonth.now();
		// 選択された年月
		YearMonth currentYearMonth = YearMonth.of(year, month);

		// 1か月前の日付を取得
		Date oneMonthAgoDate = Date.valueOf(oneMonthAgo.atDay(1));

		// 1ヶ月前の申請フラグを取得
		Boolean worktimeFlag = WorktimeSummaryMapper.getWorktimeFlagByIdAndYearmonth(userId, oneMonthAgoDate);

		// worktimeFlag が null なら false として扱う
		if (worktimeFlag == null) {
			worktimeFlag = false;
		}

		// 勤怠データを取得
		List<Attendance> attendances = attendanceService.getAttendances(currentYear, currentMonth, userId);
		// 0 から 480 までの値を 15 分ごとにリストにしたものを取得
		List<String> timeOptions = attendanceService.getTimeOptions();
		// 時間と分の選択肢を取得
		List<String> hours = attendanceService.getHoursList();
		List<String> minutes = attendanceService.getMinutesList();

		// 現在の日付より２か月前なら disable に１を設定
		if (currentYearMonth.isBefore(oneMonthAgo)){
			model.addAttribute("disable", 1);
		} else if((currentYearMonth.isBefore(nowMonth)) && (worktimeFlag == true)){
			model.addAttribute("disable", 2);
		}

		// モデルにデータを設定
		model.addAttribute("currentYear", currentYear);
		model.addAttribute("currentMonth", currentMonth);
		model.addAttribute("attendances", attendances);

		// 年と月のリストを再度渡す
		model.addAttribute("years", yearMonthService.getYearsList());
		model.addAttribute("months", yearMonthService.getMonthsList());

		model.addAttribute("timeOptions", timeOptions);
		model.addAttribute("hours", hours);
		model.addAttribute("minutes", minutes);
		
		// 現在のページの情報
	    model.addAttribute("currentPage", "attendance");

		return PathConfig.ATTENDANCE_PAGE;
	}

	// ボタン押下時に呼び出されるメソッド
	@PostMapping(PathConfig.ATTENDANCE_SAVE_URL)
	public String saveAttendance(
			@RequestParam int year,
			@RequestParam int month,
			@RequestParam("startTimeHour") List<String> startTimeHours,
			@RequestParam("startTimeMinute") List<String> startTimeMinutes,
			@RequestParam("endTimeHour") List<String> endTimeHours,
			@RequestParam("endTimeMinute") List<String> endTimeMinutes,
			@RequestParam("restTime") List<String> restTimes,
			@RequestParam("deductionTime") List<String> deductionTimes,
			@RequestParam("remarks") List<String> remarksList,
			@RequestParam("action") String action,
			HttpSession session, RedirectAttributes redirectAttributes) {

		// セッションからユーザー情報を取得
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		// ユーザーIDを取得
		Integer userId = loggedInUser.getId();

		// 勤怠データを保存または更新し、エラーメッセージを取得
		List<String> errorMessages = attendanceService.saveOrUpdateAttendances(year, month,
				startTimeHours, startTimeMinutes, endTimeHours, endTimeMinutes, restTimes, deductionTimes, remarksList,
				action, userId);

		// エラーメッセージがあればモデルに追加
		if (!errorMessages.isEmpty()) {
			redirectAttributes.addFlashAttribute("errorMessages", errorMessages);
		}

		return PathConfig.ATTENDANCE_REDIRECT_URL;
	}

}
