package com.example.demo.entity;

import java.sql.Date;
import java.sql.Timestamp;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class WorktimeSummary {
	@Id
	// 社員番号
	public Integer id;
	// 年月
	public Date yearmonth;
	// 出勤日数
	public Integer workingdays;
	// 勤務時間
	public Integer workinghours;
	// 実労働時間
	public Integer actualworkinghours;
	// 遅刻早退回数
	public Integer deductioncount;
	// 有給取得日数
	private Double paidcount;
	// 申請フラグ
	public Boolean worktimeflag;
	// 承認フラグ
	public Boolean recognitionflag;
	// 登録日
	public Timestamp createday;
	// 登録者社員番号
	public Integer createid;
	// 更新日
	public Timestamp updateday;
	// 更新者社員番号
	public Integer updateid;
	

}
