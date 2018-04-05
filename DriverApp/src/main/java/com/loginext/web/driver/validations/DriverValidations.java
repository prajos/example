package com.loginext.web.driver.validations;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.loginext.commons.aspect.Log;
import com.loginext.commons.aspect.PropertyConfig;
import com.loginext.commons.util.Constants;
import com.loginext.commons.util.Util;
import com.loginext.web.driver.controller.DriverConstants;


@Component
public class DriverValidations implements DriverConstants, Constants{

	private static @Log Logger logger;
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private PropertyConfig propertyConfig;
	
	@Value("${driver.service}")
	private String DRIVER_MICROSERVICE_URL;

	@Value("${driver.exists}")
	private String DRIVER_EXISTS;
	
	@Value("${driver.exists.license}")
	private String DRIVER_LICENSE_EXISTS;
	
	@Value("${driver.exists.employeeid}")
	private String DRIVER_EMPLOYEEID_EXISTS;

	@SuppressWarnings("unchecked")
	public List<String> checkIfExistsPhoneNumber(List<String> phoneNumbers, Integer clientId,List<String> referenceIds) {
		List<String> duplicatePhoneNumbers=new ArrayList<>();
		try {
			logger.info("Start  checkIfExistsPhoneNumber call on: [{}]", new Date());
			if(phoneNumbers !=null && phoneNumbers.size() >0){

			StringBuilder REST_URL = new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_EXISTS).append("?clientId=").append(clientId).append("&phoneNumber=").append(phoneNumbers.toString().substring(1,phoneNumbers.toString().length()-1));
			if(referenceIds!=null){
				REST_URL.append("&referenceIds=").append(referenceIds.toString().substring(1,referenceIds.toString().length()-1));
			}
					
			logger.info("Executing RestTemplate call for DriverController.checkIfExistsPhoneNumber at url: [{}] on [{}]", REST_URL, new Date());
			 duplicatePhoneNumbers = restTemplate.getForObject(REST_URL.toString(), List.class);
			logger.info("End  checkIfExistsPhoneNumber call [{}] on: [{}]",duplicatePhoneNumbers.size(), new Date());

				return duplicatePhoneNumbers;
		  }

		} catch (RestClientException ex) {
			logger.error(Util.errorMessage, ex);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return duplicatePhoneNumbers;
	}
	@SuppressWarnings("unchecked")
	public List<String> checkIfExistsLicenseNumber(List<String> licenseNumber,Integer clientId,List<String> referenceIds) {
		List<String> existsLicenseNumbers=new ArrayList<>();
		try {
			logger.info("Start  checkIfExistsLicenseNumber call on: [{}]",new Date());
			if(licenseNumber !=null && licenseNumber.size() >0){
			StringBuilder REST_URL = new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_LICENSE_EXISTS).append(PATH_SEPARATOR).append(clientId).append("?licenseNumber=").append(licenseNumber.toString().substring(1,licenseNumber.toString().length()-1));
					
			if(referenceIds!=null){
				REST_URL.append("&referenceIds=").append(referenceIds.toString().substring(1,referenceIds.toString().length()-1));
			}
			logger.debug("Executing RestTemplate call for DriverController.checkIfExistsLicenseNumber at url: [{}] on [{}]", REST_URL, new Date());
			 existsLicenseNumbers =restTemplate.getForObject(REST_URL.toString(), List.class);
			logger.info("End  checkIfExistsLicenseNumber call [{}] on: [{}]", existsLicenseNumbers.size(), new Date());

			return existsLicenseNumbers;
			}
		} catch (RestClientException ex) {
			logger.error(Util.errorMessage, ex);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return existsLicenseNumbers;

	}
	
	@SuppressWarnings("unchecked")
	public List<String> checkIfExistsEmployeeId(List<String> employeeIds,Integer clientId,List<String> referenceIds) {
		List<String> duplicateEmpIds=new ArrayList<>();
		
		try {

			logger.info("Start  checkIfExistsEmployeeId call on: [{}]",new Date());
			if(employeeIds !=null && employeeIds.size() >0){
			StringBuilder REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_EMPLOYEEID_EXISTS).append(PATH_SEPARATOR).append(clientId).append("?employeeIds=").append(employeeIds.toString().substring(1,employeeIds.toString().length()-1));
					
			if(referenceIds!=null){
				REST_URL.append("&referenceIds=").append(referenceIds.toString().substring(1,referenceIds.toString().length()-1));
			}
			logger.info("Executing RestTemplate call for DriverController.checkIfExistsEmployeeId at url: [{}] on [{}]", REST_URL, new Date());
			duplicateEmpIds = restTemplate.getForObject(REST_URL.toString(), List.class);
			logger.info("End  checkIfExistsEmployeeId call on: [{}]",duplicateEmpIds.size(),new Date());
			}

		} catch (RestClientException ex) {
			logger.error(Util.errorMessage, ex);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return duplicateEmpIds;

	}
	
}
