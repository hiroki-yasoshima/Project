package com.example.demo.service;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HolidayService {

	private static final String HOLIDAY_API_URL = "https://holidays-jp.github.io/api/v1/date.json";

	// 祝日データを取得
	@SuppressWarnings("unchecked")
	public Map<String, String> getHolidays(int year) {
		// APIを呼び出し、結果をMap形式で取得
		RestTemplate restTemplate = new RestTemplate();
		String url = HOLIDAY_API_URL; // 固定URL
		return restTemplate.getForObject(url, Map.class);
	}
}
