package com.example.demo.entity;

import java.sql.Date;
import java.sql.Timestamp;

import lombok.Data;

@Data
public class Information {
	// お知らせID
	public Integer informationid;
	// 社員番号
	public Integer id;
	// メッセージ内容
	public String message;
	// 開始日
	public Date startday;
	// 終了日
	public Date endday;
	// 登録日
	public Timestamp createday;
	// 更新日
	public Timestamp updateday;
}
