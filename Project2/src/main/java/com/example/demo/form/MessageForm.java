package com.example.demo.form;

import java.time.LocalDate;

import lombok.Data;

@Data
public class MessageForm {
	
	public String message;
	
	public LocalDate startDay;
	
	public LocalDate endDay;

}
