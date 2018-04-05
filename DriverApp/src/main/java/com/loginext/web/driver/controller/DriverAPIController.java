package com.loginext.web.driver.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.loginext.commons.aspect.CurrentSession;
import com.loginext.commons.aspect.Log;
import com.loginext.commons.aspect.PropertyConfig;
import com.loginext.commons.entity.Session;
import com.loginext.commons.enums.ResponseMessage;
import com.loginext.commons.model.AddressDTO;
import com.loginext.commons.model.DriverCreateDTO;
import com.loginext.commons.model.DriverDTO;
import com.loginext.commons.model.Error;
import com.loginext.commons.model.LocaleLookupDTO;
import com.loginext.commons.model.Response;
import com.loginext.commons.util.APIAuditService;
import com.loginext.commons.util.Constants;
import com.loginext.commons.util.RedisCacheUtilServiceImpl;
import com.loginext.commons.util.ResponseMessageConstants;
import com.loginext.commons.util.ResponseMessageUtil;
import com.loginext.commons.util.Slf4jUtility;
import com.loginext.commons.util.Util;
import com.loginext.web.driver.validations.DriverValidations;

import scala.actors.threadpool.Arrays;

@RequestMapping("/haul/v1/driver")
@Controller
public class DriverAPIController implements DriverConstants, Constants {

	private static @Log Logger logger;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private APIAuditService apiAuditService;

	@Autowired
	private PropertyConfig propertyConfig;

	@Value("${driver.service}")
	private String DRIVER_MICROSERVICE_URL;

	@Value("${driver.create}")
	private String DRIVER_CREATE;

	@Value("${driver.update}")
	private String DRIVER_UPDATE;

	@Value("${driver.update.attendance}")
	private String DRIVER_UPDATE_ACTIVE;

	@Value("${driver.exists}")
	private String DRIVER_EXISTS;

	@Value("${driver.getbyreferenceids}")
	private String DRIVER_GET_BY_REFERENCEIDS;

	@Value("${driver.delete}")
	private String DRIVER_DELETE;

	@Value("${driver.get.shifts}")
	private String DRIVER_GET_SHIFTS;

	@Value("${driver.exists.license}")
	private String DRIVER_LICENSE_EXISTS;

	@Value("${driver.exists.employeeid}")
	private String DRIVER_EMPLOYEEID_EXISTS;

	@Value("${driver.update.get}")
	private String DRIVER_UPDATE_GET;

	@Value("${lookup.service}")
	private String LOOKUP_MICROSERVICE_URL;

	@Value("${lookup.locale}")
	private String GET_LOCALEID;

	@Autowired
	private DriverValidations driverValidations;

	@Value("${driver.getdriverids}")
	private String GET_DRIVERIDS;
	
	@Value("${validation.service}")
	private String VALIDATION_MICROSERVICE_URL;
	
	@Value("${validation.driver.errors}")
	private String DRIVER_VALIDATION;

	@SuppressWarnings("rawtypes")
	@Autowired
	private RedisCacheUtilServiceImpl redisCache;


	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response<Object>> create(HttpServletRequest request, @CurrentSession Session session,
			@RequestBody List<DriverDTO> drivers) {

		logger.info("Start api get call token [{}] at: [{}]", Slf4jUtility.toObjArr(session.getToken(),new Date()));
		Boolean isPhoneNumberAlreadyExist = false;
		Response<Object> response = new Response<>();
		Boolean isValidationFailed = false;
		String errorString = "error_";
		try {
			if(!CollectionUtils.isEmpty(drivers)){
			// API Rate Limit check
			response = apiAuditService.validateRateLimitingAndAPIlogging(request, session);
			if (500 == response.getStatus() || 429 == response.getStatus()) {
				return new ResponseEntity<>(response, HttpStatus.OK);
			}
			//Perform basic client validations
			List<String> licenseNos=new ArrayList<>();
			List<String> empIds=new ArrayList<>();
			List<AddressDTO> addresses = new ArrayList<AddressDTO>();
			for(DriverDTO driver:drivers){
				if(null!=session && null!=session.getCountryCode()){
					driver.setCountryCode(session.getCountryCode());
				}
				if(null ==driver.getAddressList()){
					driver.setAddressList(new ArrayList<AddressDTO>());
				}
				if(StringUtils.isNotBlank(driver.getLicenseNumber())){
					licenseNos.add(driver.getLicenseNumber());
				}
				if(StringUtils.isNotBlank(driver.getDriverEmployeeId())){
					empIds.add(driver.getDriverEmployeeId());
				}
				if(null !=driver.getAddressList() && !driver.getAddressList().isEmpty()){
					addresses.addAll(driver.getAddressList());
				}
			}
			Response<Object> validationResponse=this.checkBasicValidations(drivers,session,CREATECHECK);
			if(validationResponse !=null){
				return new ResponseEntity<>(validationResponse, HttpStatus.OK);
			}
			
			int count = 0;
			HashMap<String, List<Error>> errorMessages = new HashMap<>();
			// commented code because duplicate number are allowed for driver
//			List<String> phoneNos = drivers.stream().filter(d -> d.getPhoneNumber() != null)
//					.map(d -> d.getPhoneNumber()).collect(Collectors.toList());
			// Validations

			// Duplicate phone number check
//			List<String> duplicatePhoneNos = driverValidations.checkIfExistsPhoneNumber(phoneNos,
//					session.getClientId().intValue(), null);
//
//			if (!duplicatePhoneNos.isEmpty()) {
//				isPhoneNumberAlreadyExist = true;
//				List<Error> errors = new ArrayList<Error>();
//				Error error = new Error();
//				error.setKey("PhoneNumber");
//				List<String> msgs = new ArrayList<String>();
//				msgs.add(ResponseMessage.PHONENUMBER_EXISTS.fromValue());
//				error.setMessage(msgs);
//				errors.add(error);
//				errorMessages.put(errorString + count, errors);
//				count++;
//				isValidationFailed = true;
//			}

			// License Number check
			List<String> duplicateLicenseNos = driverValidations.checkIfExistsLicenseNumber(licenseNos,
					session.getClientId().intValue(), null);

			if (!duplicateLicenseNos.isEmpty()) {
				List<Error> errors = new ArrayList<Error>();
				Error error = new Error();
				error.setKey("LicenseNumber");
				List<String> msgs = new ArrayList<String>();
				msgs.add(ResponseMessage.LICENSENUMBER_EXISTS.fromValue());
				error.setMessage(msgs);
				errors.add(error);
				errorMessages.put(errorString + count, errors);
				count++;
				isValidationFailed = true;
			}

			// DriverId check

			List<String> duplicateEmpIds = driverValidations.checkIfExistsEmployeeId(empIds,session.getClientId().intValue(), null);

			if (!duplicateEmpIds.isEmpty()) {
				List<Error> errors = new ArrayList<Error>();
				Error error = new Error();
				error.setKey("DriverEmployeeId");
				List<String> msgs = new ArrayList<String>();
				msgs.add(ResponseMessage.DRIVEREMPLOYEEID_EXISTS.fromValue());
				error.setMessage(msgs);
				errors.add(error);
				errorMessages.put(errorString + count, errors);
				count++;
				isValidationFailed = true;
			}

			if (isValidationFailed) {
				response.setHasError(TRUE);
				response.setMessage(ResponseMessage.FAIL.fromValue());
				response.setStatus(409);
				response.setError(errorMessages);
			} else {
	
				// Get Country and StateIds
				List<String> countryShortCodes = addresses.stream().filter(d -> d.getCountryShortCode() != null)
						.map(d -> d.getCountryShortCode()).collect(Collectors.toList());
				String GET_COUNTRY_IDS_URL = new StringBuilder().append(propertyConfig.getUrl())
						.append(LOOKUP_MICROSERVICE_URL).append(GET_LOCALEID).append("?type=")
						.append(Constants.LOCALE_COUNTRY).append("&shortcodes=")
						.append(countryShortCodes.toString().substring(1, countryShortCodes.toString().length() - 1))
						.toString();
				LocaleLookupDTO[] country_locale_arr = restTemplate.getForObject(GET_COUNTRY_IDS_URL,
						LocaleLookupDTO[].class);
				List<LocaleLookupDTO> country_locale = Arrays.asList(country_locale_arr);

				if (country_locale.isEmpty() || country_locale.size() != countryShortCodes.size()) {
					List<Error> errors = new ArrayList<Error>();
					Error error = new Error();
					error.setKey("Country Short Code");
					List<String> msgs = new ArrayList<String>();
					msgs.add("Invalid country code specified");
					error.setMessage(msgs);
					errors.add(error);
					errorMessages.put(errorString + count, errors);
					count++;
					isValidationFailed = true;
				}

				List<String> stateShortCodes = addresses.stream().filter(d -> d.getStateShortCode() != null)
						.map(d -> d.getStateShortCode()).collect(Collectors.toList());
				String GET_STATE_IDS_URL = new StringBuilder().append(propertyConfig.getUrl())
						.append(LOOKUP_MICROSERVICE_URL).append(GET_LOCALEID).append("?type=")
						.append(Constants.LOCALE_STATE).append("&shortcodes=")
						.append(stateShortCodes.toString().substring(1, stateShortCodes.toString().length() - 1))
						.toString();
				LocaleLookupDTO[] state_locale_arr = restTemplate.getForObject(GET_STATE_IDS_URL,
						LocaleLookupDTO[].class);
				List<LocaleLookupDTO> state_locale = Arrays.asList(state_locale_arr);
				stateShortCodes = new ArrayList(new HashSet(stateShortCodes));
				Set<String> distinctStates = new HashSet<String>();
				for(LocaleLookupDTO stateLocaleLookup : state_locale){
					distinctStates.add(stateLocaleLookup.getCode());
				}

				if (/*state_locale.isEmpty() || state_locale.size()*/distinctStates.size() != stateShortCodes.size()) {
					List<Error> errors = new ArrayList<Error>();
					Error error = new Error();
					error.setKey("State Short Code");
					List<String> msgs = new ArrayList<String>();
					msgs.add("Invalid state code specified");
					error.setMessage(msgs);
					errors.add(error);
					errorMessages.put(errorString + count, errors);
					count++;
					isValidationFailed = true;
				}

				if (isValidationFailed) {
					response.setHasError(TRUE);
					response.setMessage(ResponseMessage.FAIL.fromValue());
					response.setStatus(409);
					response.setError(errorMessages);
				} else {

					// Preparing Map
					HashMap<String, Integer> stateCodeIdMap = new HashMap<String, Integer>();
					for (LocaleLookupDTO localeLookupDTO : state_locale) {
						stateCodeIdMap.put(localeLookupDTO.getCode() + "-" + localeLookupDTO.getCountryCode(), localeLookupDTO.getId());
					}
					HashMap<String, Integer> countryCodeIdMap = new HashMap<String, Integer>();
					for (LocaleLookupDTO localeLookupDTO : country_locale) {
						countryCodeIdMap.put(localeLookupDTO.getCode(), localeLookupDTO.getId());
					}

					for (DriverDTO driverDTO : drivers) {
						String uuid = driverDTO.getGUID();
						if (StringUtils.isBlank(uuid)) {
							uuid = UUID.randomUUID().toString().replaceAll("-", "");
						}
						driverDTO.setGUID(uuid);
						driverDTO.setClientBranchId(session.getCurrentClientBranchId().intValue());
						driverDTO.setClientId(session.getClientId().intValue());
						driverDTO.setCreatedByUserId(session.getUserId().intValue());
						for (AddressDTO address : driverDTO.getAddressList()) {
							address.setGuid(uuid);
							if (countryCodeIdMap.get(address.getCountryShortCode()) != null)
								address.setCountry(countryCodeIdMap.get(address.getCountryShortCode()).toString());
							if (stateCodeIdMap
									.get(address.getStateShortCode() + "-" + address.getCountryShortCode()) != null)
								address.setState(stateCodeIdMap
										.get(address.getStateShortCode() + "-" + address.getCountryShortCode())
										.toString());
						}
					}

					String REST_URL = new StringBuilder().append(propertyConfig.getUrl())
							.append(DRIVER_MICROSERVICE_URL).append(DRIVER_CREATE).toString();
					URI uri = Util.buildUri(session, REST_URL);
					logger.info("Executing RestTemplate for DriverAPIController", Slf4jUtility.toObjArr(uri));
					DriverCreateDTO[] createdDrivers=restTemplate.postForObject(uri, drivers, DriverCreateDTO[].class);
					
					if(createdDrivers.length>0){
						List<String> referenceIdsList = new ArrayList<String>();
						for(DriverCreateDTO d:createdDrivers){
							referenceIdsList.add(d.getReferenceId());
						}
						if(isPhoneNumberAlreadyExist){
							response.setMessage(ResponseMessage.SUCCESS_PHONE_NO_EXIST_NOTIFY.fromValue());
						}else{
							response.setMessage(ResponseMessage.SUCCESS.fromValue());
						}
						response.setStatus(201);
						response.setData(referenceIdsList);
						return new ResponseEntity<Response<Object>>(response, HttpStatus.CREATED);
					}

				}
			}
			return new ResponseEntity<>(response, HttpStatus.OK);
			}else{
				response.setMessage("List should not be empty");
				response.setHasError(true);
				response.setStatus(400);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/update", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response<Object>> update(@CurrentSession Session session, HttpServletRequest request,
			@RequestBody List<DriverDTO> drivers) {

		logger.info("Start api update call at: [{}]", Slf4jUtility.toObjArr(new Date()));
		Boolean isPhoneNumberExists  = false;
		Response<Object> response = new Response<Object>();
		Boolean isValidationFailed = false;
		String errorString = "error_";

		try {
			// API Rate Limit check

			if(!CollectionUtils.isEmpty(drivers)){
			response = apiAuditService.validateRateLimitingAndAPIlogging(request, session);
			if (500 == response.getStatus() || 429 == response.getStatus()) {
				return new ResponseEntity<Response<Object>>(response, HttpStatus.OK);
			}
			/*
			 * String limit =
			 * redisCache.checkIfRateLimitReached(session.getClientId()); if
			 * (APIConstants.MAX_LIMIT_REACHED.equals(limit)) { logger.debug(
			 * "API Max Rate limit reached for clientId:[{}] at:[{}]",
			 * session.getClientId(), new Date()); response.setStatus(429);
			 * response.setMessage(ResponseMessage.MAX_LIMIT_REACHED.fromValue()
			 * ); return new ResponseEntity<Response<Object>>(response,
			 * HttpStatus.OK); }
			 */
			HashMap<String, List<Error>> errorMessages = new HashMap<String, List<Error>>();
			int count = 0;
			List<String> licenseNos=new ArrayList<>();
			List<String> empIds=new ArrayList<>();
			List<String> referenceIds=new ArrayList<>();
			List<AddressDTO> addresses = new ArrayList<AddressDTO>();
			for(DriverDTO driver:drivers){
				if(null!=session && null!=session.getCountryCode()){
					driver.setCountryCode(session.getCountryCode());
				}
				if(null ==driver.getAddressList()){
					driver.setAddressList(new ArrayList<AddressDTO>());
				}
				if(StringUtils.isNotBlank(driver.getLicenseNumber())){
					licenseNos.add(driver.getLicenseNumber());
				}
				if(StringUtils.isNotBlank(driver.getLicenseNumber())){
					licenseNos.add(driver.getLicenseNumber());
				}
				if(StringUtils.isNotBlank(driver.getReferenceId())){
					referenceIds.add(driver.getReferenceId());
				}
				if(null !=driver.getAddressList() && !driver.getAddressList().isEmpty()){
					addresses.addAll(driver.getAddressList());
				}
			}
			logger.info("update driver refernceid [{}] for token [{}]",referenceIds,session.getToken());
			// Missing Reference Id check
			if ((referenceIds != null && referenceIds.size() != drivers.size())) {
				List<Error> errors = new ArrayList<Error>();
				Error error = new Error();
				error.setKey("ReferenceId");
				List<String> msgs = new ArrayList<String>();
				msgs.add("Missing ReferenceId");
				error.setMessage(msgs);
				errors.add(error);
				errorMessages.put(errorString + count, errors);
				count++;
				isValidationFailed = true;
			}

			Response<Object> validateResponse=this.checkBasicValidations(drivers,session,CREATECHECK);
			if(validateResponse !=null){
				return new ResponseEntity<Response<Object>>(validateResponse, HttpStatus.OK);
			}
			
			// Duplicate phone number check
//			List<String> phoneNos = drivers.stream().filter(d -> d.getPhoneNumber() != null)
//					.map(d -> d.getPhoneNumber()).collect(Collectors.toList());
//			List<String> duplicatePhoneNos = driverValidations.checkIfExistsPhoneNumber(phoneNos,
//					session.getClientId().intValue(), referenceIds);
//
//			if (!duplicatePhoneNos.isEmpty()) {
//				isPhoneNumberExists = true;
//				List<Error> errors = new ArrayList<Error>();
//				Error error = new Error();
//				error.setKey("PhoneNumber");
//				List<String> msgs = new ArrayList<String>();
//				msgs.add(ResponseMessage.PHONENUMBER_EXISTS.fromValue());
//				error.setMessage(msgs);
//				errors.add(error);
//				errorMessages.put(errorString + count, errors);
//				count++;
//				isValidationFailed = true;
	//		}

			// License Number check

			List<String> duplicateLicenseNos = driverValidations.checkIfExistsLicenseNumber(licenseNos,
					session.getClientId().intValue(), referenceIds);

			if (!duplicateLicenseNos.isEmpty()) {
				List<Error> errors = new ArrayList<Error>();
				Error error = new Error();
				error.setKey("LicenseNumber");
				List<String> msgs = new ArrayList<String>();
				msgs.add(ResponseMessage.LICENSENUMBER_EXISTS.fromValue());
				error.setMessage(msgs);
				errors.add(error);
				errorMessages.put(errorString + count, errors);
				count++;
				isValidationFailed = true;
			}

			// DriverId check
			List<String> duplicateEmpIds = driverValidations.checkIfExistsEmployeeId(empIds,
					session.getClientId().intValue(), referenceIds);

			if (!duplicateEmpIds.isEmpty()) {
				List<Error> errors = new ArrayList<Error>();
				Error error = new Error();
				error.setKey("DriverEmployeeId");
				List<String> msgs = new ArrayList<String>();
				msgs.add(ResponseMessage.DRIVEREMPLOYEEID_EXISTS.fromValue());
				error.setMessage(msgs);
				errors.add(error);
				errorMessages.put(errorString + count, errors);
				count++;
				isValidationFailed = true;
			}

			if (isValidationFailed) {
				response.setHasError(TRUE);
				response.setMessage(ResponseMessage.FAIL.fromValue());
				response.setStatus(409);
				response.setError(errorMessages);
			} else {

				// Get Country and StateIds
				List<String> countryShortCodes = addresses.stream().filter(d -> d.getCountryShortCode() != null)
						.map(d -> d.getCountryShortCode()).collect(Collectors.toList());
				String GET_COUNTRY_IDS_URL = new StringBuilder().append(propertyConfig.getUrl())
						.append(LOOKUP_MICROSERVICE_URL).append(GET_LOCALEID).append("?type=")
						.append(Constants.LOCALE_COUNTRY).append("&shortcodes=")
						.append(countryShortCodes.toString().substring(1, countryShortCodes.toString().length() - 1))
						.toString();
				LocaleLookupDTO[] country_locale_arr = restTemplate.getForObject(GET_COUNTRY_IDS_URL,
						LocaleLookupDTO[].class);
				List<LocaleLookupDTO> country_locale = Arrays.asList(country_locale_arr);

				if (country_locale.isEmpty() || country_locale.size() != countryShortCodes.size()) {
					List<Error> errors = new ArrayList<Error>();
					Error error = new Error();
					error.setKey("Country Short Code");
					List<String> msgs = new ArrayList<String>();
					msgs.add("Invalid country code specified");
					error.setMessage(msgs);
					errors.add(error);
					errorMessages.put(errorString + count, errors);
					count++;
					isValidationFailed = true;
				}

				List<String> stateShortCodes = addresses.stream().filter(d -> d.getStateShortCode() != null)
						.map(d -> d.getStateShortCode()).collect(Collectors.toList());
				String GET_STATE_IDS_URL = new StringBuilder().append(propertyConfig.getUrl())
						.append(LOOKUP_MICROSERVICE_URL).append(GET_LOCALEID).append("?type=")
						.append(Constants.LOCALE_STATE).append("&shortcodes=")
						.append(stateShortCodes.toString().substring(1, stateShortCodes.toString().length() - 1))
						.toString();
				LocaleLookupDTO[] state_locale_arr = restTemplate.getForObject(GET_STATE_IDS_URL,
						LocaleLookupDTO[].class);
				List<LocaleLookupDTO> state_locale = Arrays.asList(state_locale_arr);
				stateShortCodes = new ArrayList(new HashSet(stateShortCodes));
				Set<String> distinctStates = new HashSet<String>();
				for(LocaleLookupDTO stateLocaleLookup : state_locale){
					distinctStates.add(stateLocaleLookup.getCode());
				}

				if (/*state_locale.isEmpty() || state_locale.size()*/distinctStates.size() != stateShortCodes.size()) {
					List<Error> errors = new ArrayList<Error>();
					Error error = new Error();
					error.setKey("State Short Code");
					List<String> msgs = new ArrayList<String>();
					msgs.add("Invalid state code specified");
					error.setMessage(msgs);
					errors.add(error);
					errorMessages.put(errorString + count, errors);
					count++;
					isValidationFailed = true;
				}

				if (isValidationFailed) {
					response.setHasError(TRUE);
					response.setMessage(ResponseMessage.FAIL.fromValue());
					response.setStatus(409);
					response.setError(errorMessages);
				} else {

					// Preparing Map
					HashMap<String, Integer> stateCodeIdMap = new HashMap<String, Integer>();
					for (LocaleLookupDTO localeLookupDTO : state_locale) {
						stateCodeIdMap.put(localeLookupDTO.getCode() + "-" + localeLookupDTO.getCountryCode(), localeLookupDTO.getId());
					}
					HashMap<String, Integer> countryCodeIdMap = new HashMap<String, Integer>();
					for (LocaleLookupDTO localeLookupDTO : country_locale) {
						countryCodeIdMap.put(localeLookupDTO.getCode(), localeLookupDTO.getId());
					}

					for (DriverDTO driverDTO : drivers) {
						driverDTO.setClientBranchId(session.getCurrentClientBranchId().intValue());
						driverDTO.setClientId(session.getClientId().intValue());
						driverDTO.setUpdatedByUserId(session.getUserId().intValue());
						for (AddressDTO address : driverDTO.getAddressList()) {
							if (countryCodeIdMap.get(address.getCountryShortCode()) != null)
								address.setCountry(countryCodeIdMap.get(address.getCountryShortCode()).toString());
							if (stateCodeIdMap.get(address.getStateShortCode()) != null)
								address.setState(stateCodeIdMap
										.get(address.getStateShortCode() + "-" + address.getCountryShortCode())
										.toString());
						}
					}

					String REST_URL = new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_UPDATE).toString();
					URI uri = Util.buildUri(session, REST_URL);
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					HttpEntity<List<DriverDTO>> req = new HttpEntity<List<DriverDTO>>(drivers, headers);
					ResponseEntity<Boolean> isUpdated = restTemplate.exchange(uri, HttpMethod.PUT, req, Boolean.class);
					logger.debug("Executed RestTemplate for DriverAPIController.update with response: [{}]",
							Slf4jUtility.toObjArr(isUpdated.getBody()));

					if (isUpdated.getBody()) {
						if(isPhoneNumberExists){
							response.setMessage(ResponseMessage.SUCCESS_PHONE_NO_EXIST_NOTIFY.fromValue());
						}else{
							response.setMessage(ResponseMessage.SUCCESS.fromValue());
						}
						response.setStatus(200);
					} else {
						response.setMessage(ResponseMessage.FAIL.fromValue());
						response.setStatus(200);
						response.setHasError(TRUE);
					}
				}
			}
			return new ResponseEntity<Response<Object>>(response, HttpStatus.OK);
			}else{
				response.setMessage("List should not be empty");
				response.setHasError(true);
				response.setStatus(400);
				return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
			}
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * delete drivers by driverIds
	 * 
	 * @param request
	 * @return Boolean
	 */
	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public ResponseEntity<Response<Object>> delete(@CurrentSession Session session, HttpServletRequest request,
			@RequestBody List<String> referenceIds) {
		Response<Object> response = new Response<Object>();
		try {
			logger.info("Start api delete call at: [{}]", Slf4jUtility.toObjArr(new Date()));

			// API Rate Limit check

			response = apiAuditService.validateRateLimitingAndAPIlogging(request, session);
			if (500 == response.getStatus() || 429 == response.getStatus()) {
				return new ResponseEntity<Response<Object>>(response, HttpStatus.OK);
			}

			/*
			 * String limit =
			 * redisCache.checkIfRateLimitReached(session.getClientId()); if
			 * (APIConstants.MAX_LIMIT_REACHED.equals(limit)) { logger.debug(
			 * "API Max Rate limit reached for clientId:[{}] at:[{}]",
			 * session.getClientId(), new Date()); response.setStatus(429);
			 * response.setMessage(ResponseMessage.MAX_LIMIT_REACHED.fromValue()
			 * ); return new ResponseEntity<Response<Object>>(response,
			 * HttpStatus.OK); }
			 */
			// Getting corresponding driverIds
			HttpHeaders header = new HttpHeaders();
			header.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<List<String>> entity = new HttpEntity<List<String>>(referenceIds, header);
			String GET_DRIVERIDS_URL = new StringBuilder().append(propertyConfig.getUrl())
					.append(DRIVER_MICROSERVICE_URL).append(GET_DRIVERIDS).toString();
			ResponseEntity<List> driverIds = restTemplate.exchange(GET_DRIVERIDS_URL, HttpMethod.POST, entity,
					List.class);

			if (!driverIds.getBody().isEmpty()) {
				String REST_URL = new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL)
						.append(DRIVER_DELETE).toString();
				URI uri = Util.buildUri(session, REST_URL);
				ResponseEntity<Boolean> isDeleted = restTemplate.exchange(uri, HttpMethod.POST,
						new HttpEntity<>(driverIds.getBody()), Boolean.class);
				logger.info("Executed RestTemplate for DriverAPIController.delete with response : [{}] at: [{}]",
						Slf4jUtility.toObjArr(isDeleted.getBody(), new Date()));

				if (isDeleted.getBody()) {
					response.setMessage(ResponseMessage.SUCCESS.fromValue());
					response.setStatus(200);
				} else {
					response.setMessage(ResponseMessage.FAIL.fromValue());
					response.setStatus(200);
					response.setHasError(TRUE);
				}
			} else {
				response.setMessage(ResponseMessage.DRIVER_NA.fromValue());
				response.setStatus(200);
			}
			return new ResponseEntity<Response<Object>>(response, HttpStatus.OK);

		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

	}

	@SuppressWarnings({ "unchecked" })
	@RequestMapping(value = "/list", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response<Object>> get(@CurrentSession Session session, HttpServletRequest request,
			@RequestBody List<String> referenceIds) {
		Response<Object> response = new Response<Object>();
		try {
			logger.info("Start api get call at: [{}]", Slf4jUtility.toObjArr(new Date()));

			// API Rate Limit check
			response = apiAuditService.validateRateLimitingAndAPIlogging(request, session);
			if (500 == response.getStatus() || 429 == response.getStatus()) {
				return new ResponseEntity<Response<Object>>(response, HttpStatus.OK);
			}

			/*
			 * String limit =
			 * redisCache.checkIfRateLimitReached(session.getClientId()); if
			 * (APIConstants.MAX_LIMIT_REACHED.equals(limit)) { logger.debug(
			 * "API Max Rate limit reached for clientId:[{}] at:[{}]",
			 * session.getClientId(), new Date()); response.setStatus(429);
			 * response.setMessage(ResponseMessage.MAX_LIMIT_REACHED.fromValue()
			 * ); return new ResponseEntity<Response<Object>>(response,
			 * HttpStatus.OK); }
			 */
			HttpHeaders header = new HttpHeaders();
			header.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<List<String>> req = new HttpEntity<List<String>>(referenceIds, header);
			String GET_DRIVERS = new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL)
					.append(DRIVER_GET_BY_REFERENCEIDS).toString();
			URI uri = Util.buildUri(session, GET_DRIVERS);
			ResponseEntity<DriverDTO[]> driversArr = restTemplate.exchange(uri, HttpMethod.POST, req, DriverDTO[].class);
			List<DriverDTO> drivers = Arrays.asList(driversArr.getBody());
			// logger.debug("Executed RestTemplate at url: [{}] on: [{}]",
			// Slf4jUtility.toObjArr(uri, new Date()));

			if (drivers.isEmpty()) {
				response.setMessage(ResponseMessage.DRIVER_NA.fromValue());
			} else if (drivers.size() != referenceIds.size()) {
				List<String> ids = ((List<DriverDTO>) drivers).stream().filter(d -> d.getReferenceId() != null)
						.map(d -> d.getReferenceId()).collect(Collectors.toList());
				Set<String> requestedIds = new HashSet<String>(referenceIds);
				Set<String> responseIds = new HashSet<String>(ids);
				requestedIds.removeAll(responseIds);
				response.setMessage("Following referenceIds not found:" + requestedIds);
				response.setData(drivers);
			} else {
				response.setMessage(ResponseMessage.SUCCESS.fromValue());
				response.setData(drivers);
			}
			response.setStatus(200);
			return new ResponseEntity<Response<Object>>(response, HttpStatus.OK);

		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@SuppressWarnings("unchecked")
	public Response<Object> checkBasicValidations(List<DriverDTO> driverTOs,Session session,String operation){
		Response< Object> response=null;
		try{
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<List<DriverDTO>> validationRequest = new HttpEntity<List<DriverDTO>>(driverTOs,headers);
			String VALIDATIONSERVICE = new StringBuilder(propertyConfig.getUrl()).append(VALIDATION_MICROSERVICE_URL).append(DRIVER_VALIDATION).append("?operation=").
					append(operation).append("&countryMode=").append(Constants.COUNTRYMODCODE).toString();
			URI validationUri = Util.buildUri(session, VALIDATIONSERVICE);
			Map<String,List<Error>> errorMessages =  restTemplate.postForObject(validationUri, validationRequest, Map.class);
			if(errorMessages !=null && errorMessages.size()>0){
				response = new Response<Object>();
				response.setHasError(TRUE);
				response.setMessage(ResponseMessage.FAIL.fromValue());
				response.setStatus(400);
				response.setError(errorMessages);
			}
			}catch (RestClientException ex) {
				logger.error(Util.errorMessage, ex);
			} catch (Exception ex) {
				logger.error(Util.errorMessage, ex);
			}
		return response;
	}
}
