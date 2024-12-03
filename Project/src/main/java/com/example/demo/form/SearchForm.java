package com.example.demo.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SearchForm {
	@Min(value = 10000, message = "{id.size}")
	@Max(value = 99999, message = "{id.size}")
	private Integer searchId;
	@Size(max = 30, message = "{name.size}")
	private String searchName;
	@Email(message = "{error.mail}")
	private String searchMail;

}
