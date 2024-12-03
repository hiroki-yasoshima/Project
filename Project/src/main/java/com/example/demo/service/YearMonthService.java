package com.example.demo.service;

import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;

@Service
public class YearMonthService {
	
	// 現在の年を取得
	public int getCurrentYear() {
		return YearMonth.now().getYear();
	}
	// 現在の月を取得
	public int getCurrentMonth() {
		return YearMonth.now().getMonthValue();
	}

	// 年リスト（今年を基準に現在-5年、現在+1年）
	public List<Integer> getYearsList() {
		int currentYear = getCurrentYear();
		return IntStream.range(currentYear - 5, currentYear + 2)
				.boxed()
				.collect(Collectors.toList());
	}
	// 月のリスト（1から12まで）
	public List<Integer> getMonthsList() {
		return IntStream.rangeClosed(1, 12)
				.boxed()
				.collect(Collectors.toList());
	}
}
