package com.boostore.utility;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Component;

import com.boostore.domain.User;

@Component
public class MailContructor {
	
	@Autowired
	private Environment env;

	public SimpleMailMessage constructResetTokenMail(String contextPath, Locale locale, String token, User user, String password) {
		
		String url = contextPath+"/newAccount?token="+token;
		String message = "\n Please click on this link to verify your email and edit your personal information. Your password is: \n "+password;
		SimpleMailMessage email = new SimpleMailMessage();
		email.setTo(user.getEmail());
		email.setSubject("N's bookstore's new user");
		email.setText(url+message);
		email.setFrom(env.getProperty("support.mail"));
		return email;
	}
}
