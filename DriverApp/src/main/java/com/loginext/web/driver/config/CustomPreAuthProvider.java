package com.loginext.web.driver.config;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;

@Component("preAuthProvider")
public class CustomPreAuthProvider extends PreAuthenticatedAuthenticationProvider {

	@Autowired
	private AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> userService;

	public CustomPreAuthProvider() {
		super();
	}

	@PostConstruct
	public void init() {
		super.setPreAuthenticatedUserDetailsService(userService);
	}
}
