package com.example.demo.entity;

import java.sql.Date;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class Request {
	// 社員番号
	public Integer id;
	// 申請ID
	public Integer requestid;
	// 申請種別ID
	public Integer requesttypeid;
	// 有給開始日
	public Date paidstartday;
	// 有給終了日
	public Date paidendday;
	// 有給取得日数
	public Double paidcount;
	// 取得理由
	public String reason;
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
