package com.example.demo.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Information;
import com.example.demo.entity.User;
import com.example.demo.form.MessageForm;
import com.example.demo.form.SearchForm;
import com.example.demo.repository.MessageMapper;

@Service
public class MessageRegisterService {

	@Autowired
	private MessageMapper messageMapper;

	public void saveMessage(MessageForm messageForm, Integer id) {

		// メッセージエンティティの作成
		Information messageEntity = new Information();
		messageEntity.setMessage(messageForm.message); // 送信されたメッセージ
		messageEntity.setStartday(Date.valueOf(messageForm.getStartDay())); // 開始日
		messageEntity.setEndday(Date.valueOf(messageForm.getEndDay())); // 終了日
		messageEntity.setId(0); // ユーザー情報
		messageEntity.setCreateday(new Timestamp(System.currentTimeMillis())); // 登録日（現在日時）

		// 最新の informationid を取得
		Integer informationid = messageMapper.findLatestInformationId();

		// 最新の informationid が null の場合、1 からスタート
		if (informationid == null) {
			messageEntity.setInformationid(1);
		} else {
			// 次の informationid を設定 (9999999999を超えないように制限)
			// Long型にする
			Long newInformationId = (long) informationid + 1;
			if (newInformationId > 9999999999L) {
				newInformationId = 1L;
			}
			// int 型に戻して設定
			messageEntity.setInformationid(newInformationId.intValue());
		}

		// メッセージをデータベースに保存
		messageMapper.save(messageEntity);
	}

	// あいまい検索
	public List<User> searchUsers(SearchForm searchForm) {
		return messageMapper.searchUsers(searchForm);
	}
	
	public void sendIndividualMessage(String individualMessage, List<Integer> userIdsToSend, LocalDate startDate,
			LocalDate endDate) {

		// メッセージをデータベースに保存
		for (Integer userId : userIdsToSend) {

			Information information = new Information();
			information.setMessage(individualMessage);
			information.setStartday(Date.valueOf(startDate));
			information.setEndday(Date.valueOf(endDate));
			information.setId(userId);
			information.setCreateday(new Timestamp(System.currentTimeMillis()));

			// データベースに保存
			messageMapper.saveIndividualMessage(information);
		}
	}
}
