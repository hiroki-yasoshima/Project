package com.example.demo.repository;

import java.sql.Timestamp;
import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.demo.entity.User;

@Mapper
public interface UserManagementMapper {
	// 社員番号とメールアドレスでユーザーを取得（deleteflagが0であること）
	@Select("SELECT id, mail FROM \"user\" WHERE id = #{id} "
			+ "AND mail = #{mail} AND deleteflag = 0")
	User selectByIdAndMail(@Param("id") Integer id, @Param("mail") String mail);

	// ユーザーを登録するメソッド
	@Insert("INSERT INTO \"user\" (id, name, mail, password, admin, deleteflag, createday, updateday) "
			+ "VALUES (#{id}, #{name}, #{mail}, #{password}, #{admin}, #{deleteflag}, #{createday}, #{updateday})")
	void insertUser(User user);

	@Select("<script>"
			+ "SELECT id, name, mail FROM \"user\" "
			+ "WHERE deleteflag = 0 "
			// 社員番号が指定されていれば検索
			+ "<if test='id != null'>AND id = #{id}</if>"
			// 氏名が指定されていれば、部分一致で検索
			+ "<if test='name != null and name != \"\"'>AND name LIKE CONCAT('%', #{name}, '%')</if>"
			// メールアドレスが指定されていれば、部分一致で検索
			+ "<if test='mail != null and mail != \"\"'>AND mail LIKE CONCAT('%', #{mail}, '%')</if>"
			// すべて未入力の場合は検索を行わない
			+ "<if test='id == null and (name == null or name == \"\") and (mail == null or mail == \"\")'>AND 1=2</if>"
			+ "</script>")
	List<User> searchUsers(@Param("id") Integer id, @Param("name") String name, @Param("mail") String mail);

	// deleteflagと更新日時を更新するSQL
	@Update("UPDATE \"user\" SET deleteflag = #{deleteFlag}, updateday = #{updateday} WHERE id = #{id}")
	int updateDeleteFlag(@Param("id") Integer id,@Param("deleteFlag") Integer deleteFlag,@Param("updateday") Timestamp updateday);
}
