package com.example.demo.entity;

import java.sql.Timestamp;

import org.springframework.data.annotation.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
	@Id
	// 社員番号
	public Integer id;
	// 社員名
	public String name;
	// メールアドレス
	public String mail;
	// パスワード
	public String password;
	// 権限
	public Integer admin;
	// 削除フラグ
	public Integer deleteflag;
	// 登録日
	public Timestamp createday;
	// 更新日
	public Timestamp updateday;
}
