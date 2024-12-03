package com.example.demo.service;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Request;
import com.example.demo.entity.User;
import com.example.demo.entity.Worktime;
import com.example.demo.entity.WorktimeSummary;
import com.example.demo.repository.RequestMapper;
import com.example.demo.repository.UserMapper;
import com.example.demo.repository.WorktimeMapper;
import com.example.demo.repository.WorktimeSummaryMapper;

@Service
public class UserListService {

	@Autowired
	private WorktimeMapper worktimeMapper;

	@Autowired
	private WorktimeSummaryMapper worktimeSummaryMapper;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private RequestMapper requestMapper;

	@Autowired
	private MessageSource messageSource;

	// ユーザー情報を取得
	public List<User> getUser() {
		// DBからdeleteflagが0であるユーザー情報を取得
		return userMapper.selectAllUsers();
	}

	// WorktimeSummaryを取得
	public List<WorktimeSummary> getWorktimeSummary(int id, int year, int month) {
		List<WorktimeSummary> summaryList = new ArrayList<>();

		// 年と月を組み合わせたDateを作成
		// 月が１桁の場合は先頭に0を付ける
		String yearMonthStr = year + "-" + (month < 10 ? "0" + month : month);
		Date yearMonth = Date.valueOf(yearMonthStr + "-01"); // 日付は月の1日で固定

		// DBからデータを取得
		WorktimeSummary summary = worktimeSummaryMapper.getWorktimeSummaryByIdAndYearmonth(id, yearMonth);

		if (summary != null) {
			// 分単位の勤務時間を時間単位に変換
			summary.setWorkinghours(summary.getWorkinghours() / 60);
			summary.setActualworkinghours(summary.getActualworkinghours() / 60);
		}
		// リストに追加
		summaryList.add(summary);

		return summaryList;
	}

	// ユーザーidでユーザー情報を取得
	public User getUserById(Integer id) {
		// DBからdeleteflagが0であるユーザー情報を取得（リストを取得）
		return userMapper.selectById(id);
	}

	public List<Request> getRequest(int year, int month, int id) {
		List<Request> requestList = new ArrayList<>();

		// 指定された年と月の日数を取得
		LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
		int daysInMonth = firstDayOfMonth.lengthOfMonth();

		// 月の各日について申請件数を生成
		for (int i = 1; i <= daysInMonth; i++) {
			LocalDate currentDate = LocalDate.of(year, month, i);
			// LocalDate -> java.sql.Date への変換
			Date sqlDate = Date.valueOf(currentDate);

			// requestMapperを使用して、userIdと一致する日付の申請情報を取得
			List<Request> requestsForDay = requestMapper.selectByIdAndsplDate(id, sqlDate);

			// 取得した申請情報をリストに追加
			requestList.addAll(requestsForDay);

		}
		return requestList;
	}

	// 勤怠データを保存または更新する
	public List<String> updateAttendances(int currentYear, int currentMonth,
			List<String> startTimeHours, List<String> startTimeMinutes,
			List<String> endTimeHours, List<String> endTimeMinutes,
			List<String> restTimes, List<String> deductionTimes,
			List<String> remarksList, Integer id, Integer userId) {

		int workingDays = 0; // 出勤日数
		int workingHours = 0; // 勤務時間の総和
		int actualWorkingHours = 0; // 実労働時間の総和
		int deductionCount = 0; // 遅刻早退回数

		// エラーメッセージリストを追加
		List<String> errorMessages = new ArrayList<>();

		// 指定された年と月の日数を取得
		LocalDate firstDayOfMonth = LocalDate.of(currentYear, currentMonth, 1);
		int daysInMonth = firstDayOfMonth.lengthOfMonth();

		// エラーメッセージリストを日付ごとに格納するマップを作成
		Map<Integer, List<String>> errorMessagesMap = new HashMap<>();

		for (int i = 0; i < daysInMonth; i++) {

			int day = i + 1;

			// yyyy-MM-dd形式の文字列をLocalDateに変換
			LocalDate workDate = LocalDate.of(currentYear, currentMonth, day);

			// 勤務時間の処理
			String startTimeString = String.format("%02d:%02d:00", Integer.parseInt(startTimeHours.get(i)),
					Integer.parseInt(startTimeMinutes.get(i)));
			String endTimeString = String.format("%02d:%02d:00", Integer.parseInt(endTimeHours.get(i)),
					Integer.parseInt(endTimeMinutes.get(i)));

			Time startTime = Time.valueOf(startTimeString);
			Time endTime = Time.valueOf(endTimeString);
			Integer restTime = Integer.parseInt(restTimes.get(i));
			Integer deductionTime = Integer.parseInt(deductionTimes.get(i));
			String remarks = remarksList.get(i);

			// 既存の勤怠データを取得
			Worktime existingWorktime = worktimeMapper.findByIdAndDate(id, Date.valueOf(workDate));

			// 初期値と比較して変更があった場合のみ保存
			boolean isChanged = !startTimeString.equals("00:00:00") ||
					!endTimeString.equals("00:00:00") ||
					!restTimes.get(i).equals("60") ||
					!deductionTimes.get(i).equals("0") ||
					!remarksList.get(i).equals("");

			// 初期値と比較して変更があった場合
			// または
			// 初期値と比較して変更はないが既存の勤怠データがある場合
			// データの更新または新規登録
			if (isChanged || (!isChanged && existingWorktime != null)) {

				long workTimeMillis = endTime.getTime() - startTime.getTime(); // 終業時間 - 始業時間
				long workTimeInMinutes = workTimeMillis / (60 * 1000); // 勤務時間（分）

				// 1. 始業時間と就業時間の整合性チェック
				if (startTime.after(endTime)) {
					// メッセージソースからエラーメッセージを取得
					String errorMessage = messageSource.getMessage("startTime.after.endTime", null, null);
					errorMessages.add(day + errorMessage);
					errorMessagesMap.computeIfAbsent(day, k -> new ArrayList<>()).add(errorMessage);
				}

				// 2. 勤務時間と休憩時間の整合性チェック
				if (!(!isChanged && existingWorktime != null) && workTimeInMinutes <= restTime) {
					// メッセージソースからエラーメッセージを取得
					String errorMessage = messageSource.getMessage("workTimeInMinutes.restTime", null, null);
					errorMessages.add(day + errorMessage);
					errorMessagesMap.computeIfAbsent(day, k -> new ArrayList<>()).add(errorMessage);
				}

				// 3. 勤務時間と遅刻早退時間の整合性チェック
				if (workTimeInMinutes < deductionTime) {
					String errorMessage = messageSource.getMessage("workTimeInMinutes.deductionTime", null, null);
					errorMessages.add(day + errorMessage);
					errorMessagesMap.computeIfAbsent(day, k -> new ArrayList<>()).add(errorMessage);
				}

				// 4. もしremarksの長さが255文字を超えていたらエラーメッセージをリストに追加
				if (remarks.length() > 255) {
					String errorMessage = messageSource.getMessage("remarks.length", null, null);
					errorMessages.add(day + errorMessage);
					errorMessagesMap.computeIfAbsent(day, k -> new ArrayList<>()).add(errorMessage);
				}

				// もしエラーメッセージが1つでもある場合、次のループへ
				if (errorMessagesMap.containsKey(day) && !errorMessagesMap.get(day).isEmpty()) {
					continue;
				}

				if (existingWorktime != null) {
					// 既存データがある場合は更新
					existingWorktime.setStarttime(startTime);
					existingWorktime.setEndtime(endTime);
					existingWorktime.setResttime(restTime);
					existingWorktime.setDeductiontime(deductionTime);
					existingWorktime.setRemarks(remarks);
					existingWorktime.setUpdateday(new Timestamp(System.currentTimeMillis()));
					existingWorktime.setUpdateid(userId);

					// 更新処理を実行
					worktimeMapper.update(existingWorktime);
				} else {
					// データが存在しない場合は登録
					Worktime worktime = new Worktime();
					worktime.setId(id);
					worktime.setYearmonthday(Date.valueOf(workDate));
					worktime.setStarttime(startTime);
					worktime.setEndtime(endTime);
					worktime.setResttime(restTime);
					worktime.setDeductiontime(deductionTime);
					worktime.setRemarks(remarks);
					worktime.setCreateday(new Timestamp(System.currentTimeMillis()));
					worktime.setCreateid(userId);

					// 登録処理を実行
					worktimeMapper.insert(worktime);
				}
				// 勤務日数、勤務時間、実労働時間、遅刻早退回数の計算
				if (!startTimeString.equals("00:00:00") && !endTimeString.equals("00:00:00")) {
					workingDays++; // 勤務日数を増加

					// 勤務時間計算
					long workedHours = endTime.getTime() - startTime.getTime();
					long actualWorkedHoursInMillis = workedHours - (restTime * 60 * 1000); // 実労働時間は休憩時間を差し引く

					// 勤務時間（分単位で計算）
					workingHours += (int) (workedHours / (60 * 1000)); // 分単位
					actualWorkingHours += (int) (actualWorkedHoursInMillis / (60 * 1000)); // 実労働時間（分単位）

					// 遅刻回数を計算（deductionTimeが0でない場合に遅刻回数を増加）
					if (deductionTime > 0) {
						deductionCount++;
					}
				}
			}
		}
		
		if (!errorMessages.isEmpty()) {
			return errorMessages;
		}

		// 選択された年月の1日を指定
		Date currentYearMonth = Date.valueOf(LocalDate.of(currentYear, currentMonth, 1));

		// WorktimeSummaryを取得（userIdとyearMonthで一致するもの）
		WorktimeSummary summary = worktimeSummaryMapper.getWorktimeSummaryByIdAndYearmonth(id,
				currentYearMonth);

		// 選択した年月のWorktimeSummaryのworktimeflagをtrueにする
		if (summary != null) {
			// WorktimeSummaryが存在する場合、worktimeflagをtrueに設定
			summary.setId(id);
			summary.setRecognitionflag(true);
			summary.setUpdateday(new Timestamp(System.currentTimeMillis()));
			summary.setUpdateid(userId);
			summary.setWorkingdays(workingDays); // 勤務日数を加算
			summary.setWorkinghours(workingHours); // 勤務時間を加算
			summary.setActualworkinghours(actualWorkingHours); // 実労働時間を加算
			summary.setDeductioncount(deductionCount); // 遅刻早退回数を加算
			// 更新処理を実行
			worktimeSummaryMapper.updateApproval(summary);
		} else if (summary == null) {
			// WorktimeSummaryが存在しない場合、新規作成する場合の処理
			WorktimeSummary newSummary = new WorktimeSummary();
			newSummary.setId(id);
			newSummary.setYearmonth(currentYearMonth);
			newSummary.setWorkingdays(workingDays);
			newSummary.setWorkinghours(workingHours);
			newSummary.setActualworkinghours(actualWorkingHours);
			newSummary.setDeductioncount(deductionCount);
			newSummary.setRecognitionflag(true);
			newSummary.setCreateday(new Timestamp(System.currentTimeMillis()));
			newSummary.setCreateid(userId);
			// 新規登録処理を実行
			worktimeSummaryMapper.insertApproval(newSummary);
		}

		return errorMessages;
	}

	// 申請データを保存するメソッド
	public void saveRequest(Integer id, Integer userId, int currentYear, int currentMonth) {

		// 指定された年と月の日数を取得
		LocalDate firstDayOfMonth = LocalDate.of(currentYear, currentMonth, 1);
		int daysInMonth = firstDayOfMonth.lengthOfMonth();

		// 月の各日について申請件数を生成
		for (int i = 1; i <= daysInMonth; i++) {
			LocalDate currentDate = LocalDate.of(currentYear, currentMonth, i);
			// LocalDate -> java.sql.Date への変換
			Date sqlDate = Date.valueOf(currentDate);

			// 新しい申請オブジェクトを作成
			Request newSaveRequest = new Request();
			newSaveRequest.setRecognitionflag(true);
			// 更新者社員番号と更新日を設定
			newSaveRequest.setUpdateid(userId);
			newSaveRequest.setUpdateday(new Timestamp(System.currentTimeMillis()));

			// 新しい有給取得日数をrequestテーブルに更新
			requestMapper.updateRequest(id, sqlDate, newSaveRequest);
		}
	}

}
