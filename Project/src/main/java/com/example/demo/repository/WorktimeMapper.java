package com.example.demo.repository;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Worktime;

@Mapper
public interface WorktimeMapper {

	// ユーザーIDと日付に基づいて勤怠データをリストで取得
	@Select("SELECT id, yearmonthday, starttime, endtime, resttime, deductiontime, remarks, "
			+ "createday, createid, updateday, updateid "
			+ "FROM worktime WHERE yearmonthday = #{date} AND id = #{userId}")
	List<Worktime> selectAll(LocalDate date, Integer userId);

	// 既存の勤怠データを検索
	@Select("SELECT * FROM worktime WHERE id = #{id} AND yearmonthday = #{yearmonthday}")
	Worktime findByIdAndDate(@Param("id") Integer id, @Param("yearmonthday") Date yearmonthday);

	@Insert("INSERT INTO worktime (id, yearmonthday, starttime, endtime, resttime, deductiontime, remarks, createday, createid) "
			+
			"VALUES (#{id}, #{yearmonthday}, #{starttime}, #{endtime}, #{resttime}, #{deductiontime}, #{remarks}, #{createday}, #{createid})")
	void insert(Worktime worktime);

	@Update("UPDATE worktime " +
			"SET starttime = #{starttime}, endtime = #{endtime}, resttime = #{resttime}, " +
			"deductiontime = #{deductiontime}, remarks = #{remarks}, updateday = #{updateday}, updateid = #{updateid} "
			+
			"WHERE id = #{id} AND yearmonthday = #{yearmonthday}")
	void update(Worktime worktime);
}
