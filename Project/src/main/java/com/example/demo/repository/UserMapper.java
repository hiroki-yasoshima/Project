package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.demo.entity.User;

@Mapper
public interface UserMapper {

	// メールアドレスとパスワードでユーザーを取得（deleteflagがoであること）
	@Select("SELECT id, name, admin FROM \"user\" WHERE mail = #{email} "
			+ "AND password = #{password} AND deleteflag = 0")
	User selectByEmailAndPassword( String email, String password);

	// すべてのユーザー情報を取得（deleteflagが0であること）
    @Select("SELECT * FROM \"user\" WHERE deleteflag = 0")
    List<User> selectAllUsers();
    
    // 社員番号でユーザーを取得
    @Select("SELECT id, name, mail FROM \"user\" WHERE id = #{id} AND deleteflag = 0")
	User selectById(Integer id);
}
