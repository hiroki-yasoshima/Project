package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.demo.entity.Information;
import com.example.demo.entity.User;
import com.example.demo.form.MessageForm;
import com.example.demo.form.SearchForm;

@Mapper
public interface MessageMapper {

	// 条件に一致する全体お知らせを取得
	@Select("SELECT message FROM information WHERE id = 0 " +
			"AND CURRENT_DATE BETWEEN startday AND endday")
	List<MessageForm> findMessages();

	// 社員IDと条件に一致する個別メッセージを取得
	@Select("SELECT message FROM information WHERE id = #{userId} " +
			"AND CURRENT_DATE BETWEEN startday AND endday")
	List<MessageForm> findMessagesByUserId(Integer userId);

	// 最新の informationid を取得
	@Select("SELECT MAX(informationid) FROM information")
	Integer findLatestInformationId();

	// 全体メッセージの登録
	@Insert("INSERT INTO information (id, message, startday, endday, createday) " +
			"VALUES (#{id}, #{message}, #{startday}, #{endday}, #{createday})")
	void save(Information messageEntity);

	// あいまい検索
	@Select("<script>"
			+ "SELECT id, name FROM \"user\" "
			// 削除フラグが0出ないこと
			+ "WHERE deleteflag = 0 "
			// 社員番号が指定されていれば検索
			+ "<if test='searchId != null'>AND id = #{searchId}</if>"
			// 氏名が指定されていれば、部分一致で検索
			+ "<if test='searchName != null and searchName != \"\"'>AND name LIKE CONCAT('%', #{searchName}, '%')</if>"
			// すべて未入力の場合は検索を行わない
			+ "<if test='searchId == null and (searchName == null or searchName == \"\")'>AND 1=2</if>"
			+ "</script>")
	List<User> searchUsers(SearchForm searchForm);

	// 個別メッセージの登録
	@Insert("INSERT INTO information (id, message, startday, endday, createday) " +
			"VALUES (#{id}, #{message}, #{startday}, #{endday}, #{createday})")
	void saveIndividualMessage(Information information);

}
