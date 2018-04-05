package com.loginext.web.driver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

import com.loginext.commons.aspect.PropertyConfig;
import com.loginext.commons.security.AuthenticationEntryPointDenied;
import com.loginext.commons.security.UserSessionAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private AuthenticationEntryPointDenied authenticationEntryPoint;

	@Autowired
	private UserSessionAuthenticationFilter usaf;

	@Autowired
	private AuthenticationProvider authenticationProvider;

	@Autowired
	@Qualifier("preAuthProvider")
	private AuthenticationProvider preAuthProvider;
	
	@Autowired
	private PropertyConfig propertyConfig;
	
	@Value("${actuator.user}")
	private String actuatorUserName;
	
	@Value("${actuator.password}")
	private String actuatorPassword;

	@Bean
	@Override
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}

	@Override
	public void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.authenticationProvider(authenticationProvider).authenticationProvider(preAuthProvider);
		auth.inMemoryAuthentication()
	      .withUser(actuatorUserName).password(actuatorPassword).roles("ACTUATOR");
	}

	protected void configure(HttpSecurity http) throws Exception {
		http.csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.exceptionHandling().authenticationEntryPoint(authenticationEntryPoint).and().addFilter(usaf)
				.authorizeRequests().antMatchers("/driver/**").hasRole("USER")
		        .antMatchers("/haul/v1/driver/**").hasRole("USER");
		http.authorizeRequests().antMatchers("/loginextActuator/*").hasRole("ACTUATOR").and().httpBasic();
	}

}
