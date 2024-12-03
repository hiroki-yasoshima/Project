package com.example.demo.service;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.form.RegisterForm;
import com.example.demo.repository.UserManagementMapper;

@Service
public class UserManagementService {

	@Autowired
	private UserManagementMapper userManagementMapper;

	// 既存ユーザーがいるかの確認
	public boolean isUserAlreadyExists(Integer id, String mail) {
		// id と mail の両方でユーザーを検索
		User user = userManagementMapper.selectByIdAndMail(id, mail);
		// 既存ユーザーが見つかれば true を返す
		return user != null;
	}

	// ユーザー登録のロジック
	public boolean registerUser(RegisterForm form) {
		// 新しい User オブジェクトを生成
		User registerUser = new User();
		registerUser.setId(form.getId());
		registerUser.setName(form.getName());
		registerUser.setMail(form.getMail());
		registerUser.setPassword(form.getPassword());
		// form.getAdmin() が null の場合は 1 を、1 の場合は 2 を設定
		if (form.getAdmin() == null) {
			registerUser.setAdmin(1); // nullの場合は1を設定
		} else if (form.getAdmin() == 1) {
			registerUser.setAdmin(2); // form.getAdmin() が 1 の場合は 2 を設定
		}
		registerUser.setDeleteflag(0); // 削除されていないユーザー
		registerUser.setCreateday(new Timestamp(System.currentTimeMillis())); // 作成日時
		registerUser.setUpdateday(new Timestamp(System.currentTimeMillis())); // 更新日時

		try {
			// ユーザー情報をデータベースに挿入
			userManagementMapper.insertUser(registerUser);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// あいまい検索
	public List<User> searchUsers(Integer searchId, String searchName, String searchMail) {
		return userManagementMapper.searchUsers(searchId, searchName, searchMail);
	}

	// ユーザーを削除（deleteflagを更新）
	public boolean deleteUserById(Integer userId) {
		// 現在の日時を取得
		Timestamp currentTime = getCurrentTimestamp();

		// deleteflagを1に変更し、更新日時を設定する更新を実行
		int affectedRows = userManagementMapper.updateDeleteFlag(userId, 1, currentTime);

		// 影響を受けた行数が1以上なら削除成功、それ以外は失敗
		return affectedRows > 0;
	}

	// 現在の日時を取得するメソッド
	private Timestamp getCurrentTimestamp() {
		// 現在の時刻を取得し、Stringとして返す
		return new Timestamp(System.currentTimeMillis()); // 現在の日時を "YYYY-MM-DD HH:MM:SS" 形式で取得
	}
}
