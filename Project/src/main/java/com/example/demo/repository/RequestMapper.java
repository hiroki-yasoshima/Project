package com.example.demo.repository;

import java.sql.Date;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.demo.entity.Request;

@Mapper
public interface RequestMapper {

	// 最新の requesttypeid を取得
	@Select("SELECT MAX(requesttypeid) FROM request")
	Integer findLatestRequestTypeId();

	// ユーザーIDと日付に基づいて申請データをリストで取得
	@Select("SELECT id, paidstartday, paidendday, paidcount, reason, recognitionflag "
			+ "FROM request WHERE id = #{userId} AND paidstartday = #{date}")
	List<Request> selectByIdAndDate(Integer userId, Date date);

	// ユーザーIDと日付に基づいて申請データをリストで取得
	@Select("SELECT id, paidstartday, paidendday, paidcount, reason, recognitionflag, createday "
			+ "FROM request WHERE id = #{id} AND paidstartday = #{sqlDate}")
	List<Request> selectByIdAndsplDate(Integer id, Date sqlDate);

	// 1年以内の日付のデータを取得する
	@Select("SELECT paidstartday FROM request " +
			"WHERE id = #{userId} " +
			"AND paidstartday >= #{oneYearAgo} " + // 1年前の日付以上
			"ORDER BY paidstartday DESC")
	List<Request> getPaidLeaveRequests(Integer userId, Date oneYearAgo);

	@Insert("INSERT INTO request (id, requestid, requesttypeid, paidstartday, paidendday,"
			+ "paidcount, reason, recognitionflag, createday, createid) "
			+
			"VALUES (#{id}, #{requestid}, #{requesttypeid}, #{paidstartday}, #{paidendday},"
			+ "#{paidcount}, #{reason}, #{recognitionflag}, #{createday}, #{createid})")
	void insert(Request newRequest);

	@Update("UPDATE request SET recognitionflag = true, updateday = #{newRequest.updateday}, updateid = #{newRequest.updateid}"
			+ " WHERE id = #{id} AND paidstartday = #{sqldate}")
	void updateRequest(Integer id, Date sqldate, Request newRequest);

}
