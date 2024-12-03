package com.example.demo.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.WorktimeSummary;
import com.example.demo.repository.WorktimeSummaryMapper;

@Service
public class AttendanceHistoryService {

	@Autowired
	private WorktimeSummaryMapper worktimeSummaryMapper;

	public List<WorktimeSummary> getHistory(int year, Integer userId) {
		List<WorktimeSummary> history = new ArrayList<>();

		// 1月から12月まで、全ての月についてデータを取得
		for (int month = 1; month <= 12; month++) {

			// 年と月を組み合わせたDateを作成
			// 月が１桁の場合は先頭に0を付ける
			String yearMonthStr = year + "-" + (month < 10 ? "0" + month : month);
			Date yearMonth = Date.valueOf(yearMonthStr + "-01"); // 日付は月の1日で固定

			// DBからデータを取得
			WorktimeSummary summary = worktimeSummaryMapper.getWorktimeSummaryByIdAndYearmonth(userId, yearMonth);

			if (summary == null) {
				// データが無い場合は、デフォルトの値を設定
				summary = new WorktimeSummary();
				summary.setId(userId);
				summary.setYearmonth(yearMonth);
			} else {
				// 分単位の勤務時間を時間単位に変換
				summary.setWorkinghours(summary.getWorkinghours() / 60);
			}
			// リストに追加
			history.add(summary);
		}

		return history;
	}

}
