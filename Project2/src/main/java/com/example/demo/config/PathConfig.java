package com.example.demo.config;

public class PathConfig {
	
	// ログイン画面
	public static final String LOGIN_PAGE = "login";
	public static final String LOGIN_URL = "/" + LOGIN_PAGE;
	public static final String LOGIN_REDIRECT_URL = "redirect:" + LOGIN_URL;
	
	// ログアウト機能
	public static final String LOGOUT_URL = "/logout";
	public static final String LOGOUT_REDIRECT_URL = "redirect:" + LOGOUT_URL;
	
	// ホーム画面
	public static final String HOME_PAGE = "home";
	public static final String HOME_URL = "/" + HOME_PAGE;
	public static final String HOME_REDIRECT_URL = "redirect:" + HOME_URL;
	
	// 勤怠登録画面
	public static final String ATTENDANCE_PAGE = "attendance";
	public static final String ATTENDANCE_URL = "/" + ATTENDANCE_PAGE;
	public static final String ATTENDANCE_UPDATE_URL = ATTENDANCE_URL + "/update";
	public static final String ATTENDANCE_SAVE_URL = ATTENDANCE_URL + "/save";
	public static final String ATTENDANCE_REDIRECT_URL = "redirect:" + ATTENDANCE_URL;
	
	// 申請画面
	public static final String REQUEST_PAGE = "request";
	public static final String REQUEST_URL = "/" + REQUEST_PAGE;
	public static final String REQUEST_UPDATE_URL = REQUEST_URL + "/update";
	public static final String REQUEST_SAVE_URL = REQUEST_URL + "/save";
	
	// 勤怠履歴画面
	public static final String ATTENDANCE_HISTORY_PAGE = "attendance-history";
	public static final String ATTENDANCE_HISTORY_URL = "/" + ATTENDANCE_HISTORY_PAGE;
	public static final String ATTENDANCE_HISTORY_UPDATE_URL = ATTENDANCE_HISTORY_URL + "/update";
	
	// ユーザー登録・削除画面
	public static final String USER_MANAGEMENT_PAGE = "user-management";
	public static final String USER_MANAGEMENT_URL = "/" + USER_MANAGEMENT_PAGE;
	public static final String USER_MANAGEMENT_REDIRECT_URL  = "redirect:" + USER_MANAGEMENT_URL;
	
	// ユーザー一覧画面
	public static final String USER_LIST_PAGE = "user-list";
	public static final String USER_DETAIL_PAGE = "user-detail";
	public static final String USER_LIST_URL = "/" + USER_LIST_PAGE;
	public static final String LIST_UPDATE_URL = "/list/update";
	public static final String DETAIL_YEAR_URL = "/detail/year";
	public static final String DETAIL_MONTH_URL = "/detail/month";
	public static final String DETAIL_APPROVAL_URL = "/detail/approval";
	
	// メッセージ登録画面
	public static final String MESSAGE_REGISTER_PAGE = "message-register";
	public static final String MESSAGE_URL = "/message";
	public static final String MESSAGE_REGISTER_URL = "/" + MESSAGE_REGISTER_PAGE;
	public static final String MESSAGE_SEARCH_URL = "/message-search";
	public static final String INDIVIDUAL_MESSAGE_REGISTER_URL = "/individual-message-register";
	public static final String MESSAGE_REDIRECT_URL = "redirect:" + MESSAGE_URL;
}
