package com.example.demo.controller;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
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

import com.example.demo.config.PathConfig;
import com.example.demo.entity.Request;
import com.example.demo.entity.User;
import com.example.demo.form.RequestForm;
import com.example.demo.service.RequestService;
import com.example.demo.service.YearMonthService;

import jakarta.servlet.http.HttpSession;

@Controller
public class RequestController {

	@Autowired
	private YearMonthService yearMonthService;

	@Autowired
	private RequestService requestService;

	@Autowired
	private MessageSource messageSource;
	
	@ModelAttribute
	public void addCssPath(Model model) {
		model.addAttribute("cssPaths", new String[] { "/css/base.css", "/css/request.css" });
	}

	@GetMapping(PathConfig.REQUEST_URL)
	public String showRequestPage(HttpSession session, Model model) {

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

		List<Request> request = requestService.getRequest(currentYear, currentMonth, userId);

		// 申請件数を計算
		int requestCount = request.size();

		model.addAttribute("request", request);
		model.addAttribute("years", years);
		model.addAttribute("months", months);
		model.addAttribute("currentYear", currentYear);
		model.addAttribute("currentMonth", currentMonth);
		model.addAttribute("requestCount", requestCount);
		model.addAttribute("requestForm", new RequestForm());
		
		// 現在のページの情報
	    model.addAttribute("currentPage", "request");
	    

		return PathConfig.REQUEST_PAGE;
	}

	@PostMapping(PathConfig.REQUEST_UPDATE_URL)
	public String updateRequest(@RequestParam int year, @RequestParam int month,
			HttpSession session, Model model) {

		// セッションからユーザー情報を取得
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		model.addAttribute("employeeAdmin", loggedInUser.getAdmin());
		// ユーザーIDを取得
		Integer userId = loggedInUser.getId();
		// 月が変更されたので、年と月を設定
		int currentYear = year;
		int currentMonth = month;

		// 現在の年月
		YearMonth nowMonth = YearMonth.now();
		// 選択された年月
		YearMonth currentYearMonth = YearMonth.of(year, month);

		List<Request> request = requestService.getRequest(currentYear, currentMonth, userId);
		// 申請件数を計算
		int requestCount = request.size();

		if (currentYearMonth.isBefore(nowMonth)) {
			model.addAttribute("disable", 1);
		}

		model.addAttribute("request", request);
		model.addAttribute("currentYear", currentYear);
		model.addAttribute("currentMonth", currentMonth);
		model.addAttribute("requestCount", requestCount);
		// 年と月のリストを再度渡す
		model.addAttribute("years", yearMonthService.getYearsList());
		model.addAttribute("months", yearMonthService.getMonthsList());
		model.addAttribute("requestForm", new RequestForm());
		
		// 現在のページの情報
	    model.addAttribute("currentPage", "request");

		return PathConfig.REQUEST_PAGE;
	}

	@PostMapping(PathConfig.REQUEST_SAVE_URL)
	public String updateRequest(
			@Validated @ModelAttribute("requestForm") RequestForm requestForm,
			BindingResult result, @RequestParam int year,
			@RequestParam int month, HttpSession session, Model model) {
		
		// ユーザー情報をセッションから取得
		User loggedInUser = (User) session.getAttribute("loggedInUser");
		model.addAttribute("employeeAdmin", loggedInUser.getAdmin());
		Integer userId = loggedInUser.getId();
		
		// 年と月の情報を取得
		int currentYear = year;
		int currentMonth = month;

		// 開始日と終了日を手動で比較
		String startDate = requestForm.getStartDate();
		String endDate = requestForm.getEndDate();

		if (startDate != null && endDate != null && !startDate.isEmpty() && !endDate.isEmpty()) {
			LocalDate start = LocalDate.parse(startDate);
			LocalDate end = LocalDate.parse(endDate);

			if (start.isAfter(end)) {
				// 開始日が終了日より後の場合にエラーメッセージを追加
				model.addAttribute("Match",
						messageSource.getMessage("endDate.before.startDate", null, null));

				List<Request> request = requestService.getRequest(currentYear, currentMonth, userId);

				// 申請件数を計算
				int requestCount = request.size();

				model.addAttribute("request", request);
				model.addAttribute("currentYear", currentYear);
				model.addAttribute("currentMonth", currentMonth);
				model.addAttribute("requestCount", requestCount);
				// 年と月のリストを再度渡す
				model.addAttribute("years", yearMonthService.getYearsList());
				model.addAttribute("months", yearMonthService.getMonthsList());
				model.addAttribute("requestForm", new RequestForm());
				
				// 現在のページの情報
			    model.addAttribute("currentPage", "request");
				
				return PathConfig.REQUEST_PAGE;
			}
			// 終了日 - 開始日 + 1 を計算して取得日数を求める
			long actualPaidDays = ChronoUnit.DAYS.between(start, end) + 1;

			// フォームに入力された有給取得日数（paidCount）と一致するか、または0.5より大きくないかをチェック
			Double paidCount = requestForm.getPaidCount();
			if (paidCount != null && !(actualPaidDays == paidCount || actualPaidDays == paidCount + 0.5)) {
				model.addAttribute("paidCountError",
						messageSource.getMessage("paidCount.error", null, null));

				List<Request> request = requestService.getRequest(currentYear, currentMonth, userId);

				// 申請件数を計算
				int requestCount = request.size();

				model.addAttribute("request", request);
				model.addAttribute("currentYear", currentYear);
				model.addAttribute("currentMonth", currentMonth);
				model.addAttribute("requestCount", requestCount);
				// 年と月のリストを再度渡す
				model.addAttribute("years", yearMonthService.getYearsList());
				model.addAttribute("months", yearMonthService.getMonthsList());
				model.addAttribute("requestForm", new RequestForm());
				
				// 現在のページの情報
			    model.addAttribute("currentPage", "request");
				
				return PathConfig.REQUEST_PAGE;
			}
		}

		// フォームからデータを取得
		String start = requestForm.getStartDate();
		String end = requestForm.getEndDate();
		Double paidCount = requestForm.getPaidCount();
		String reason = requestForm.getReason();

		if (result.hasErrors()) {
			if (start == null || start.isEmpty()) {
				model.addAttribute("startNullError",
						messageSource.getMessage("start.null.error", null, null));
			}
			if (end == null || end.isEmpty()) {
				model.addAttribute("endNullError",
						messageSource.getMessage("end.null.error", null, null));
			}
			if (paidCount == null) {
				model.addAttribute("countNullError",
						messageSource.getMessage("count.null.error", null, null));
			}
			if (reason == null || reason.isEmpty()) {
				model.addAttribute("reasonNullError",
						messageSource.getMessage("reason.null.error", null, null));
			}
			if (reason.length() > 255) {
				model.addAttribute("lengthError", messageSource.getMessage("length.error", null, null));
			}
			if (paidCount != null && paidCount > 31) {
				model.addAttribute("conutError", messageSource.getMessage("conut.error", null, null));
			}

			List<Request> request = requestService.getRequest(currentYear, currentMonth, userId);

			// 申請件数を計算
			int requestCount = request.size();

			model.addAttribute("request", request);
			model.addAttribute("currentYear", currentYear);
			model.addAttribute("currentMonth", currentMonth);
			model.addAttribute("requestCount", requestCount);
			// 年と月のリストを再度渡す
			model.addAttribute("years", yearMonthService.getYearsList());
			model.addAttribute("months", yearMonthService.getMonthsList());
			model.addAttribute("requestForm", new RequestForm());
			
			// 現在のページの情報
		    model.addAttribute("currentPage", "request");
			
			return PathConfig.REQUEST_PAGE;
		}

		// 新しい申請オブジェクトを作成
		Request newRequest = new Request();
		newRequest.setPaidstartday(Date.valueOf(start));
		newRequest.setPaidendday(Date.valueOf(end));
		newRequest.setPaidcount(paidCount);
		newRequest.setReason(reason);
		newRequest.setId(userId);
		newRequest.setRecognitionflag(false);

		// 登録者社員番号と登録日を設定
		newRequest.setCreateid(userId);
		newRequest.setCreateday(new Timestamp(System.currentTimeMillis()));

		// サービスを使って申請を保存
		requestService.saveRequest(userId, currentYear, currentMonth, newRequest);

		List<Request> request = requestService.getRequest(currentYear, currentMonth, userId);

		// 申請件数を計算
		int requestCount = request.size();

		model.addAttribute("request", request);
		model.addAttribute("currentYear", currentYear);
		model.addAttribute("currentMonth", currentMonth);
		model.addAttribute("requestCount", requestCount);
		// 年と月のリストを再度渡す
		model.addAttribute("years", yearMonthService.getYearsList());
		model.addAttribute("months", yearMonthService.getMonthsList());
		model.addAttribute("requestForm", new RequestForm());
		model.addAttribute("requestSaved", messageSource.getMessage("request.saved", null, null));

		// 現在のページの情報
	    model.addAttribute("currentPage", "request");
		
		return PathConfig.REQUEST_PAGE;

	}

}
