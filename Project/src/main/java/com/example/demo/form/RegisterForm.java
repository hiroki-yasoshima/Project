package com.example.demo.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterForm {
	@Min(value = 10000, message = "{id.size}")
	@Max(value = 99999, message = "{id.size}")
	private Integer id;
	@Size(max = 30, message = "{name.size}")
	private String name;
	@Email(message = "{error.mail}")
	private String mail;
	@Email(message = "{error.reemail}")
	private String reemail;
	@Size(min = 8, max = 8, message = "{password.size}")
	@Pattern(regexp = "^[a-zA-Z0-9]+$", message = "{password.pattern}")
	private String password;
	private Integer admin;
}
