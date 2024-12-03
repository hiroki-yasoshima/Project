package com.example.demo.service;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Request;
import com.example.demo.entity.WorktimeSummary;
import com.example.demo.repository.RequestMapper;
import com.example.demo.repository.WorktimeSummaryMapper;

@Service
public class RequestService {

	@Autowired
	private RequestMapper requestMapper;

	@Autowired
	private WorktimeSummaryMapper worktimeSummaryMapper;

	public List<Request> getRequest(int currentYear, int currentMonth, Integer userId) {
		List<Request> requestList = new ArrayList<>();

		// 指定された年と月の日数を取得
		LocalDate firstDayOfMonth = LocalDate.of(currentYear, currentMonth, 1);
		int daysInMonth = firstDayOfMonth.lengthOfMonth();

		// 月の各日について申請件数を生成
		for (int i = 1; i <= daysInMonth; i++) {
			LocalDate currentDate = LocalDate.of(currentYear, currentMonth, i);
			// LocalDate -> java.sql.Date への変換
			Date sqlDate = Date.valueOf(currentDate);

			// requestMapperを使用して、userIdと一致する日付の申請情報を取得
			List<Request> requestsForDay = requestMapper.selectByIdAndDate(userId, sqlDate);

			// 取得した申請情報をリストに追加
			requestList.addAll(requestsForDay);

		}
		return requestList;
	}

	// 申請データを保存するメソッド
	public void saveRequest(Integer userId, int currentYear, int currentMonth, Request newRequest) {

		// 最新の requesttypeid を取得
		Integer latestRequestTypeId = requestMapper.findLatestRequestTypeId();

		// 最新の requesttypeid が null の場合、100からスタート
		if (latestRequestTypeId == null) {
			newRequest.setRequestid(100);
			newRequest.setRequesttypeid(100);
		} else {
			// 次の requesttypeid を設定 (999を超えないように制限)
			int newRequestTypeId = latestRequestTypeId + 1;
			if (newRequestTypeId > 999) {
				newRequestTypeId = 100;
			}
			newRequest.setRequestid(newRequestTypeId);
			newRequest.setRequesttypeid(newRequestTypeId);
		}

		// 年月の1日を取得
		YearMonth currentYearMonth = YearMonth.of(currentYear, currentMonth);
		LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
		Date sqlDate = Date.valueOf(firstDayOfMonth);

		// 既存の有給取得日数を取得
		Double workPaidCount = worktimeSummaryMapper.getpaidCountByIdAndYearmonth(userId, sqlDate);
		if (workPaidCount == null) {
			workPaidCount = 0.0; // 存在しない場合は0日として扱う
		}
		// newRequestの有給取得日数を取得
		Double newPaidCount = newRequest.getPaidcount();
		if (newPaidCount == null) {
			newPaidCount = 0.0; // newRequestの有給取得日数がnullの場合は0日
		}

		// 合計有給取得日数を計算
		Double totalPaidCount = workPaidCount + newPaidCount;

		// DBからデータを取得
		WorktimeSummary summary = worktimeSummaryMapper.getWorktimeSummaryByIdAndYearmonth(userId, sqlDate);

		if (summary == null) {
			// WorktimeSummaryを新規作成
			WorktimeSummary newSummary = new WorktimeSummary();
			newSummary.setId(userId);
			newSummary.setYearmonth(sqlDate);
			newSummary.setPaidcount(totalPaidCount);
			newSummary.setCreateid(userId);
			newSummary.setCreateday(new Timestamp(System.currentTimeMillis()));
			newSummary.setWorkingdays(0);
			newSummary.setWorkinghours(0);
			newSummary.setActualworkinghours(0);
			newSummary.setDeductioncount(0);
			worktimeSummaryMapper.insertPaidCount(newSummary);
		} else {
			// 新しい有給取得日数をWorktimeSummaryテーブルに更新
			summary.setUpdateid(userId);
			summary.setUpdateday(new Timestamp(System.currentTimeMillis()));
			summary.setPaidcount(totalPaidCount);
			worktimeSummaryMapper.updatePaidCount(userId, sqlDate, summary);
		}
		requestMapper.insert(newRequest);
	}

}
