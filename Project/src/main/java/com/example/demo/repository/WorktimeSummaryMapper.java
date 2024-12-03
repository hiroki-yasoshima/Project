package com.example.demo.repository;

import java.sql.Date;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.WorktimeSummary;

@Mapper
public interface WorktimeSummaryMapper {
	// 社員番号と年月を使用して申請フラグを取得
	@Select("SELECT worktimeflag FROM worktime_summary WHERE id = #{id} AND yearmonth = #{yearmonth}")
	Boolean getWorktimeFlagByIdAndYearmonth(Integer id, Date yearmonth);

	// 社員番号と年月を使用して有給取得日数を取得
	@Select("SELECT paidcount FROM worktime_summary WHERE id = #{userId} AND yearmonth = #{sqlDate}")
	Double getpaidCountByIdAndYearmonth(Integer userId, Date sqlDate);

	// 有給取得日数を登録
	@Insert("INSERT INTO worktime_summary (id, yearmonth, workingdays, workinghours, actualworkinghours, "
			+ "deductioncount, paidcount, createday, createid)"
			+ "VALUES (#{id}, #{yearmonth}, #{workingdays}, #{workinghours}, #{actualworkinghours}, "
			+ "#{deductioncount}, #{paidcount}, #{createday}, #{createid})")
	void insertPaidCount(WorktimeSummary newSummary);

	// 有給取得日数を更新
	@Update("UPDATE worktime_summary SET paidcount = #{summary.paidcount}, updateday =#{summary.updateday}, "
			+ "updateid = #{summary.updateid} WHERE id = #{userId} AND yearmonth = #{sqlDate}")
	void updatePaidCount(Integer userId, Date sqlDate, @Param("summary")WorktimeSummary summary);

	// 社員番号と年月を使用して WorktimeSummary を取得
	@Select("SELECT * FROM worktime_summary WHERE id = #{id} AND yearmonth = #{yearmonth}")
	WorktimeSummary getWorktimeSummaryByIdAndYearmonth(Integer id, Date yearmonth);

	// 年月を使用して WorktimeSummary を取得
	@Select("SELECT * FROM worktime_summary WHERE yearmonth = #{yearmonth}")
	WorktimeSummary getWorktimeSummaryByYearmonth(Date yearmonth);

	// 申請フラグがtrueかつ1年以内の日付のデータを取得する
	@Select("SELECT yearmonth FROM worktime_summary " +
			"WHERE id = #{userId} " +
			"AND worktimeflag = true " +
			"AND yearmonth >= #{oneYearAgo} " + // 1年前の日付以上
			"ORDER BY yearmonth DESC")
	List<WorktimeSummary> getRequestMessages(Integer userId, Date oneYearAgo);

	//勤怠情報の更新
	@Update("UPDATE worktime_summary SET workingdays = #{workingdays}, workinghours = #{workinghours}, "
			+ "actualworkinghours = #{actualworkinghours}, deductioncount = #{deductioncount}, "
			+ "worktimeflag = #{worktimeflag}, updateday =#{updateday}, updateid = #{updateid}"
			+ " WHERE id = #{id} AND yearmonth = #{yearmonth}")
	void update(WorktimeSummary summary);

	// 勤怠情報の登録
	@Insert("INSERT INTO worktime_summary (id, yearmonth, workingdays, workinghours, actualworkinghours, "
			+ "deductioncount, worktimeflag, createday, createid) VALUES (#{id}, #{yearmonth}, "
			+ "#{workingdays}, #{workinghours}, #{actualworkinghours}, #{deductioncount}, #{worktimeflag}, "
			+ "#{createday}, #{createid})")
	void insert(WorktimeSummary newSummary);

	@Update("UPDATE worktime_summary SET workingdays = #{workingdays}, workinghours = #{workinghours}, "
			+ "actualworkinghours = #{actualworkinghours}, deductioncount = #{deductioncount}, "
			+ "recognitionflag = #{recognitionflag}, updateday =#{updateday}, updateid = #{updateid}"
			+ " WHERE id = #{id} AND yearmonth = #{yearmonth}")
	void updateApproval(WorktimeSummary summary);

	@Insert("INSERT INTO worktime_summary (id, yearmonth, workingdays, workinghours, actualworkinghours, "
			+ "deductioncount, recognitionflag, createday, createid) VALUES (#{id}, #{yearmonth}, "
			+ "#{workingdays}, #{workinghours}, #{actualworkinghours}, #{deductioncount}, #{recognitionflag}, "
			+ "#{createday}, #{createid})")
	void insertApproval(WorktimeSummary newSummary);

	@Update("UPDATE worktime_summary SET recognitionflag = #{recognitionflag}, updateday =#{updateday}, updateid = #{updateid}"
			+ " WHERE id = #{id} AND yearmonth = #{yearmonth}")
	void updateApproval2(WorktimeSummary summary);

	@Insert("INSERT INTO worktime_summary (id, yearmonth, recognitionflag, createday, createid) VALUES (#{id}, #{yearmonth}, "
			+ "#{recognitionflag}, #{createday}, #{createid})")
	void insertApproval2(WorktimeSummary newSummary);

}
