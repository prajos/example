package com.loginext.web.driver.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.URI;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.POIXMLDocument;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.loginext.commons.aspect.CurrentSession;
import com.loginext.commons.aspect.Log;
import com.loginext.commons.aspect.PropertyConfig;
import com.loginext.commons.entity.Session;
import com.loginext.commons.enums.ResponseMessage;
import com.loginext.commons.model.DriverContactUpdateDTO;
import com.loginext.commons.model.DriverCreateDTO;
import com.loginext.commons.model.DriverDTO;
import com.loginext.commons.model.DriverListHolderDTO;
import com.loginext.commons.model.Error;
import com.loginext.commons.model.GenericTabularDTO;
import com.loginext.commons.model.Label;
import com.loginext.commons.model.PageDTO;
import com.loginext.commons.model.ResourceTrackingMapDTO;
import com.loginext.commons.model.Response;
import com.loginext.commons.model.ShiftDTO;
import com.loginext.commons.util.Constants;
import com.loginext.commons.util.DateUtility;
import com.loginext.commons.util.ResponseMessageConstants;
import com.loginext.commons.util.ResponseMessageUtil;
import com.loginext.commons.util.Slf4jUtility;
import com.loginext.commons.util.Util;
import com.loginext.web.driver.util.DriverExcel;
import com.loginext.web.driver.validations.DriverValidations;

@RequestMapping("/driver/fmlm")
@Controller
public class DriverFirstLastMileController implements DriverConstants, Constants {

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private PropertyConfig propertyConfig;
	
	@Autowired
	private DriverUtillity driverUtillity;

	private static @Log Logger logger;

	@Value("${driver.service}")
	private String DRIVER_MICROSERVICE_URL;
	
	@Value("${driver.create}")
	private String DRIVER_CREATE; 
	
	@Value("${driver.exists.license}")
	private String DRIVER_LICENSE_EXISTS;
	
	@Value("${driver.exists.employeeid}")
	private String DRIVER_EMPLOYEEID_EXISTS;
	
	@Value("${driver.exists}")
	private String DRIVER_EXISTS;
	
	@Value("${driver.get}")
	private String DRIVER_GET;
	
	@Value("${driver.delete}")
	private String DRIVER_DELETE;
	
	@Value("${driver.fmlm.attendance}")
	private String DRIVER_MARK_ATTENDANCE;

	@Value("${driver.fmlm.activestate}")
	private String DRIVER_ACTIVE_STATE;
	
	@Value("${driver.update}")
	private String DRIVER_UPDATE;
	
	@Value("${driver.update.get}")
	private String DRIVER_UPDATE_GET;
	
	@Value("${client.service}")
	private String CLIENT_MICROSERVICE_URL;
	
	@Value("${client.getClientBranchName}")
	private String GET_CLIENTBRANCH_NAME;
	
	@Value("${driver.update.list}")
	private String DRIVER_LIST_UPDATE;
	
	@Value("${label.get}")
	private String GET_LABELS;
	
	@Value("${framework.service}")
	private String FRAMEWORK_SERVICE;
	
	@Value("${shipmentReportPath}")
	private String driverReportPath;
	
	@Value("${driver.excel.download}")
	private String DRIVER_DOWNLOAD;
	
	@Value("${driver.excel.maxRowCount}")
	private String MAX_ROW_COUNT;
	
	@Value("${driver.get.shifts}")
	private String DRIVER_GET_SHIFTS;
	
	@Value("${list.available.driver}")
	private String DRIVER_LIST_AVAILABLE;
	
	@Value("${clientproperty.service.getByClientIdAndPropKeys}")
	private String CLIENTPROPERTY_GETBYCLIENTID_AND_PROPERTYKEYS;
	
	@Value("${driver.get.intransit.contact}")
	private String DRIVER_INTRANSIT_CONTACT_DETAILS;
	
	@Value("${driver.contact.update}")
	private String UPDATE_DRIVER_CONTACT;
	
	@Autowired
	private DriverValidations driverValidations;
	
	@Autowired
	private ResponseMessageUtil messageUtil;
	
	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Response<Object>> create(@CurrentSession Session session,@RequestPart(value = "licenseFileUpload", required = false) MultipartFile licenseFileUpload, @RequestPart(value = "idProofUpload", required = false) MultipartFile idProofUpload,
			HttpServletRequest request,@RequestParam(value = "proceedWithDupContactFl", required = false,defaultValue = "false") Boolean proceedWithDupContactFl) {
		Response<Object> response = null;
		Boolean isPhoneNumberAlreadyExist = false;
		try{
			response = new Response<Object>();
			List<String> ExistList=new ArrayList<String>();
			Integer clientId = session.getClientId().intValue();
			logger.info("start driver create call with Session is [{}] on [{}]", session.getToken(),new Date());
			Integer createdByUserId = session.getUserId().intValue();
			
			DriverDTO driverDTO = new DriverDTO();
			
			if (!Util.isNullOrEmpty(request.getParameter("phoneNumber"))) {
				String phone=request.getParameter("phoneNumber");
				List<String> phoneList=new ArrayList<>();
				phoneList.add(phone);
				List<String>  isexisting=checkIfExistsPhoneNumber(phoneList,clientId);
				if(isexisting !=null && isexisting.size() > 0){
					//ExistList.add(messageUtil.getMessageFromKey(ResponseMessageConstants.PHONE_EXISTS, session,propertyConfig.getUrl(), restTemplate));
					isPhoneNumberAlreadyExist = true;
					if(!proceedWithDupContactFl){
						logger.info("Create Driver, PhoneNumber Already Exists, confimation required [{}] on [{}]", session.getToken(),new Date());
						Error e = new Error();
						e.setMessage(ExistList);
						response.setError(e);
						response.setData(null);
						response.setStatus(428);
						response.setMessage(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_CONTACT_EXIST_CONFIRMATION, session,propertyConfig.getUrl(), restTemplate));
						response.setHasError(TRUE);
						response.setError(e);
						return new ResponseEntity<Response<Object>>(response, HttpStatus.PRECONDITION_REQUIRED);
					}
				}
			}
			
			if (!Util.isNullOrEmpty(request.getParameter("licenseNumber"))) {
				String license=request.getParameter("licenseNumber");
				List<String> licenseList=new ArrayList<>();
				licenseList.add(license);
				List<String>  isexisting=checkIfExistsLicenseNumber(licenseList,clientId);
				if( isexisting !=null && isexisting.size() > 0){
					ExistList.add(messageUtil.getMessageFromKey(ResponseMessageConstants.LICENSENUMBER_ALREADY_EXISTS, session,propertyConfig.getUrl(), restTemplate));
				}
			}
		
			if (!Util.isNullOrEmpty(request.getParameter("driverEmployeeId"))) {
				String employeeId=request.getParameter("driverEmployeeId");
				List<String> employeeidList=new ArrayList<>();
				employeeidList.add(employeeId);
				List<String>  isexisting=checkIfExistsEmployeeId(employeeidList,clientId);
				if(isexisting !=null && isexisting.size()  > 0){
					ExistList.add(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_EMPID_ALREADY_EXISTS, session,propertyConfig.getUrl(), restTemplate));
				}
			}
			
			if(ExistList.size() > 0 && null != ExistList)
			{
				logger.info("Create Driver Conflict Found, Fields Already Exist, Session is [{}] on [{}]", session.getToken(),new Date());
				Error e = new Error();
				e.setMessage(ExistList);
				response.setError(e);
				response.setData(null);
				response.setStatus(409);
				response.setMessage(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_DATA_CONFLICT, session,propertyConfig.getUrl(), restTemplate));
				response.setHasError(TRUE);
				response.setError(e);
				return new ResponseEntity<Response<Object>>(response, HttpStatus.CONFLICT);
			}
			
			driverDTO.setClientId(clientId);
			driverDTO.setCreatedByUserId(createdByUserId);
			List<DriverDTO> drivers=new ArrayList<>();
			DriverDTO filledDTO = driverUtillity.getdriverDTOfromRequest(driverDTO, licenseFileUpload, idProofUpload, request);
			if(null == filledDTO.getClientBranchId())
			{
				if (null != session.getCurrentClientBranchId()) {
					driverDTO.setClientBranchId(session.getCurrentClientBranchId().intValue());
				}
			}
			Boolean isCreated=false;
			drivers.add(filledDTO);
			String REST_URL = new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_CREATE).toString();
			 URI uri = Util.buildUri(session, REST_URL);
			 DriverCreateDTO[] createdIds=restTemplate.postForObject(uri, drivers, DriverCreateDTO[].class);
			List<DriverCreateDTO> createdList = Arrays.asList(createdIds);
			logger.debug("Executed RestTemplate for DriverController.Create Driver: [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(isCreated, uri, new Date()));
			if(null != createdList && createdList.size() > 0)
			{
				if(isPhoneNumberAlreadyExist){
					response.setMessage(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_CREATED_SUCCESS_MOBILENUM_EXIST, session,propertyConfig.getUrl(), restTemplate));
				}else{
					response.setMessage(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_CREATED_SUCCESS, session,propertyConfig.getUrl(), restTemplate));
				}
				response.setStatus(200);
				response.setHasError(Boolean.FALSE);
				return new ResponseEntity<Response<Object>>(response,HttpStatus.OK);
			}
		} catch (RestClientException ex) {
			logger.error(Util.errorMessage, ex);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		response = new Response<Object>();
		Error e = new Error();
		String message = messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_CREATED_FAILURE, session,propertyConfig.getUrl(), restTemplate);
		//"Create Driver Failed";
		List<String> messages = new ArrayList<>();
		messages.add(message);
		e.setMessage(messages);
		response.setStatus(500);
		//response.setMessage(ResponseMessage.FAIL.fromValue());
		response.setMessage(message);
		response.setHasError(Boolean.TRUE);
		response.setError(e);
		return new ResponseEntity<Response<Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@SuppressWarnings("unchecked")
	public List<String> checkIfExistsPhoneNumber(List<String> phoneNumbers, Integer clientId) {
		try {
			logger.info("start  checkIfExistsPhoneNumber call on: [{}]", new Date());
			if(phoneNumbers !=null && phoneNumbers.size() >0){
			String REST_URL = new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_EXISTS).append("?clientId=").append(clientId).append("&phoneNumber=").append(phoneNumbers.toString().substring(1,phoneNumbers.toString().length()-1)).toString();
			logger.debug("Executing RestTemplate call for DriverController.checkIfExistsPhoneNumber at url: [{}] on [{}]", REST_URL, new Date());
			List<String> dupicatePhoneNumbers = restTemplate.getForObject(REST_URL, List.class);
			logger.info("End  checkIfExistsPhoneNumber call [{}] on: [{}]",dupicatePhoneNumbers.size(), new Date());
			return dupicatePhoneNumbers;
			}
		} catch (RestClientException ex) {
			logger.error(Util.errorMessage, ex);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return null;

	}
	
	@SuppressWarnings("unchecked")
	public List<String> checkIfExistsLicenseNumber(List<String> licenseNumber,Integer clientId) {
		List<String> existsLicenseNumbers=new ArrayList<>();
		try {
			logger.info("Start  checkIfExistsLicenseNumber call on: [{}]",new Date());
			if(licenseNumber !=null && licenseNumber.size() >0){
			String REST_URL = new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_LICENSE_EXISTS).append(PATH_SEPARATOR).append(clientId).append("?licenseNumber=").append(licenseNumber.toString().substring(1,licenseNumber.toString().length()-1)).toString();
			logger.debug("Executing RestTemplate call for DriverController.checkIfExistsLicenseNumber at url: [{}] on [{}]", REST_URL, new Date());
			 existsLicenseNumbers =restTemplate.getForObject(REST_URL, List.class);
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
	public List<String> checkIfExistsEmployeeId(List<String> employeeIds,Integer clientId) {
		try {
			logger.info("Start checkIfExistsEmployeeId call on: [{}]",new Date());
			if(employeeIds !=null && employeeIds.size() >0){
			String REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_EMPLOYEEID_EXISTS).append(PATH_SEPARATOR).append(clientId).append("?employeeIds=").append(employeeIds.toString().substring(1,employeeIds.toString().length()-1)).toString();
			logger.info("Executing RestTemplate call for DriverController.checkIfExistsEmployeeId at url: [{}] on [{}]", REST_URL, new Date());
			List<String> duplicateEmpIds = restTemplate.getForObject(REST_URL, List.class);
			logger.info("End checkIfExistsEmployeeId call on: [{}]",duplicateEmpIds.size(),new Date());
			return duplicateEmpIds;
			}

		} catch (RestClientException ex) {
			logger.error(Util.errorMessage, ex);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return null;

	}
	
	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response<Object>> getDrivers(@CurrentSession Session currentSession, HttpServletRequest request,@RequestParam (required = false)String dashboardStatus) {
		Response<Object> responseObj = new Response<Object>();
		try {
			String token = currentSession.getToken();
			logger.info("Executing DriverController.getDrivers for session: [{}] on: [{}]", token, new Date());
			PageDTO page = Util.buildPageableObject(request);
			String get_drivers_url =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_GET).append(PATH_SEPARATOR).append(currentSession.getClientId()).toString();
			if(null != dashboardStatus && !dashboardStatus.isEmpty()){
				get_drivers_url=get_drivers_url+"?dashboardStatus="+dashboardStatus;
			}
			final URI uri = Util.buildPageableUri(currentSession, page, get_drivers_url);
			logger.debug("Executed RestTemplate for token [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(token, uri, new Date()));
			ResponseEntity<GenericTabularDTO> response = restTemplate.exchange(uri, HttpMethod.GET,  Util.buildHttpEntity(MediaType.APPLICATION_JSON),
															  new ParameterizedTypeReference<GenericTabularDTO>() {});
			logger.debug("Executed RestTemplate for token [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(token, uri,response.getBody().getTotalCount(), new Date()));
			if(null != response.getBody())
			{
				responseObj.setStatus(200);
				responseObj.setMessage(ResponseMessage.SUCCESS.fromValue());
				responseObj.setHasError(Boolean.FALSE);
				responseObj.setData(response.getBody());
				return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.OK);
			}
			
			logger.info("Executed DriverController.getDrivers for session: [{}] on: [{}]", token, new Date());
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		Error e = new Error();
		String message = "Failed to obtain Drivers";
		List<String> messages = new ArrayList<>();
		messages.add(message);
		e.setMessage(messages);
		responseObj.setStatus(500);
		responseObj.setMessage(ResponseMessage.FAIL.fromValue());
		responseObj.setHasError(Boolean.TRUE);
		responseObj.setError(e);
		return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
	public ResponseEntity<Response<Object>> delete(@CurrentSession Session session ,@RequestBody List<Integer> driverIds) {
		Response<Object> responseObj = new Response<Object>();
		try {
			if (driverIds != null && driverIds.size() > 0) {
				String REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_DELETE).toString();
				 URI uri = Util.buildUri(session, REST_URL);
				logger.info("Executing RestTemplate call for DriverController.delete at url: [{}] on [{}]", REST_URL, new Date());
				ResponseEntity<Boolean> isDeleted = restTemplate.exchange(uri, HttpMethod.POST, new HttpEntity<>(driverIds), Boolean.class);
				logger.debug("Executed RestTemplate for DriverController.delete with response : [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(isDeleted.getBody(), uri, new Date()));
				if(isDeleted.getBody())
				{
					responseObj.setStatus(200);
					responseObj.setMessage(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_DELETED_SUCCESS, session,propertyConfig.getUrl(), restTemplate));
					responseObj.setHasError(Boolean.FALSE);
					return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.OK);
				}
			}
		} catch (RestClientException ex) {
			logger.error(Util.errorMessage, ex);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		Error e = new Error();
		String message = messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_DELETED_FAILURE, session,propertyConfig.getUrl(), restTemplate);
		//String message = "Failed to Delete Driver";
		List<String> messages = new ArrayList<>();
		messages.add(message);
		e.setMessage(messages);
		responseObj.setStatus(500);
		responseObj.setMessage(ResponseMessage.FAIL.fromValue());
		responseObj.setHasError(Boolean.TRUE);
		responseObj.setError(e);
		return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/attendance", method = RequestMethod.PUT)
	public ResponseEntity<Response<Object>> updateAttendance(@CurrentSession Session session ,@RequestBody List<DriverDTO> driverDTOs) {
		Response<Object> responseObj = new Response<Object>();
		try {
			if (driverDTOs != null) {
				Integer userId = session.getUserId().intValue();
				for(DriverDTO d:driverDTOs)
				{
					d.setUpdatedByUserId(userId);
				}
				
				String REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_MARK_ATTENDANCE).toString();
				 URI uri = Util.buildUri(session, REST_URL);
				logger.info("Executing RestTemplate call for DriverController.updateAttendance at url: [{}] on [{}]", REST_URL, new Date());
				ResponseEntity<Boolean> isUpdated = restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity<>(driverDTOs), Boolean.class);
				logger.debug("Executed RestTemplate for DriverController.updateAttendance with response : [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(isUpdated.getBody(), uri, new Date()));
				if(isUpdated.getBody())
				{
					responseObj.setStatus(200);
					responseObj.setMessage(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_UPDATE_ATTENDANCE_SUCCESS, session,propertyConfig.getUrl(), restTemplate));
					responseObj.setHasError(Boolean.FALSE);
					return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.OK);
				}
			}
		} catch (RestClientException ex) {
			logger.error(Util.errorMessage, ex);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		Error e = new Error();
		//String message = "Failed to Mark Attendance";
		String message = messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_UPDATE_ATTENDANCE_FAILURE, session,propertyConfig.getUrl(), restTemplate);
		List<String> messages = new ArrayList<>();
		messages.add(message);
		e.setMessage(messages);
		responseObj.setStatus(500);
		//responseObj.setMessage(ResponseMessage.FAIL.fromValue());
		responseObj.setMessage(message);
		responseObj.setHasError(Boolean.TRUE);
		responseObj.setError(e);
		return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/state", method = RequestMethod.PUT)
	public ResponseEntity<Response<Object>> updateActiveState(@CurrentSession Session session ,@RequestBody List<DriverDTO> driverDTOs) {
		Response<Object> responseObj = new Response<Object>();
		try {
			if (driverDTOs != null) {
				Integer userId = session.getUserId().intValue();
				for(DriverDTO d:driverDTOs)
				{
					d.setUpdatedByUserId(userId);
				}
				
				String REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_ACTIVE_STATE).toString();
				 URI uri = Util.buildUri(session, REST_URL);
				logger.info("Executing RestTemplate call for DriverController.updateAttendance at url: [{}] on [{}]", REST_URL, new Date());
				ResponseEntity<Boolean> isUpdated = restTemplate.exchange(uri, HttpMethod.PUT, new HttpEntity<>(driverDTOs), Boolean.class);
				logger.debug("Executed RestTemplate for DriverController.updateAttendance with response : [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(isUpdated.getBody(), uri, new Date()));
				if(isUpdated.getBody())
				{
					responseObj.setStatus(200);
					//responseObj.setMessage(ResponseMessage.SUCCESS.fromValue());
					responseObj.setMessage(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_UPDATE_STATUS_SUCCESS, session,propertyConfig.getUrl(), restTemplate));
					responseObj.setHasError(Boolean.FALSE);
					return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.OK);
				}
			}
		} catch (RestClientException ex) {
			logger.error(Util.errorMessage, ex);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		Error e = new Error();
		//String message = "Failed to Mark Attendance";
		String message = messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_UPDATE_STATUS_FAILURE, session,propertyConfig.getUrl(), restTemplate);
		List<String> messages = new ArrayList<>();
		messages.add(message);
		e.setMessage(messages);
		responseObj.setStatus(500);
		//responseObj.setMessage(ResponseMessage.FAIL.fromValue());
		responseObj.setMessage(message);
		responseObj.setHasError(Boolean.TRUE);
		responseObj.setError(e);
		return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	

	@RequestMapping(value = "/update", method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<Response<Object>> update(@RequestPart(value = "licenseFileUpload", required = false) MultipartFile licenseFileUpload, @RequestPart(value = "idProofUpload", required = false) MultipartFile idProofUpload,
			HttpServletRequest request,  @CurrentSession Session session,@RequestParam(value = "proceedWithDupContactFl", required = false,defaultValue = "false") Boolean proceedWithDupContactFl) {
		Response<Object> responseObj = new Response<Object>();
		try {
			List<String> ExistList=new ArrayList<String>();
			Integer clientId = session.getClientId().intValue();
			logger.info("start driver update call with Session token  [{}] with clientId [{}] on [{}]",Slf4jUtility.toObjArr(session.getToken(), clientId, new Date()));
			Integer updatedByUserId = session.getUserId().intValue();
			Integer driverId=Integer.parseInt(request.getParameter("driverId"));
			DriverDTO driverDTO = new DriverDTO();
			if (session.getCurrentClientBranchId() != null) {
				driverDTO.setClientBranchId(session.getCurrentClientBranchId().intValue());
			}
			driverDTO.setClientId(clientId);
			driverDTO.setUpdatedByUserId(updatedByUserId);
			driverDTO.setCreatedByUserId(updatedByUserId);
			
			if (!Util.isNullOrEmpty(request.getParameter("phoneNumber"))) {
				if(!proceedWithDupContactFl){
					String phone=request.getParameter("phoneNumber");
					String previousPhone=request.getParameter("prevPhoneNumber");
					List<String> phoneList=new ArrayList<>();
					phoneList.add(phone);
					if(previousPhone != null && !previousPhone.equals(phone)){
					List<String>  isexisting=checkIfExistsPhoneNumber(phoneList,clientId);
					if(isexisting !=null && isexisting.size() >0){
						String message = messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_UPDATE_PHONENO_EXIST_CONFIRMATION, session,propertyConfig.getUrl(), restTemplate);
						ExistList.add(message);
						Error e = new Error();
						e.setMessage(ExistList);
						responseObj.setError(e);
						responseObj.setData(null);
						responseObj.setStatus(428);
						responseObj.setMessage(message);
						responseObj.setHasError(Boolean.TRUE);
						responseObj.setError(e);
						return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.PRECONDITION_REQUIRED);
						}
					}
				}
			}
			
			if (!Util.isNullOrEmpty(request.getParameter("licenseNumber"))) {
				String license=request.getParameter("licenseNumber");
				String previousLicense=request.getParameter("prevLisenceNumber");
				List<String> licenseList=new ArrayList<>();
				licenseList.add(license);
				if(previousLicense != null && !previousLicense.equals(license)){
				List<String>  isexisting=checkIfExistsLicenseNumber(licenseList,clientId);
				if( isexisting !=null && isexisting.size() >0){
					ExistList.add(LICENSE_NUMBER_EXISTS);
					}
				}
			}
			
			if (!Util.isNullOrEmpty(request.getParameter("driverEmployeeId"))) {
				String employeeId = request.getParameter("driverEmployeeId");
				String previousemployeeId = request.getParameter("previousEmpId");
				List<String> employeeidList=new ArrayList<>();
				employeeidList.add(employeeId);
				if (previousemployeeId != null && !previousemployeeId.equals(employeeId)) {
					List<String> isexisting = checkIfExistsEmployeeId(employeeidList, clientId);
					if (isexisting != null && isexisting.size() > 0) {
						ExistList.add(DRIVER_ID_EXISTS);
					}
				}
			}
			if(null != ExistList && ExistList.size() > 0)
			{
				logger.info("update Driver Conflict Found, Fields Already Exist, Session is [{}] on [{}]", session.getToken(),new Date());
				Error e = new Error();
				e.setMessage(ExistList);
				responseObj.setError(e);
				responseObj.setData(null);
				responseObj.setStatus(409);
				//responseObj.setMessage(ResponseMessage.CONFLICT.fromValue());
				responseObj.setMessage(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_DATA_CONFLICT, session,propertyConfig.getUrl(), restTemplate));
				responseObj.setHasError(Boolean.TRUE);
				responseObj.setError(e);
				return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.CONFLICT);
			}

			List<DriverDTO> drivers=new ArrayList<>();

			DriverDTO filleddriverDTO = driverUtillity.getdriverDTOfromRequest(driverDTO, licenseFileUpload, idProofUpload, request);
			filleddriverDTO.setDriverId(driverId);
			String REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_UPDATE).toString();
			URI uri=Util.buildUri(session, REST_URL);


			drivers.add(filleddriverDTO);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<List<DriverDTO>> req = new HttpEntity<List<DriverDTO>>(drivers,headers);
			ResponseEntity<Boolean> isDriverUpdated=restTemplate.exchange(uri,HttpMethod.PUT, req, Boolean.class);
			logger.info("Executed restTemplate for driver update call with response  [{}] for URL [{}] on [{}]",Slf4jUtility.toObjArr(isDriverUpdated.getBody(), uri, new Date()));
			
			if(isDriverUpdated.getBody()){
				responseObj.setStatus(200);
				//responseObj.setMessage(ResponseMessage.SUCCESS.fromValue());
				responseObj.setMessage(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_UPDATED_SUCCESS, session,propertyConfig.getUrl(), restTemplate));
				responseObj.setHasError(Boolean.FALSE);
				return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.OK);
			}
			
		} catch (RestClientException ex) {
			logger.error(Util.errorMessage, ex);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		Error e = new Error();
		//String message = "Failed to Update Driver";
		String message = messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_UPDATED_FAILURE, session,propertyConfig.getUrl(), restTemplate);
		List<String> messages = new ArrayList<>();
		messages.add(message);
		e.setMessage(messages);
		responseObj.setStatus(500);
		//responseObj.setMessage(ResponseMessage.FAIL.fromValue());
		responseObj.setMessage(message);
		responseObj.setHasError(Boolean.TRUE);
		responseObj.setError(e);
		return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response<Object>> getByDriverId(@CurrentSession Session session,@RequestParam(value="driverId") Integer driverId,@RequestParam(required=false,defaultValue=Constants.ALL) String fetchType, HttpServletRequest request) {
		Response<Object> responseObj = null;
		try {
			logger.debug("start call getByDriverId for  token [{}] and  driverId: [{}] on: [{}]", Slf4jUtility.toObjArr(session.getToken(),driverId, new Date()));
			
			 String GET_REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_UPDATE_GET).append(PATH_SEPARATOR).append(driverId).append("?fetchType=").append(fetchType).toString();			
			 URI uri = Util.buildUri(session, GET_REST_URL);
			DriverDTO driver = restTemplate.getForObject(uri,DriverDTO.class);
			if(null !=driver && null != driver.getClientBranchId())
			{
				logger.debug("Executed RestTemplate for driverId: [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(driver.getDriverId(), uri, new Date()));
				HttpHeaders header = new HttpHeaders();
				header.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
				header.set(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE);
				HttpEntity<String> requestHeader = new HttpEntity<String>(null, header);
				String GET_CLIENT_BRANCH_NAME = new StringBuilder().append(propertyConfig.getUrl()).append(CLIENT_MICROSERVICE_URL).
						append(GET_CLIENTBRANCH_NAME).append("?clientBranchId=").append(driver.getClientBranchId()).toString();
				//String clientBranchName = restTemplate.getForObject(GET_CLIENT_BRANCH_NAME, String.class);
				ResponseEntity<String> clientBranchName = restTemplate.exchange(GET_CLIENT_BRANCH_NAME, HttpMethod.GET, requestHeader, String.class);
				driver.setClientBranchName(clientBranchName.getBody());
			}
			logger.info("End getByDriverId call on: [{}]", new Date());
			responseObj = new Response<Object>();
			responseObj.setStatus(200);
			responseObj.setData(driver);
			responseObj.setMessage(ResponseMessage.SUCCESS.fromValue());
			responseObj.setHasError(Boolean.FALSE);
			return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.OK);			
		} catch (RestClientException ex) {
			logger.error(Util.errorMessage, ex);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		Error e = new Error();
		String message = "Failed to get Driver for Update";
		List<String> messages = new ArrayList<>();
		messages.add(message);
		e.setMessage(messages);
		responseObj.setStatus(500);
		responseObj.setMessage(ResponseMessage.FAIL.fromValue());
		responseObj.setHasError(Boolean.TRUE);
		responseObj.setError(e);
		return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/update/list", method = RequestMethod.PUT,produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response<Object>> updateDriver(@RequestBody List<DriverDTO> driverDTOs,@CurrentSession Session session) {
		Response<Object> responseObj = null;
		try {
			Integer clientId = session.getClientId().intValue();
			logger.info("start driver Bulk update call with Session token  [{}] with clientId [{}] on [{}]",Slf4jUtility.toObjArr(session.getToken(), clientId, new Date()));
			if(null != driverDTOs && driverDTOs.size() > 0)
			{
				List<String> existCheckList = getDuplicates(driverDTOs);
				responseObj = new Response<Object>();
				
				if(existCheckList.size() > 0 && null != existCheckList)
				{
					List<String> messageString = new ArrayList<String>();
					//messageString.add(PHONE_NUMBER_EXISTS);
					messageString.add(messageUtil.getMessageFromKey(ResponseMessageConstants.PHONE_EXISTS, session,propertyConfig.getUrl(), restTemplate));
					logger.info("Bulk update Driver Internal Conflict Found, Fields Already Exist, Session is [{}] on [{}]", session.getToken(),new Date());
					Error e = new Error();
					e.setMessage(messageString);
					responseObj.setError(e);
					responseObj.setData(null);
					responseObj.setStatus(409);
					//responseObj.setMessage(ResponseMessage.CONFLICT.fromValue());
					responseObj.setMessage(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_DATA_CONFLICT, session,propertyConfig.getUrl(), restTemplate));
					responseObj.setHasError(Boolean.TRUE);
					responseObj.setError(e);
					return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.CONFLICT);	
				}
				List<String> phoneNumbers = new ArrayList<String>();
				for(DriverDTO dto : driverDTOs)
				{
					dto.setUpdatedByUserId(session.getUserId().intValue());
					dto.setUpdatedOnDt(new Date());
					if(null != dto.getPreviousPhoneNumber() && !dto.getPreviousPhoneNumber().equals(dto.getPhoneNumber()))
					{
						phoneNumbers.add(dto.getPhoneNumber());
					}
				}
				if(null != phoneNumbers && phoneNumbers.size() > 0) {
					List<String>  isexisting=driverValidations.checkIfExistsPhoneNumber(phoneNumbers,clientId,null);
					if(isexisting !=null && isexisting.size() > 0){
						logger.info("Bulk update Driver Internal Conflict Found in DB check, Fields Already Exist, Session is [{}] on [{}]", session.getToken(),new Date());
						List<String> messageString = new ArrayList<String>();
						//messageString.add(PHONE_NUMBER_EXISTS);
						messageString.add(messageUtil.getMessageFromKey(ResponseMessageConstants.PHONE_EXISTS, session,propertyConfig.getUrl(), restTemplate));
						Error e = new Error();
						e.setMessage(messageString);
						responseObj.setError(e);
						responseObj.setData(null);
						responseObj.setStatus(409);
						//responseObj.setMessage(ResponseMessage.CONFLICT.fromValue());
						responseObj.setMessage(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_DATA_CONFLICT, session,propertyConfig.getUrl(), restTemplate));
						responseObj.setHasError(Boolean.TRUE);
						responseObj.setError(e);
						return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.CONFLICT);	
						}
				}

				String REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_LIST_UPDATE).toString();
				URI uri=Util.buildUri(session, REST_URL);
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				HttpEntity<List<DriverDTO>> req = new HttpEntity<List<DriverDTO>>(driverDTOs,headers);
				ResponseEntity<Boolean> isDriverUpdated=restTemplate.exchange(uri,HttpMethod.PUT, req, Boolean.class);
				logger.info("Executed restTemplate for  driver update call with response  [{}] for URL [{}] on [{}]",Slf4jUtility.toObjArr(isDriverUpdated.getBody(), uri, new Date()));
				
				if(isDriverUpdated.getBody()){
					responseObj.setStatus(200);
					//responseObj.setMessage(ResponseMessage.SUCCESS.fromValue());
					responseObj.setMessage(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_UPDATED_SUCCESS, session,propertyConfig.getUrl(), restTemplate));
					responseObj.setHasError(Boolean.FALSE);
					return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.OK);
				}
			}	
			} catch (RestClientException ex) {
				logger.error(Util.errorMessage, ex);
			} catch (Exception ex) {
				logger.error(Util.errorMessage, ex);
			}
		
		Error e = new Error();
		//String message = "Failed to Update Driver";
		String message = messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_UPDATED_FAILURE, session,propertyConfig.getUrl(), restTemplate);
		List<String> messages = new ArrayList<>();
		messages.add(message);
		e.setMessage(messages);
		responseObj.setStatus(500);
		//responseObj.setMessage(ResponseMessage.FAIL.fromValue());
		responseObj.setMessage(message);
		responseObj.setHasError(Boolean.TRUE);
		responseObj.setError(e);
		return new ResponseEntity<Response<Object>>(responseObj, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	private List<String> getDuplicates(List<DriverDTO> driverDTOs) {
		try {
				// Finding out duplicate phoneNumber internal check
				List<String> phoneNumberExistList=new ArrayList<String>();
				Set<String> phoneNumberSet = new HashSet<String>();
				
				for (DriverDTO dto : driverDTOs) {
					if (!phoneNumberSet.add(dto.getPhoneNumber())) {
						phoneNumberExistList.add(dto.getPhoneNumber());
					}
				}
			return phoneNumberExistList;
		}
		catch(Exception ex)
		{
			logger.error(Util.errorMessage, ex);
		}
		return null;
	}
	
	@RequestMapping(value = "/listview/download", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<byte[]> downloadExcelReport(@CurrentSession Session currentSession, HttpServletRequest request,@RequestParam(required=false)String dashboardStatus) {
		try {
			String token = currentSession.getToken();
			logger.info("Executing DriverController.downloadExcelReport for session: [{}] on: [{}]", token, new Date());
			PageDTO page = Util.buildPageableObject(request);
			page.setSize(Integer.parseInt(MAX_ROW_COUNT));
			String get_drivers_url =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_DOWNLOAD).append(PATH_SEPARATOR).append(currentSession.getClientId()).toString();
			if(null != dashboardStatus && !dashboardStatus.isEmpty()){
				get_drivers_url=get_drivers_url+"?dashboardStatus="+dashboardStatus;
			}
			final URI uri = Util.buildPageableUri(currentSession, page, get_drivers_url);
			logger.debug("Executed RestTemplate for token [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(token, uri, new Date()));
			ResponseEntity<List<DriverDTO>> response = restTemplate.exchange(uri, HttpMethod.GET,  Util.buildHttpEntity(MediaType.APPLICATION_JSON),
															  new ParameterizedTypeReference<List<DriverDTO>>() {});
			logger.debug("Executed RestTemplate for token [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(token, uri,response.getBody().size(), new Date()));

			//Report settings
			DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			String fileName = "DriverReport.xlsx";
			String sourceFileName = "DriverReport_" + dateFormat.format(new Date()) + ".xlsx";
			String sourceFileAddress = driverReportPath+sourceFileName;
			File excelReport = new File(driverReportPath+sourceFileName);
			if(excelReport.exists())
				excelReport.delete();
			// fetching labels
			
			String[] headings = {"name","clientBranchName","contactNo","workHrs","trackerID","status","lastTracking","tripNo","vehicleNo","attendance","active"};
			String [] statusLabels = {"ACTIVE","INACTIVE","INTRANSIT","AVAILABLE","ABSENT"} ;
			List<String> labels = new ArrayList<String>();
			labels.addAll(Arrays.asList(statusLabels));
			labels.addAll(Arrays.asList(headings));   
			
			String REST_URL = new StringBuilder().append(propertyConfig.getUrl()).append(FRAMEWORK_SERVICE)
					.append(GET_LABELS).append("?labels=").append(labels.toString().substring(1, labels.toString().length() - 1)).toString();
			URI labelUri = Util.buildUri(currentSession,REST_URL);
			Label[] labelsArray = restTemplate.getForObject(labelUri,Label[].class);
			List<Label> labelsList = Arrays.asList(labelsArray);
			Map<String, Label> labelsDataMap = labelsList.stream()
					.collect(Collectors.toMap(Label::getKey, (r) -> r));
			
			//fetched labels from mongo
			DriverExcel.buildExcel(response.getBody(), sourceFileAddress,currentSession,labelsDataMap,restTemplate,propertyConfig,CLIENT_MICROSERVICE_URL,CLIENTPROPERTY_GETBYCLIENTID_AND_PROPERTYKEYS);

			byte[] body = Files.readAllBytes(new File(sourceFileAddress).toPath());
			HttpHeaders respHeaders = new HttpHeaders();
			respHeaders.set("Content-Type", "application/vnd.ms-excel");
			respHeaders.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(body.length));
			respHeaders.set("Content-Disposition", "attachment; filename=" + fileName);
			respHeaders.set("Transfer-Encoding", "binary");
			respHeaders.setContentLength(body.length);
			return new ResponseEntity<byte[]>(body, respHeaders, HttpStatus.OK);
			
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/getShifts", method = RequestMethod.GET)
	public ResponseEntity<Response<Object>> getShifts(@CurrentSession Session session,@RequestParam Integer driverId) {
		Response<Object> result = new Response<>();
		try {
			logger.info("Executing  getShifts call on: [{}]",new Date());

			if (driverId != null) {
				
				String REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_GET_SHIFTS).append("?driverId=").append(driverId).toString();
				URI uri=Util.buildUri(session, REST_URL);
				ShiftDTO[] shiftList = restTemplate.getForObject(REST_URL, ShiftDTO[].class);
				logger.info("Executed  getShifts call with response  [{}] for URL [{}] on [{}]",Slf4jUtility.toObjArr(Arrays.asList(shiftList).size(), uri, new Date()));
				result.setStatus(200);
				result.setData(Arrays.asList(shiftList));
				result.setMessage(ResponseMessage.SUCCESS.fromValue());
				return new ResponseEntity<Response<Object>>(result,HttpStatus.OK);
			}
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		result.setStatus(500);
		result.setHasError(Boolean.TRUE);
		result.setMessage(ResponseMessage.FAIL.fromValue());
		return new ResponseEntity<Response<Object>>(result,HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/download", method = RequestMethod.POST)
	public void downloadExcel(HttpServletRequest request, HttpServletResponse response) {

		String path = request.getSession().getServletContext().getRealPath("/Uploads");
		// String
		path = "/Users/priyankshivhare/Documents/branch_workspace/TrackAPack_super/src/main/webapp/Uploads";
		String filename;

		int fileType = 2;// Integer.parseInt(request.getParameter("file"));

		String name = "Loginext_Driver_Upload_Format.xls";
		if (fileType == 1)
			filename = "Loginext_Driver_Upload_Format.xls";
		else {

			//int clientId = 24;//
			int clientId=Integer.parseInt(request.getSession().getAttribute("clientId").toString());
			String loggedInUser = "12";// (String)(request.getSession().getAttribute("userid"));
			int userId = Integer.parseInt(loggedInUser);
			filename = "Loginext_Driver_Upload_Format" + clientId + "_" + userId + ".xls";
		}
		FileInputStream in=null;
		try {
			OutputStream out = response.getOutputStream();
			 in = new FileInputStream(path + File.separator + filename);

			response.setContentType("application/vnd.ms-excel");
			response.addHeader("content-disposition", "attachment; filename=" + name);

			int octet;
			while ((octet = in.read()) != -1)
				out.write(octet);

			in.close();
			out.close();
			if (fileType != 1) {
				File file = new File(path + File.separator + filename);
				file.delete();
			}
		} catch (Exception e) {
			logger.error("" + e);
		}finally{
			Util.resourceCloseUtil(in, null, null);
		}

	}
	
	// TODO : import driver is pending
		@RequestMapping(value = "/import", method = RequestMethod.POST)
		public ResponseEntity<Boolean> importExcel(@RequestPart(value = "importExcel") MultipartFile excelData, HttpServletRequest request) throws IOException {
			InputStream in=null;
			Workbook wb =null;
			try {
				Session session = (Session) request.getAttribute(SESSION_OBJECT);
				Integer clientId = null;
				Integer clientBranchId = null;
				Integer createdByUserId = null;
				//
				clientId = session.getClientId().intValue();
				createdByUserId = session.getUserId().intValue();
				clientBranchId = null;// session.getDistributionCenter();

				// String filename = "userFile" + clientId + "_" + createdByUserId +
				// ".xls";
				 in = excelData.getInputStream();
				 wb = new HSSFWorkbook(new POIFSFileSystem(in));

				Boolean isNotValidExcel = validate(wb, clientId, createdByUserId);

				if (!isNotValidExcel) {
					DriverListHolderDTO holderDTOs = driverUtillity.getDriverDTOFromExcel(wb, clientId, clientBranchId, createdByUserId);

					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					HttpEntity<Object> req = new HttpEntity<Object>(holderDTOs, headers);

					String CREATE_DRIVER_FROM_LIST = propertyConfig.getUrl() + DRIVER_MICROSERVICE_URL + "import";
					Boolean isCreated = restTemplate.postForObject(CREATE_DRIVER_FROM_LIST, req, Boolean.class);

					return new ResponseEntity<Boolean>(isCreated, isCreated == Boolean.TRUE ? HttpStatus.CREATED : HttpStatus.INTERNAL_SERVER_ERROR);
				} else {
					return new ResponseEntity<>(false, HttpStatus.METHOD_NOT_ALLOWED);
				}

			} catch (RestClientException ex) {
				logger.error(Util.errorMessage, ex);
			} catch (Exception ex) {
				logger.error(Util.errorMessage, ex);
			}finally{
				if(in !=null)
					in.close();
				if(wb !=null)
					wb.close();
			}
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// TODO: retrieveAllState,language, service,defaultvehicle,licenseNumber
		private Boolean validate(Workbook wb, Integer clientId, Integer userId) throws IOException {
			Boolean dirtyFile = false;

			Sheet sheet = wb.getSheetAt(0);
			Integer lastRowNum = sheet.getLastRowNum();
			System.out.println(lastRowNum);

			List<String> defaultVehicles = new ArrayList<String>();
			List<String> licenseNumberList = new ArrayList<String>();

			// To hold all unique set
			Set<String> defualtVehicleSet = new HashSet<String>();
			Set<String> licenseNumberSet = new HashSet<String>();

			// To hold all duplicate entries
			Set<String> duplicateDefualtVehicleSet = new HashSet<>();
			Set<String> duplicatelicenseNumberSet = new HashSet<>();

			// To hold error set
			List<String> message = new ArrayList<>();
			int startRow = 6;
			Row fieldNameRow = sheet.getRow(3);
			int stateId = 0;

			for (int i = startRow; i <= lastRowNum; i++) {
				Row currentRow = sheet.getRow(i);
				Cell defualtVehicle = CellUtil.getCell(currentRow, ASSIGN_VEHICLE);
				System.out.println(defualtVehicle.toString());
				if (defualtVehicle.getCellType() != Cell.CELL_TYPE_BLANK) {
					defaultVehicles.add(defualtVehicle.toString());
					if (defualtVehicleSet.contains(defualtVehicle.toString())) {
						duplicateDefualtVehicleSet.add(defualtVehicle.toString());
					} else {
						defualtVehicleSet.add(defualtVehicle.toString());
					}
				}
				Cell licenseNumber = CellUtil.getCell(currentRow, LICENSE_NUMBER);
				System.out.println(licenseNumber.toString());
				if (licenseNumber.getCellType() != Cell.CELL_TYPE_BLANK) {
					String licenseNumberValue = licenseNumber.toString();
					licenseNumberList.add(licenseNumberValue);
					if (licenseNumberSet.contains(licenseNumberValue)) {
						duplicatelicenseNumberSet.add(licenseNumberValue);
					} else {
						licenseNumberSet.add(licenseNumberValue);
					}
				}
			}

			String CHECK_IF_VEHICLE_EXISTS =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_EXISTS).append("?clientId=").append(clientId).append("&assignVehicle=").append(defaultVehicles).toString();
			String[] duplicatedefaultVehiclesList = restTemplate.getForObject(CHECK_IF_VEHICLE_EXISTS, String[].class);
			Set<String> duplicatedefaultVehicleDBSet = new HashSet<String>(Arrays.asList(duplicatedefaultVehiclesList));
			// String CHECK_IF_VEHICLE_EXISTS = DRIVER_MICROSERVICE_URL +
			// "exists?clientId=" + clientId
			// + "&assignVehicle=" + defaultVehicles;
			// String[] duplicatedefaultVehiclesList =
			// restTemplate.getForObject(CHECK_IF_VEHICLE_EXISTS, String[].class);
			// Set<String> duplicateVehicleNumberDBSet = new
			// HashSet<String>(Arrays.asList(duplicatedefaultVehiclesList));
			//
			// Validate entire Row
			for (int row = startRow; row <= lastRowNum; row++) {
				Boolean validationFlag = false;
				Row currentRow = sheet.getRow(row);

				// VehicleNumber check
				Cell licensdeNumberCell = CellUtil.getCell(currentRow, LICENSE_NUMBER);
				if (licensdeNumberCell.getCellType() == Cell.CELL_TYPE_BLANK) {
					message.add(fieldNameRow.getCell(LICENSE_NUMBER).toString());
					validationFlag = true;
					dirtyFile = true;
				}
				Cell vehicleCell = CellUtil.getCell(currentRow, ASSIGN_VEHICLE);
				if (vehicleCell.getCellType() != Cell.CELL_TYPE_BLANK) {
					if (duplicatedefaultVehicleDBSet.contains(vehicleCell.toString())) {
						message.add("Vehicle : " + vehicleCell.toString() + "already mapped.");
						validationFlag = true;
						dirtyFile = true;
					}
				}
				Cell curstateCell = CellUtil.getCell(currentRow, CUR_STATE);
				if (curstateCell.getCellType() != Cell.CELL_TYPE_BLANK) {
					stateId = 0; // getState(state,
									// curstateCell.toString());
					if (stateId == 0) {
						message.add("State : " + curstateCell.toString() + " doesn't exist.");
						validationFlag = true;
						dirtyFile = true;
					}
				}

				Cell perstateCell = CellUtil.getCell(currentRow, PER_STATE);
				if (perstateCell.getCellType() != Cell.CELL_TYPE_BLANK) {
					stateId = 0;// barcodeFound(state,
								// stateCell.toString());
					if (stateId == 0) {
						message.add("State : " + perstateCell.toString() + " doesn't exist.");
						validationFlag = true;
						dirtyFile = true;
					}
				}
				List<Integer> integerNumberFormatCell = new ArrayList<Integer>(
						Arrays.asList(DRIVER_NAME, CUR_APARTMENT, CUR_STREET_NAME, CUR_AREA_NAME, CUR_LANDMARK, CUR_CITY, PER_APARTMENT, PER_STREET_NAME, PER_AREA_NAME, PER_LANDMARK, PER_CITY));
				for (Integer fieldValue : integerNumberFormatCell) {
					Cell fieldCell = CellUtil.getCell(currentRow, fieldValue);
					try {

						if (fieldCell.getCellType() != Cell.CELL_TYPE_BLANK)
							;
						fieldCell.toString();
					} catch (Exception e) {
						message.add("It Is Mandatory Field " + fieldNameRow.getCell(fieldValue));
						validationFlag = true;
						dirtyFile = true;
					}
				}
				List<Integer> integerFormatCell = new ArrayList<Integer>(Arrays.asList(CONTACT_NUMBER, MANAGER_CONTACT_NUMBER, CUR_PINCODE, PER_PINCODE));
				for (Integer fieldValue : integerFormatCell) {
					Cell fieldCell = CellUtil.getCell(currentRow, fieldValue);
					try {
						if (fieldCell.getCellType() != Cell.CELL_TYPE_BLANK)
							Integer.parseInt(fieldValue.toString());
					} catch (Exception e) {
						message.add("Please enter Integer value for " + fieldNameRow.getCell(fieldValue));
						validationFlag = true;
						dirtyFile = true;
					}
				}
				Cell licenseValidityCell = CellUtil.getCell(currentRow, LICENSE_VALIDITY);
				if (licenseValidityCell.getCellType() != Cell.CELL_TYPE_BLANK) {
					try {
						 DateUtility.stringToDateFormat(licenseValidityCell.toString());
					} catch (Exception e) {
						message.add("Wrong date format of :" + fieldNameRow.getCell(LICENSE_VALIDITY));
						validationFlag = true;
						dirtyFile = true;
					}
				}

				Cell dateofbirth = CellUtil.getCell(currentRow, DATE_OF_BIRTH);
				if (dateofbirth.getCellType() != Cell.CELL_TYPE_BLANK) {
					try {
						DateUtility.stringToDateFormat(dateofbirth.toString());
					} catch (Exception e) {
						message.add("Wrong date format of :" + fieldNameRow.getCell(DATE_OF_BIRTH));
						validationFlag = true;
						dirtyFile = true;
					}
				}

				List<Integer> doubleNumberFormatCheckList = new ArrayList<Integer>(Arrays.asList(SALARY));
				for (Integer fieldValue : doubleNumberFormatCheckList) {
					Cell fieldCell = CellUtil.getCell(currentRow, fieldValue);
					try {
						if (fieldCell.getCellType() != Cell.CELL_TYPE_BLANK)
							Double.parseDouble(fieldValue.toString());
					} catch (Exception e) {
						message.add("Please enter Decimal value for " + fieldNameRow.getCell(fieldValue));
						validationFlag = true;
						dirtyFile = true;
					}
				}

				if (validationFlag) {
					Cell validationCell = currentRow.createCell(ERROR_MSG);
					validationCell = fillCell(wb, validationCell, message.toString(), IndexedColors.RED.getIndex());

				} else {
					Cell validationCell = currentRow.createCell(ERROR_MSG);
					validationCell = fillCell(wb, validationCell, "Validated", IndexedColors.GREEN.getIndex());
				}
			}

			// To mark duplicates within excel
			for (int row = startRow; row <= lastRowNum; row++) {
				Row currentRow = sheet.getRow(row);
				String licenseNumberValue = currentRow.getCell(LICENSE_NUMBER).toString();
				String defaultVehicleValue = currentRow.getCell(ASSIGN_VEHICLE).toString();
				Cell validationCell = currentRow.getCell(ERROR_MSG);

				if (duplicatelicenseNumberSet.contains(licenseNumberValue)) {
					validationCell = fillCell(wb, validationCell, "Duplicate license number in excel", IndexedColors.RED.getIndex());
					dirtyFile = true;
				}
				if (duplicateDefualtVehicleSet.contains(defaultVehicleValue)) {
					validationCell = fillCell(wb, validationCell, "Duplicate assign vehicle in excel", IndexedColors.RED.getIndex());
					dirtyFile = true;
				}
			}
			// "/Uploads" + File.separator + "Loginext_Vehicle_Upload_Format" +
			// clientId + "_" + userId + ".xls"

			try {
				if (!dirtyFile) {
					FileOutputStream bos = new FileOutputStream(new File("/Users/vishwanath/Downloads/vishwa123.xls"));
					wb.write(bos);
					bos.close();
				}
			} catch (Exception e) {
				System.out.println(e);
			} finally {
				wb.close();
			}
			return dirtyFile;

		}

		private Cell fillCell(Workbook wb, Cell cell, String cellValue, short backgroundColourIndex) {
			Font font = wb.createFont();
			font.setBold(true);
			font.setFontName("Arial");

			CellStyle style = wb.createCellStyle();
			style.setFillForegroundColor(backgroundColourIndex);
			style.setFillBackgroundColor(backgroundColourIndex);
			style.setFont(font);
			cell.setCellStyle(style);
			cell.setCellValue(cellValue + "," + cell.toString());
			return cell;
		}

		public Workbook create(InputStream inp) throws Exception {
			if (!inp.markSupported()) {
				inp = new PushbackInputStream(inp, 8);
			}
			if (POIFSFileSystem.hasPOIFSHeader(inp)) {
				logger.error("2003 and below");
				return new HSSFWorkbook(inp);
			}
			if (POIXMLDocument.hasOOXMLHeader(inp)) {
				logger.error("2007 and above");
				return new XSSFWorkbook(OPCPackage.open(inp));
			}
			logger.error("Your version of Excel is not POI analysis");
			return null;
		}	
		
	@RequestMapping(value = "/list/available", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Response<Object>> getAvailableDriverByClientIdAndBranches(@CurrentSession Session session) {
		Response<Object> response = new Response<>();
		try {
			String GET_AVAILBLE_VEHICLES = new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_LIST_AVAILABLE).toString();
			URI uri = Util.buildUri(session, GET_AVAILBLE_VEHICLES);
			logger.info("start call getAvailableDriverByClientIdAndBranches with  url: [{}] on: [{}]", Slf4jUtility.toObjArr(uri, new Date()));

			ResourceTrackingMapDTO[] notAssignedVehicleDTOs = restTemplate.getForObject(uri,ResourceTrackingMapDTO[].class);
			if (notAssignedVehicleDTOs != null && notAssignedVehicleDTOs.length > 0) {
				logger.debug("Executed RestTemplate for getAvailableDriverByClientIdAndBranches: [{}] at url: [{}] on: [{}]",Slf4jUtility.toObjArr(notAssignedVehicleDTOs.length, uri, new Date()));
				response.setData(Arrays.asList(notAssignedVehicleDTOs));
			}
			response.setStatus(200);
			response.setMessage(" Drivers Get Successfully");
			return new ResponseEntity<Response<Object>>(response, HttpStatus.OK);
		} catch (RestClientException ex) {
			logger.error(Util.errorMessage, ex);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		response.setStatus(500);
		response.setMessage("Driver Get  Failed");
		response.setHasError(TRUE);
		return new ResponseEntity<Response<Object>>(response, HttpStatus.INTERNAL_SERVER_ERROR);
	}

		@SuppressWarnings("unchecked")
		@RequestMapping(value = "/contact/validate", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity<Response<Object>> validateDriverContact(@CurrentSession Session session,@RequestBody List<String> phoneNumbers) {
			List<String> existingPhoneNumbers = new ArrayList<>();
			Response<Object> response = new Response<Object>();
			try {
				if(null != phoneNumbers && phoneNumbers.size() > 0){
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					HttpEntity<List<String>> validationRequest = new HttpEntity<List<String>>(phoneNumbers, headers);
					StringBuilder REST_URL = new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_INTRANSIT_CONTACT_DETAILS);
					URI uri = Util.buildUri(session, REST_URL.toString());
					logger.info("Executing RestTemplate call for DriverFirstLastMileController.validateDriverContact at url: [{}] on [{}]",REST_URL, new Date());
					existingPhoneNumbers = restTemplate.postForObject(uri,validationRequest, List.class);
				}
				response.setStatus(200);
				response.setMessage(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_CONTACT_VERIFIED_SUCCESS, session,propertyConfig.getUrl(), restTemplate));
				response.setHasError(Boolean.FALSE);
				response.setData(existingPhoneNumbers);
				return new ResponseEntity<Response<Object>>(response, HttpStatus.OK);
			} catch (RestClientException ex) {
				logger.error(Util.errorMessage, ex);
			} catch (Exception ex) {
				logger.error(Util.errorMessage, ex);
			}
			response.setStatus(500);
			response.setMessage(messageUtil.getMessageFromKey(ResponseMessageConstants.DRIVER_CONTACT_VERIFIED_SUCCESS, session,propertyConfig.getUrl(), restTemplate));
			response.setHasError(Boolean.TRUE);
			return new ResponseEntity<Response<Object>>(response,HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		@RequestMapping(value = "/contact", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
		public ResponseEntity<Boolean> updateDriverPhoneNumbers(@CurrentSession Session session,@RequestBody List<DriverContactUpdateDTO> driverContactUpdateDTO) {
			Boolean isUpdated = false;
			try {
				if(null != driverContactUpdateDTO && driverContactUpdateDTO.size() > 0){
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					HttpEntity<List<DriverContactUpdateDTO>> validationRequest = new HttpEntity<List<DriverContactUpdateDTO>>(driverContactUpdateDTO, headers);
					StringBuilder REST_URL = new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(UPDATE_DRIVER_CONTACT);
					URI uri = Util.buildUri(session, REST_URL.toString());
					logger.info("Executing RestTemplate call for DriverFirstLastMileController.updateDriverPhoneNumbers at url: [{}] on [{}]",REST_URL, new Date());
					ResponseEntity<Boolean> response = restTemplate.exchange(uri, HttpMethod.PUT,validationRequest, Boolean.class);
					isUpdated = response.getBody();
				}
				return new ResponseEntity<Boolean>(isUpdated, HttpStatus.OK);
			} catch (RestClientException ex) {
				logger.error(Util.errorMessage, ex);
			} catch (Exception ex) {
				logger.error(Util.errorMessage, ex);
			}
			return new ResponseEntity<Boolean>(isUpdated, HttpStatus.INTERNAL_SERVER_ERROR);
		}
}
