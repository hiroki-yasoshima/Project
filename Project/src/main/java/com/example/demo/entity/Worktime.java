package com.example.demo.entity;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class Worktime {
	@Id
	// 社員番号
	public Integer id;
	// 年月日
	public Date yearmonthday;
	// 始業時間
	public Time starttime;
	// 終業時間
	public Time endtime;
	// 休憩時間
	public Integer resttime;
	// 遅刻早退時間
	public Integer deductiontime;
	// 備考
	public String remarks;
	// 登録日
	public Timestamp createday;
	// 登録者社員番号
	public Integer createid;
	// 更新日
	public Timestamp updateday;
	// 更新者社員番号
	public Integer updateid;

}
