package com.example.demo.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginForm {
	
    @Email(message = "{error.login.failed}") 
    @NotBlank(message = "{error.login.failed}") 
    @Size(min = 3, message = "{error.login.failed}" )
	private String email;
    
    @NotBlank(message = "{error.login.failed}") 
    @Size(min = 8, max = 8, message = "{error.login.failed}")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "{error.login.failed}")
	private String password;
	
//	public String getEmail() {
//		return email;
//	}
//	public void setEmail(String email) {
//		this.email = email;
//	}
//	public String getPassword() {
//		return password;
//	}
//	public void setPassword(String password) {
//		this.password = password;
//	}	
}
