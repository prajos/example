package com.loginext.web.driver.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.loginext.commons.multipart.config.ExtendedMultiPartResolver;
import com.loginext.commons.util.Util;

@EnableWebMvc
@EnableAutoConfiguration
@ComponentScan({ "com.loginext.web.login.controller" })
public class MvcConfig extends WebMvcConfigurerAdapter {

	@Value("${max.upload.filesize}")
	private String maxUploadSize;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
	}

	@Override
	public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Bean(name = "multipartResolver")
	public ExtendedMultiPartResolver getMultipartResolver() {
		ExtendedMultiPartResolver multipartResolver = new ExtendedMultiPartResolver();
		if (!Util.isNullOrEmpty(maxUploadSize)) {
			multipartResolver.setMaxUploadSize(Integer.parseInt(maxUploadSize));
		}
		return multipartResolver;
	}

}
