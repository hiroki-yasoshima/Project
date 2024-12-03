package com.example.demo.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import lombok.Data;

@Data
public class Attendance {
	private LocalDate date; // 日付
	private String weekday; // 曜日
	private String holiday; // 祝日
	private String startTime; // 始業時間
	private String endTime; // 終業時間
	private String restTime; // 休憩時間
	private String deductionTime; // 遅刻早退時間
	private String remarks; // 備考

	// 引数付きコンストラクタ
	public Attendance(LocalDate date, String weekday, String holiday,
			String startTime, String endTime,
			String restTime, String deductionTime, String remarks) {
		this.date = date;
		this.weekday = weekday;
		this.holiday = holiday;
		this.startTime = startTime;
		this.endTime = endTime;
		this.restTime = restTime;
		this.deductionTime = deductionTime;
		this.remarks = remarks;
	}
	
	public String getFormattedDate() {
        // LocalDateを "d" 形式（例: 1, 2, 3,...）で表示する
        return date.format(DateTimeFormatter.ofPattern("d"));
    }

}
