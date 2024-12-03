package com.example.demo.service;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Worktime;
import com.example.demo.entity.WorktimeSummary;
import com.example.demo.model.Attendance;
import com.example.demo.repository.WorktimeMapper;
import com.example.demo.repository.WorktimeSummaryMapper;

@Service
public class AttendanceService {

	@Autowired
	private WorktimeMapper worktimeMapper;

	@Autowired
	private WorktimeSummaryMapper worktimeSummaryMapper;

	@Autowired
	private MessageSource messageSource;

	@Autowired
	private HolidayService holidayService;

	// 勤怠データを月ごとに取得するメソッド
	public List<Attendance> getAttendances(int year, int month, Integer userId) {
		List<Attendance> attendances = new ArrayList<>();

		// 指定された年と月の日数を取得
		LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
		int daysInMonth = firstDayOfMonth.lengthOfMonth();

		// 祝日データを取得
		Map<String, String> holidays = holidayService.getHolidays(year);

		// 月の各日について勤怠データを生成
		for (int i = 1; i <= daysInMonth; i++) {
			LocalDate date = LocalDate.of(year, month, i);
			String weekday = date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.JAPAN);
			// 祝日情報を取得して、祝日があればその名前を設定
			String holiday = holidays.get(date.toString()); // 祝日があれば取得

			// データベースからその月の全勤怠データを取得
			List<Worktime> worktimes = worktimeMapper.selectAll(date, userId);

			// その日に該当するデータがあるかチェック
			Worktime matchingWorktime = null;
			for (Worktime worktime : worktimes) {
				if (worktime.getYearmonthday().toLocalDate().equals(date)) {
					matchingWorktime = worktime;
					break;
				}
			}

			// データが見つかった場合、その値を使い、見つからない場合は初期値を使用
			if (matchingWorktime != null) {
				attendances.add(new Attendance(date, weekday, holiday,
						matchingWorktime.getStarttime().toString(), // Time -> String
						matchingWorktime.getEndtime().toString(), // Time -> String
						matchingWorktime.getResttime().toString(), // Integer -> String
						matchingWorktime.getDeductiontime().toString(), // Integer -> String
						// 備考がnullの場合空文字にする
						matchingWorktime.getRemarks() != null ? matchingWorktime.getRemarks() : ""));
			} else {
				// 勤怠データがない場合は初期値で設定
				attendances.add(new Attendance(date, weekday, holiday, "0:00", "0:00", "60", "0", ""));
			}
		}

		return attendances;
	}

	// 時間の選択肢（0 ~ 24）
	public List<String> getHoursList() {
		List<String> hours = new ArrayList<>();
		for (int i = 0; i <= 24; i++) {
			hours.add(String.format("%02d", i));
		}
		return hours;
	}

	// 分の選択肢（00, 10, 20, ..., 50）
	public List<String> getMinutesList() {
		List<String> minutes = new ArrayList<>();
		for (int i = 0; i < 60; i += 10) {
			minutes.add(String.format("%02d", i));
		}
		return minutes;
	}

	// 0 から 480 までの値を 15 分ごとにリスト化
	public List<String> getTimeOptions() {
		List<String> timeOptions = new ArrayList<>();
		for (int i = 0; i <= 480; i += 15) {
			// 時間をStringに変換してリストに追加
			timeOptions.add(String.format("%01d", i));
		}
		return timeOptions;
	}

	// 勤怠データを保存または更新する
	public List<String> saveOrUpdateAttendances(int year, int month,
			List<String> startTimeHours, List<String> startTimeMinutes,
			List<String> endTimeHours, List<String> endTimeMinutes,
			List<String> restTimes, List<String> deductionTimes,
			List<String> remarksList, String action, Integer userId) {

		int workingDays = 0; // 出勤日数
		int workingHours = 0; // 勤務時間の総和
		int actualWorkingHours = 0; // 実労働時間の総和
		int deductionCount = 0; // 遅刻早退回数

		// エラーメッセージリストを追加
		List<String> errorMessages = new ArrayList<>();

		// 指定された年と月の日数を取得
		LocalDate firstDayOfMonth = LocalDate.of(year, month, 1);
		int daysInMonth = firstDayOfMonth.lengthOfMonth();

		// エラーメッセージリストを日付ごとに格納するマップを作成
		Map<Integer, List<String>> errorMessagesMap = new HashMap<>();

		for (int i = 0; i < daysInMonth; i++) {

			int day = i + 1;

			// yyyy-MM-dd形式の文字列をLocalDateに変換
			LocalDate workDate = LocalDate.of(year, month, day);

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
			Worktime existingWorktime = worktimeMapper.findByIdAndDate(userId, Date.valueOf(workDate));

			// 初期値と比較して変更があるかの確認
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
					worktime.setId(userId);
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

		// WorktimeSummaryを取得（userIdとyearMonthで一致するもの）
		WorktimeSummary summary = worktimeSummaryMapper.getWorktimeSummaryByIdAndYearmonth(userId,
				Date.valueOf(firstDayOfMonth));

		// actionがsubmitの場合、選択した年月のWorktimeSummaryのworktimeflagをtrueにする
		if ((summary != null) && ("submit".equals(action))) {
			// WorktimeSummaryが存在する場合、worktimeflagをtrueに設定
			summary.setWorktimeflag(true);
			summary.setUpdateday(new Timestamp(System.currentTimeMillis()));
			summary.setUpdateid(userId);
			summary.setWorkingdays(workingDays); // 勤務日数を加算
			summary.setWorkinghours(workingHours); // 勤務時間を加算
			summary.setActualworkinghours(actualWorkingHours); // 実労働時間を加算
			summary.setDeductioncount(deductionCount); // 遅刻早退回数を加算
			// 更新処理を実行
			worktimeSummaryMapper.update(summary);
			if (errorMessages.isEmpty()) {
				// 申請成功メッセージの追加
				errorMessages.add(messageSource.getMessage("submit.successful", null, null));
			}

		} else if ((summary == null) && ("submit".equals(action))) {
			// WorktimeSummaryが存在しない場合、新規作成する場合の処理
			WorktimeSummary newSummary = new WorktimeSummary();
			newSummary.setId(userId);
			newSummary.setYearmonth(Date.valueOf(firstDayOfMonth));
			newSummary.setWorkingdays(workingDays);
			newSummary.setWorkinghours(workingHours);
			newSummary.setActualworkinghours(actualWorkingHours);
			newSummary.setDeductioncount(deductionCount);
			newSummary.setWorktimeflag(true);
			newSummary.setCreateday(new Timestamp(System.currentTimeMillis()));
			newSummary.setCreateid(userId);
			// 新規登録処理を実行
			worktimeSummaryMapper.insert(newSummary);
			if (errorMessages.isEmpty()) {
				// 申請成功メッセージの追加
				errorMessages.add(messageSource.getMessage("submit.successful", null, null));
			}
			
		} else if (summary != null && ("save".equals(action))) {
			summary.setWorktimeflag(false);
			summary.setUpdateday(new Timestamp(System.currentTimeMillis()));
			summary.setUpdateid(userId);
			summary.setWorkingdays(workingDays); // 勤務日数を加算
			summary.setWorkinghours(workingHours); // 勤務時間を加算
			summary.setActualworkinghours(actualWorkingHours); // 実労働時間を加算
			summary.setDeductioncount(deductionCount); // 遅刻早退回数を加算
			// 更新処理を実行
			worktimeSummaryMapper.update(summary);
			if (errorMessages.isEmpty()) {
				// 保存成功メッセージの追加
				errorMessages.add(messageSource.getMessage("save.successful", null, null));
			}
			
		} else if (summary == null && ("save".equals(action))) {
			WorktimeSummary newSummary = new WorktimeSummary();
			newSummary.setId(userId);
			newSummary.setYearmonth(Date.valueOf(firstDayOfMonth));
			newSummary.setWorkingdays(workingDays);
			newSummary.setWorkinghours(workingHours);
			newSummary.setActualworkinghours(actualWorkingHours);
			newSummary.setDeductioncount(deductionCount);
			newSummary.setWorktimeflag(false);
			newSummary.setCreateday(new Timestamp(System.currentTimeMillis()));
			newSummary.setCreateid(userId);
			// 新規登録処理を実行
			worktimeSummaryMapper.insert(newSummary);
			
			if (errorMessages.isEmpty()) {
				// 保存成功メッセージの追加
				errorMessages.add(messageSource.getMessage("save.successful", null, null));
			}
			
		}
		return errorMessages;
	}

}
