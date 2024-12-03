package com.example.demo.service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Request;
import com.example.demo.entity.WorktimeSummary;
import com.example.demo.form.MessageForm;
import com.example.demo.repository.MessageMapper;
import com.example.demo.repository.RequestMapper;
import com.example.demo.repository.WorktimeSummaryMapper;

@Service
public class HomeService {

	@Autowired
	private RequestMapper requestMapper;

	@Autowired
	private MessageMapper messageMapper;

	@Autowired
	private WorktimeSummaryMapper worktimeSummaryMapper;

	// 全体お知らせを取得
	public List<MessageForm> getMessages() {
		// メッセージをDBから取得
		List<MessageForm> Allmessages = messageMapper.findMessages();

		return Allmessages;
	}

	// ユーザーIDを基に個別メッセージを取得
	public List<MessageForm> getMessagesForUser(Integer userId) {
		// メッセージをDBから取得
		List<MessageForm> messages = messageMapper.findMessagesByUserId(userId);

		return messages;
	}

	// 勤怠申請と有給休暇申請を統合して新しい年月順に並べる
	public List<String> getAllRequestMessages(Integer userId) {
		// 勤怠申請と有給休暇申請を取得
		List<String> attendanceRequest = getRequestMessage(userId);
		List<String> paidLeaveRequest = getPaidLeaveMessage(userId);

		// 両方のリストを1つに統合
		List<String> allRequests = new ArrayList<>();
		allRequests.addAll(attendanceRequest);
		allRequests.addAll(paidLeaveRequest);

		// 新しい年月順にソート
		Collections.sort(allRequests, (a, b) -> {
			// 年と月を抽出して比較
			String monthA = a.split("年")[0] + "年" + a.split("月")[0] + "月";
			String monthB = b.split("年")[0] + "年" + b.split("月")[0] + "月";
			return monthB.compareTo(monthA); // 降順
		});
		return allRequests;
	}

	// 勤怠申請を取得
	public List<String> getRequestMessage(Integer userId) {
		// 現在の年月から1年前の日付を取得し、日を1日に設定
		LocalDate oneYearAgoLocalDate = LocalDate.now().minusYears(1).withDayOfMonth(1);

		// LocalDateをDateに変換
		Date oneYearAgo = Date.valueOf(oneYearAgoLocalDate);

		// 申請フラグがtrueかつ1年以内のデータを取得する
		List<WorktimeSummary> yearMonth = worktimeSummaryMapper.getRequestMessages(userId, oneYearAgo);

		// 結果を格納するリスト
		List<String> messages = new ArrayList<>();

		if (yearMonth != null && !yearMonth.isEmpty()) {
			// ループして、すべての月に対してメッセージを作成
			for (WorktimeSummary summary : yearMonth) {
				Date requestDate = summary.getYearmonth();

				// 年月がnullでないことを確認
				if (requestDate != null) {
					// Date を LocalDate に変換
					LocalDate localDate = requestDate.toLocalDate();

					// 日付を「yyyy年MM月」の形式にフォーマット
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月");
					String formattedDate = localDate.format(formatter);

					// メッセージ作成
					String message = formattedDate + "勤怠申請完了";

					// メッセージリストに追加
					messages.add(message);
				}
			}
		}
		return messages;
	}

	// 有給休暇申請を取得
	public List<String> getPaidLeaveMessage(Integer userId) {
		// 現在の年月から1年前の日付を取得し、日を1日に設定
		LocalDate oneYearAgoLocalDate = LocalDate.now().minusYears(1).withDayOfMonth(1);

		// LocalDateをDateに変換
		Date oneYearAgo = Date.valueOf(oneYearAgoLocalDate);

		// 有給休暇申請フラグがtrueかつ1年以内のデータを取得する
		List<Request> paidLeaveList = requestMapper.getPaidLeaveRequests(userId, oneYearAgo);

		// 月ごとに集計するマップ
		Map<String, Integer> monthlyRequests = new HashMap<>();

		// 取得したデータを月ごとに集計
		if (paidLeaveList != null && !paidLeaveList.isEmpty()) {
			for (Request summary : paidLeaveList) {
				Date leaveStartDate = summary.getPaidstartday();

				// 有給休暇開始日がnullでないことを確認
				if (leaveStartDate != null) {
					// Date を LocalDate に変換
					LocalDate localDate = leaveStartDate.toLocalDate();

					// 日付を「yyyy年MM月」の形式にフォーマット
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月");
					String formattedDate = localDate.format(formatter);

					// 月ごとの件数をカウント
					monthlyRequests.put(formattedDate, monthlyRequests.getOrDefault(formattedDate, 0) + 1);
				}
			}
		}
		// メッセージのリストを作成
		List<String> messages = new ArrayList<>();
		for (Map.Entry<String, Integer> entry : monthlyRequests.entrySet()) {
			String month = entry.getKey();
			Integer count = entry.getValue();
			// メッセージ作成
			String message = month + "有給休暇" + count + "件申請完了";
			// メッセージリストに追加
			messages.add(message);
		}
		return messages;
	}

}
