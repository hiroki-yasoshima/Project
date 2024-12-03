package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserMapper;

@Service
public class LoginService {

	@Autowired
	private UserMapper userMapper;

	public User login(String email, String password) {
		// データベースからユーザーを取得
		return userMapper.selectByEmailAndPassword(email, password);
	}
}