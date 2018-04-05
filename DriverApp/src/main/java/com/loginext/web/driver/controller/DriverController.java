package com.loginext.web.driver.controller;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.loginext.commons.aspect.Log;
import com.loginext.commons.util.Constants;
import com.loginext.web.driver.validations.DriverValidations;

@RequestMapping("/driver")
@Controller
public class DriverController implements DriverConstants, Constants {

	private static @Log Logger logger;

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
	
	@Value("${driver.get}")
	private String DRIVER_GET;
	
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

	@Value("${driver.excel.maxRowCount}")
	private String MAX_ROW_COUNT;
	
	@Value("${shipmentReportPath}")
	private String driverReportPath;
	
	@Value("${driver.excel.download}")
	private String DRIVER_DOWNLOAD;
	

	@Value("${label.get}")
	private String GET_LABELS;
	
	@Value("${framework.service}")
	private String FRAMEWORK_SERVICE;
	
	
	private final static String  PHONE_NUMBER_EXISTS="Phone Number Already Exists";
	private final static String  LICENSE_NUMBER_EXISTS="License Number Already Exists";
	private final static String  DRIVER_ID_EXISTS="Driver Id Already Exists";

	@Autowired
	private DriverValidations driverValidations;

	/**
	 * get drivers by client Id and clientBranchids
	 * 
	 * @param request
	 * @return list
	 */
//	@RequestMapping(value = "/list", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<GenericTabularDTO> getDrivers(@CurrentSession Session currentSession, HttpServletRequest request) {
//		try {
//			String token = currentSession.getToken();
//			logger.info("Executing DriverController.getDrivers for session: [{}] on: [{}]", token, new Date());
//			PageDTO page = Util.buildPageableObject(request);
//			final String get_drivers_url =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_GET).append(PATH_SEPARATOR).append(currentSession.getClientId()).toString();
//			final URI uri = Util.buildPageableUri(currentSession, page, get_drivers_url);
//			logger.debug("Executed RestTemplate for token [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(token, uri, new Date()));
//			ResponseEntity<GenericTabularDTO> response = restTemplate.exchange(uri, HttpMethod.GET,  Util.buildHttpEntity(MediaType.APPLICATION_JSON),
//															  new ParameterizedTypeReference<GenericTabularDTO>() {});
//			logger.debug("Executed RestTemplate for token [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(token, uri,response.getBody().getTotalCount(), new Date()));
//
//			logger.info("Executed DriverController.getDrivers for session: [{}] on: [{}]", token, new Date());
//			return new ResponseEntity<GenericTabularDTO>(response.getBody(), HttpStatus.OK);
//		} catch (Exception ex) {
//			logger.error(Util.errorMessage, ex);
//		}
//		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//	}
//	
//	@RequestMapping(value = "/listview/download", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<byte[]> downloadExcelReport(@CurrentSession Session currentSession, HttpServletRequest request) {
//		try {
//			String token = currentSession.getToken();
//			logger.info("Executing DriverController.downloadExcelReport for session: [{}] on: [{}]", token, new Date());
//			PageDTO page = Util.buildPageableObject(request);
//			page.setSize(Integer.parseInt(MAX_ROW_COUNT));
//			final String get_drivers_url =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_DOWNLOAD).append(PATH_SEPARATOR).append(currentSession.getClientId()).toString();
//			final URI uri = Util.buildPageableUri(currentSession, page, get_drivers_url);
//			logger.debug("Executed RestTemplate for token [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(token, uri, new Date()));
//			ResponseEntity<List<DriverDTO>> response = restTemplate.exchange(uri, HttpMethod.GET,  Util.buildHttpEntity(MediaType.APPLICATION_JSON),
//															  new ParameterizedTypeReference<List<DriverDTO>>() {});
//			logger.debug("Executed RestTemplate for token [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(token, uri,response.getBody().size(), new Date()));
//
//			//Report settings
//			String fileName = "DriverReport.xls";
//			String sourceFileAddress = driverReportPath+fileName;
//			File excelReport = new File(driverReportPath+fileName);
//			if(excelReport.exists())
//				excelReport.delete();
//			// fetching labels
//			
//			String[] headings = {"name","contactNo","workHrs","trackerID","status","lastTracking","tripNo","vehicleNo","attendance","active"};
//			
//			List<String> labels = new ArrayList<String>();
//			labels.addAll(Arrays.asList(headings));   
//			
//			String REST_URL = new StringBuilder().append(propertyConfig.getUrl()).append(FRAMEWORK_SERVICE)
//					.append(GET_LABELS).append("?labels=").append(labels.toString().substring(1, labels.toString().length() - 1)).toString();
//			URI labelUri = Util.buildUri(currentSession,REST_URL);
//			Label[] labelsArray = restTemplate.getForObject(labelUri,Label[].class);
//			List<Label> labelsList = Arrays.asList(labelsArray);
//			Map<String, Label> labelsDataMap = labelsList.stream()
//					.collect(Collectors.toMap(Label::getKey, (r) -> r));
//			
//			//fetched labels from mongo
//			
//			
//			DriverExcel.buildExcel(response.getBody(), sourceFileAddress,currentSession,labelsDataMap);
//
//			byte[] body = Files.readAllBytes(new File(sourceFileAddress).toPath());
//			HttpHeaders respHeaders = new HttpHeaders();
//			respHeaders.set("Content-Type", "application/vnd.ms-excel");
//			respHeaders.set(HttpHeaders.CONTENT_LENGTH, String.valueOf(body.length));
//			respHeaders.set("Content-Disposition", "attachment; filename=" + fileName);
//			respHeaders.set("Transfer-Encoding", "binary");
//			respHeaders.setContentLength(body.length);
//			return new ResponseEntity<byte[]>(body, respHeaders, HttpStatus.OK);
//			
//		} catch (Exception ex) {
//			logger.error(Util.errorMessage, ex);
//		}
//		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//	}
//
//	@RequestMapping(value = "/get", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<DriverDTO> getByDriverId(@CurrentSession Session session,@RequestParam(value="driverId") Integer driverId, HttpServletRequest request) {
//		try {
//			logger.debug("start call getByDriverId for  token [{}] and  driverId: [{}] on: [{}]", Slf4jUtility.toObjArr(session.getToken(),driverId, new Date()));
//			
//			 String GET_REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_UPDATE_GET).append(PATH_SEPARATOR).append(driverId).toString();			
//			 URI uri = Util.buildUri(session, GET_REST_URL);
//			DriverDTO driver = restTemplate.getForObject(uri,DriverDTO.class);
//			logger.debug("Executed RestTemplate for driverId: [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(driver.getDriverId(), uri, new Date()));
//			logger.info("End getByDriverId call on: [{}]", new Date());
//			return new ResponseEntity<DriverDTO>(driver, HttpStatus.OK);			
//		} catch (RestClientException ex) {
//			logger.error(Util.errorMessage, ex);
//		} catch (Exception ex) {
//			logger.error(Util.errorMessage, ex);
//		}
//		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//	}
//	
//	/**
//	 * update drivers
//	 * 
//	 * @param request
//	 * @return void
//	 */
//	//TODO: pending for bulk update
//	@RequestMapping(value = "/update/list", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<List<String>> updateDrivers(@CurrentSession Session session,@RequestBody List<DriverDTO> drivers, HttpServletRequest request) {
//		try {
//			List<String> phoneList=new ArrayList<>();
//			List<String> updatedList=new ArrayList<>();
//			Boolean isUpdate=true;
//			if (drivers != null && drivers.size() > 0) {
//				Integer clientId = session.getClientId().intValue();
//				Integer userId = session.getUserId().intValue();
//				List<Integer> clientBranchId = session.getClientBranches();
//				
//				for (DriverDTO driverDTO : drivers) {
//					driverDTO.setClientId(clientId);
//					driverDTO.setUpdatedByUserId(userId);
//					if (clientBranchId != null && clientBranchId.size() > 0) {
//						driverDTO.setClientBranchId(clientBranchId.get(0));
//					}
//					phoneList.add(driverDTO.getPhoneNumber());
//				}
//				List<String>  isExistNumber=driverValidations.checkIfExistsPhoneNumber(phoneList,clientId,null);
//				if(isExistNumber !=null && isExistNumber.size() >0){
//					return new ResponseEntity<List<String>>(isExistNumber, HttpStatus.CONFLICT);
//					}
//				
//				String REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_UPDATE).toString();
//				 URI uri = Util.buildUri(session, REST_URL);
//				HttpHeaders headers = new HttpHeaders();
//				headers.setContentType(MediaType.APPLICATION_JSON);
//				HttpEntity<List<DriverDTO>> req = new HttpEntity<List<DriverDTO>>(drivers,headers);
//				ResponseEntity<Boolean> isupdated=restTemplate.exchange(uri,HttpMethod.PUT, req, Boolean.class);
//				logger.debug("Executed RestTemplate for DriverController.updateDrivers: [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(drivers.size(), uri, new Date()));
//
//				if(isupdated.equals(isUpdate)){
//					updatedList.add("Drivers updated SuccessFully");
//				}
//				return new ResponseEntity<List<String>>(updatedList,HttpStatus.OK);
//			}
//		} catch (RestClientException ex) {
//			logger.error(Util.errorMessage, ex);
//		} catch (Exception ex) {
//			logger.error(Util.errorMessage, ex);
//		}
//		return null;
//	}
//
//	/**
//	 * update driver attandence and isActivefl
//	 * 
//	 * @param request
//	 * @return boolean
//	 */
//	@RequestMapping(value = "/attendance", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
//	public ResponseEntity<Boolean> updateAttandence(@CurrentSession Session session,@RequestBody DriverDTO driverdto, HttpServletRequest request) {
//		try {
//
//			if (driverdto != null) {
//				logger.info("Session is {}", session.getToken());
//				HttpHeaders header = new HttpHeaders();
//				header.setContentType(MediaType.APPLICATION_JSON);
//				HttpEntity<DriverDTO> req = new HttpEntity<DriverDTO>(driverdto, header);
//				
//				String REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_UPDATE_ACTIVE).toString() ;
//				 URI uri = Util.buildUri(session, REST_URL);
//
//				driverdto.setClientId(session.getClientId().intValue());
//				ResponseEntity<Boolean> isUpdated = restTemplate.exchange(uri, HttpMethod.PUT, req, Boolean.class);
//				logger.debug("Executed RestTemplate for DriverController.updateDrivers: [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(driverdto.getDriverId(), uri, new Date()));
//
//				return new ResponseEntity<Boolean>(isUpdated.getBody(), HttpStatus.OK);
//			}
//		} catch (RestClientException ex) {
//			logger.error(Util.errorMessage, ex);
//		} catch (Exception ex) {
//			logger.error(Util.errorMessage, ex);
//		}
//		return new ResponseEntity<Boolean>(Boolean.FALSE, HttpStatus.BAD_REQUEST);
//	}
//
//	/**
//	 * create driver with file
//	 * 
//	 * @param request with MultipartFile
//	 * @return Map<String,String>
//	 */
//	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public ResponseEntity<Map<String,String>> create(@RequestPart(value = "licenseFileUpload", required = false) MultipartFile licenseFileUpload, @RequestPart(value = "idProofUpload", required = false) MultipartFile idProofUpload,
//			HttpServletRequest request) {
//
//		try {
//			
//			Map<String,String> mapList=new HashMap<String, String>();
//			Boolean iscreate=true;
//			Session session = (Session) request.getAttribute(SESSION_OBJECT);
//			Integer clientId = session.getClientId().intValue();
//			logger.info("start driver create call with Session is [{}] on [{}]", session.getToken(),new Date());
//			Integer createdByUserId = session.getUserId().intValue();
//			List<Integer> clientBranchId = session.getClientBranches();
//			DriverDTO driverDTO = new DriverDTO();
//			if (clientBranchId != null && clientBranchId.size() > 0) {
//				driverDTO.setClientBranchId(clientBranchId.get(0));
//			}
//			if (!Util.isNullOrEmpty(request.getParameter("phoneNumber"))) {
//				String phone=request.getParameter("phoneNumber");
//				List<String> phoneList=new ArrayList<>();
//				phoneList.add(phone);
//				List<String>  isexisting=driverValidations.checkIfExistsPhoneNumber(phoneList,clientId,null);
//				if(isexisting !=null && isexisting.size() >0){
//					mapList.put(Message,PHONE_NUMBER_EXISTS);
//					return new ResponseEntity<Map<String,String>>(mapList, HttpStatus.CONFLICT);
//					}
//			}
//			if (!Util.isNullOrEmpty(request.getParameter("licenseNumber"))) {
//				String license=request.getParameter("licenseNumber");
//				List<String> licenseList=new ArrayList<>();
//				licenseList.add(license);
//				List<String>  isexisting=driverValidations.checkIfExistsLicenseNumber(licenseList,clientId,null);
//				if( isexisting !=null && isexisting.size() >0){
//					mapList.put(Message,LICENSE_NUMBER_EXISTS);
//					return new ResponseEntity<Map<String,String>>(mapList, HttpStatus.CONFLICT);
//				}
//			}
//		
//			if (!Util.isNullOrEmpty(request.getParameter("driverEmployeeId"))) {
//				String employeeId=request.getParameter("driverEmployeeId");
//				List<String> employeeidList=new ArrayList<>();
//				employeeidList.add(employeeId);
//				List<String>  isexisting=driverValidations.checkIfExistsEmployeeId(employeeidList,clientId,null);
//				if(isexisting !=null && isexisting.size()  >0){
//					mapList.put(Message,DRIVER_ID_EXISTS);
//					return new ResponseEntity<Map<String,String>>(mapList, HttpStatus.CONFLICT);
//				}
//			}
//			driverDTO.setClientId(clientId);
//			driverDTO.setCreatedByUserId(createdByUserId);
//			List<DriverDTO> drivers=new ArrayList<>();
//			DriverDTO filledDTO = driverUtillity.getdriverDTOfromRequest(driverDTO, licenseFileUpload, idProofUpload, request);
//			drivers.add(filledDTO);
//			String REST_URL = new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_CREATE).toString();
//			 URI uri = Util.buildUri(session, REST_URL);
//
//
//			DriverCreateDTO[] referenceIds=restTemplate.postForObject(uri, drivers, DriverCreateDTO[].class);
//			logger.debug("Executed RestTemplate for DriverController.createDrivers: at url: [{}] on: [{}]", Slf4jUtility.toObjArr(uri, new Date()));
//			if(referenceIds.length>0){
//				mapList.put(Message,"Driver Created Successfully");
//			}else{
//				return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//
//			}
//			return new ResponseEntity<Map<String,String>>(mapList,HttpStatus.OK);
//
//		} catch (RestClientException ex) {
//			logger.error(Util.errorMessage, ex);
//		} catch (Exception ex) {
//			logger.error(Util.errorMessage, ex);
//		}
//		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//	}
//
//	/**
//	 * delete drivers by driverIds
//	 * 
//	 * @param request
//	 * @return Boolean
//	 */
//	@RequestMapping(value = "/delete", method = RequestMethod.DELETE)
//	public ResponseEntity<Boolean> delete(@CurrentSession Session session ,@RequestBody List<Integer> driverIds) {
//		try {
//			if (driverIds != null && driverIds.size() > 0) {
//				String REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_DELETE).toString();
//				 URI uri = Util.buildUri(session, REST_URL);
//				logger.info("Executing RestTemplate call for DriverController.delete at url: [{}] on [{}]", REST_URL, new Date());
//				ResponseEntity<Boolean> isDeleted = restTemplate.exchange(uri, HttpMethod.DELETE, new HttpEntity<>(driverIds), Boolean.class);
//				logger.debug("Executed RestTemplate for DriverController.delete with response : [{}] at url: [{}] on: [{}]", Slf4jUtility.toObjArr(isDeleted.getBody(), uri, new Date()));
//
//				return new ResponseEntity<Boolean>(isDeleted.getBody() == Boolean.TRUE ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR);
//			}
//		} catch (RestClientException ex) {
//			logger.error(Util.errorMessage, ex);
//		} catch (Exception ex) {
//			logger.error(Util.errorMessage, ex);
//		}
//		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//
//	}
//
//	@RequestMapping(value = "/update", method = RequestMethod.PUT, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//	public ResponseEntity<Map<String,String>> update(@RequestPart(value = "licenseFileUpload", required = false) MultipartFile licenseFileUpload, @RequestPart(value = "idProofUpload", required = false) MultipartFile idProofUpload,
//			HttpServletRequest request,  @CurrentSession Session session) {
//
//		try {
//			Boolean isUpdate=true;
//			Map<String,String> mapList=new HashMap<String, String>();
//			Integer clientId = session.getClientId().intValue();
//			logger.info("start driver update call with Session token  [{}] with clientId [{}] on [{}]",Slf4jUtility.toObjArr(session.getToken(), clientId, new Date()));
//			Integer updatedByUserId = session.getUserId().intValue();
//			Integer driverId=Integer.parseInt(request.getParameter("driverId"));
//			List<Integer> clientBranchId = session.getClientBranches();
//			DriverDTO driverDTO = new DriverDTO();
//			if (clientBranchId != null && clientBranchId.size() > 0) {
//				driverDTO.setClientBranchId(clientBranchId.get(0));
//			}
//			driverDTO.setClientId(clientId);
//			driverDTO.setUpdatedByUserId(updatedByUserId);
//			driverDTO.setCreatedByUserId(updatedByUserId);
//			
//			if (!Util.isNullOrEmpty(request.getParameter("phoneNumber"))) {
//				String phone=request.getParameter("phoneNumber");
//				String previousPhone=request.getParameter("prevPhoneNumber");
//				List<String> phoneList=new ArrayList<>();
//				phoneList.add(phone);
//				if(previousPhone != null && !previousPhone.equals(phone)){
//				List<String>  isexisting=driverValidations.checkIfExistsPhoneNumber(phoneList,clientId,null);
//				if(isexisting !=null && isexisting.size() >0){
//					mapList.put(Message, PHONE_NUMBER_EXISTS);
//					return new ResponseEntity<Map<String,String>>(mapList, HttpStatus.CONFLICT);
//					}
//				}
//			}
//			
//			if (!Util.isNullOrEmpty(request.getParameter("licenseNumber"))) {
//				String license=request.getParameter("licenseNumber");
//				String previousLicense=request.getParameter("prevLisenceNumber");
//				List<String> licenseList=new ArrayList<>();
//				licenseList.add(license);
//				if(previousLicense != null && !previousLicense.equals(license)){
//				List<String>  isexisting=driverValidations.checkIfExistsLicenseNumber(licenseList,clientId,null);
//				if( isexisting !=null && isexisting.size() >0){
//					mapList.put(Message, LICENSE_NUMBER_EXISTS);
//					return new ResponseEntity<Map<String,String>>(mapList, HttpStatus.CONFLICT);
//				}
//				}
//			}
//			
//			if (!Util.isNullOrEmpty(request.getParameter("driverEmployeeId"))) {
//				String employeeId = request.getParameter("driverEmployeeId");
//				String previousemployeeId = request.getParameter("previousEmpId");
//				List<String> employeeidList=new ArrayList<>();
//				employeeidList.add(employeeId);
//				if (previousemployeeId != null && !previousemployeeId.equals(employeeId)) {
//					List<String> isexisting = driverValidations.checkIfExistsEmployeeId(employeeidList, clientId,null);
//					if (isexisting != null && isexisting.size() > 0) {
//						mapList.put(Message, DRIVER_ID_EXISTS);
//						return new ResponseEntity<Map<String, String>>(mapList, HttpStatus.CONFLICT);
//					}
//
//				}
//			}
//
//			List<DriverDTO> drivers=new ArrayList<>();
//
//			DriverDTO filleddriverDTO = driverUtillity.getdriverDTOfromRequest(driverDTO, licenseFileUpload, idProofUpload, request);
//			filleddriverDTO.setDriverId(driverId);
//			String REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_UPDATE).toString();
//			URI uri=Util.buildUri(session, REST_URL);
//
//
//			drivers.add(filleddriverDTO);
//			HttpHeaders headers = new HttpHeaders();
//			headers.setContentType(MediaType.APPLICATION_JSON);
//			HttpEntity<List<DriverDTO>> req = new HttpEntity<List<DriverDTO>>(drivers,headers);
//			ResponseEntity<Boolean> isDriverUpdated=restTemplate.exchange(uri,HttpMethod.PUT, req, Boolean.class);
//			logger.info("Executed restTemplate for  driver update call with response  [{}] for URL [{}] on [{}]",Slf4jUtility.toObjArr(isDriverUpdated.getBody(), uri, new Date()));
//			if(isDriverUpdated.equals(isUpdate)){
//			mapList.put(Message,"Driver updated SuccessFully");
//			}
//			return new ResponseEntity<Map<String,String>>(mapList, HttpStatus.OK);
//			
//		} catch (RestClientException ex) {
//			logger.error(Util.errorMessage, ex);
//		} catch (Exception ex) {
//			logger.error(Util.errorMessage, ex);
//		}
//		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//	}
//
//	@RequestMapping(value = "/getShifts", method = RequestMethod.GET)
//	public ResponseEntity<List<ShiftDTO>> getShifts(@CurrentSession Session session,@RequestParam Integer driverId) {
//		try {
//			logger.info("Executing  getShifts call on: [{}]",new Date());
//
//			if (driverId != null) {
//				
//				String REST_URL =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_GET_SHIFTS).append("?driverId=").append(driverId).toString();
//				URI uri=Util.buildUri(session, REST_URL);
//				ShiftDTO[] shiftList = restTemplate.getForObject(REST_URL, ShiftDTO[].class);
//				logger.info("Executed  getShifts call with response  [{}] for URL [{}] on [{}]",Slf4jUtility.toObjArr(Arrays.asList(shiftList).size(), uri, new Date()));
//
//				return new ResponseEntity<List<ShiftDTO>>(Arrays.asList(shiftList), HttpStatus.OK);
//			}
//		} catch (Exception ex) {
//			logger.error(Util.errorMessage, ex);
//		}
//		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//	}
//
//	// TODO : import driver is pending
//	@RequestMapping(value = "/import", method = RequestMethod.POST)
//	public ResponseEntity<Boolean> importExcel(@RequestPart(value = "importExcel") MultipartFile excelData, HttpServletRequest request) throws IOException {
//		InputStream in=null;
//		Workbook wb =null;
//		try {
//			Session session = (Session) request.getAttribute(SESSION_OBJECT);
//			Integer clientId = null;
//			Integer clientBranchId = null;
//			Integer createdByUserId = null;
//			//
//			clientId = session.getClientId().intValue();
//			createdByUserId = session.getUserId().intValue();
//			clientBranchId = null;// session.getDistributionCenter();
//
//			// String filename = "userFile" + clientId + "_" + createdByUserId +
//			// ".xls";
//			 in = excelData.getInputStream();
//			 wb = new HSSFWorkbook(new POIFSFileSystem(in));
//
//			Boolean isNotValidExcel = validate(wb, clientId, createdByUserId);
//
//			if (!isNotValidExcel) {
//				DriverListHolderDTO holderDTOs = driverUtillity.getDriverDTOFromExcel(wb, clientId, clientBranchId, createdByUserId);
//
//				HttpHeaders headers = new HttpHeaders();
//				headers.setContentType(MediaType.APPLICATION_JSON);
//				HttpEntity<Object> req = new HttpEntity<Object>(holderDTOs, headers);
//
//				String CREATE_DRIVER_FROM_LIST = propertyConfig.getUrl() + DRIVER_MICROSERVICE_URL + "import";
//				Boolean isCreated = restTemplate.postForObject(CREATE_DRIVER_FROM_LIST, req, Boolean.class);
//
//				return new ResponseEntity<Boolean>(isCreated, isCreated == Boolean.TRUE ? HttpStatus.CREATED : HttpStatus.INTERNAL_SERVER_ERROR);
//			} else {
//				return new ResponseEntity<>(false, HttpStatus.METHOD_NOT_ALLOWED);
//			}
//
//		} catch (RestClientException ex) {
//			logger.error(Util.errorMessage, ex);
//		} catch (Exception ex) {
//			logger.error(Util.errorMessage, ex);
//		}finally{
//			if(in !=null)
//				in.close();
//			if(wb !=null)
//				wb.close();
//		}
//		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//	}
//
//	// TODO: retrieveAllState,language, service,defaultvehicle,licenseNumber
//	private Boolean validate(Workbook wb, Integer clientId, Integer userId) throws IOException {
//		Boolean dirtyFile = false;
//
//		Sheet sheet = wb.getSheetAt(0);
//		Integer lastRowNum = sheet.getLastRowNum();
//		System.out.println(lastRowNum);
//
//		List<String> defaultVehicles = new ArrayList<String>();
//		List<String> licenseNumberList = new ArrayList<String>();
//
//		// To hold all unique set
//		Set<String> defualtVehicleSet = new HashSet<String>();
//		Set<String> licenseNumberSet = new HashSet<String>();
//
//		// To hold all duplicate entries
//		Set<String> duplicateDefualtVehicleSet = new HashSet<>();
//		Set<String> duplicatelicenseNumberSet = new HashSet<>();
//
//		// To hold error set
//		List<String> message = new ArrayList<>();
//		int startRow = 6;
//		Row fieldNameRow = sheet.getRow(3);
//		int stateId = 0;
//
//		for (int i = startRow; i <= lastRowNum; i++) {
//			Row currentRow = sheet.getRow(i);
//			Cell defualtVehicle = CellUtil.getCell(currentRow, ASSIGN_VEHICLE);
//			System.out.println(defualtVehicle.toString());
//			if (defualtVehicle.getCellType() != Cell.CELL_TYPE_BLANK) {
//				defaultVehicles.add(defualtVehicle.toString());
//				if (defualtVehicleSet.contains(defualtVehicle.toString())) {
//					duplicateDefualtVehicleSet.add(defualtVehicle.toString());
//				} else {
//					defualtVehicleSet.add(defualtVehicle.toString());
//				}
//			}
//			Cell licenseNumber = CellUtil.getCell(currentRow, LICENSE_NUMBER);
//			System.out.println(licenseNumber.toString());
//			if (licenseNumber.getCellType() != Cell.CELL_TYPE_BLANK) {
//				String licenseNumberValue = licenseNumber.toString();
//				licenseNumberList.add(licenseNumberValue);
//				if (licenseNumberSet.contains(licenseNumberValue)) {
//					duplicatelicenseNumberSet.add(licenseNumberValue);
//				} else {
//					licenseNumberSet.add(licenseNumberValue);
//				}
//			}
//		}
//
//		String CHECK_IF_VEHICLE_EXISTS =new StringBuilder().append(propertyConfig.getUrl()).append(DRIVER_MICROSERVICE_URL).append(DRIVER_EXISTS).append("?clientId=").append(clientId).append("&assignVehicle=").append(defaultVehicles).toString();
//		String[] duplicatedefaultVehiclesList = restTemplate.getForObject(CHECK_IF_VEHICLE_EXISTS, String[].class);
//		Set<String> duplicatedefaultVehicleDBSet = new HashSet<String>(Arrays.asList(duplicatedefaultVehiclesList));
//		// String CHECK_IF_VEHICLE_EXISTS = DRIVER_MICROSERVICE_URL +
//		// "exists?clientId=" + clientId
//		// + "&assignVehicle=" + defaultVehicles;
//		// String[] duplicatedefaultVehiclesList =
//		// restTemplate.getForObject(CHECK_IF_VEHICLE_EXISTS, String[].class);
//		// Set<String> duplicateVehicleNumberDBSet = new
//		// HashSet<String>(Arrays.asList(duplicatedefaultVehiclesList));
//		//
//		// Validate entire Row
//		for (int row = startRow; row <= lastRowNum; row++) {
//			Boolean validationFlag = false;
//			Row currentRow = sheet.getRow(row);
//
//			// VehicleNumber check
//			Cell licensdeNumberCell = CellUtil.getCell(currentRow, LICENSE_NUMBER);
//			if (licensdeNumberCell.getCellType() == Cell.CELL_TYPE_BLANK) {
//				message.add(fieldNameRow.getCell(LICENSE_NUMBER).toString());
//				validationFlag = true;
//				dirtyFile = true;
//			}
//			Cell vehicleCell = CellUtil.getCell(currentRow, ASSIGN_VEHICLE);
//			if (vehicleCell.getCellType() != Cell.CELL_TYPE_BLANK) {
//				if (duplicatedefaultVehicleDBSet.contains(vehicleCell.toString())) {
//					message.add("Vehicle : " + vehicleCell.toString() + "already mapped.");
//					validationFlag = true;
//					dirtyFile = true;
//				}
//			}
//			Cell curstateCell = CellUtil.getCell(currentRow, CUR_STATE);
//			if (curstateCell.getCellType() != Cell.CELL_TYPE_BLANK) {
//				stateId = 0; // getState(state,
//								// curstateCell.toString());
//				if (stateId == 0) {
//					message.add("State : " + curstateCell.toString() + " doesn't exist.");
//					validationFlag = true;
//					dirtyFile = true;
//				}
//			}
//
//			Cell perstateCell = CellUtil.getCell(currentRow, PER_STATE);
//			if (perstateCell.getCellType() != Cell.CELL_TYPE_BLANK) {
//				stateId = 0;// barcodeFound(state,
//							// stateCell.toString());
//				if (stateId == 0) {
//					message.add("State : " + perstateCell.toString() + " doesn't exist.");
//					validationFlag = true;
//					dirtyFile = true;
//				}
//			}
//			List<Integer> integerNumberFormatCell = new ArrayList<Integer>(
//					Arrays.asList(DRIVER_NAME, CUR_APARTMENT, CUR_STREET_NAME, CUR_AREA_NAME, CUR_LANDMARK, CUR_CITY, PER_APARTMENT, PER_STREET_NAME, PER_AREA_NAME, PER_LANDMARK, PER_CITY));
//			for (Integer fieldValue : integerNumberFormatCell) {
//				Cell fieldCell = CellUtil.getCell(currentRow, fieldValue);
//				try {
//
//					if (fieldCell.getCellType() != Cell.CELL_TYPE_BLANK)
//						;
//					fieldCell.toString();
//				} catch (Exception e) {
//					message.add("It Is Mandatory Field " + fieldNameRow.getCell(fieldValue));
//					validationFlag = true;
//					dirtyFile = true;
//				}
//			}
//			List<Integer> integerFormatCell = new ArrayList<Integer>(Arrays.asList(CONTACT_NUMBER, MANAGER_CONTACT_NUMBER, CUR_PINCODE, PER_PINCODE));
//			for (Integer fieldValue : integerFormatCell) {
//				Cell fieldCell = CellUtil.getCell(currentRow, fieldValue);
//				try {
//					if (fieldCell.getCellType() != Cell.CELL_TYPE_BLANK)
//						Integer.parseInt(fieldValue.toString());
//				} catch (Exception e) {
//					message.add("Please enter Integer value for " + fieldNameRow.getCell(fieldValue));
//					validationFlag = true;
//					dirtyFile = true;
//				}
//			}
//			Cell licenseValidityCell = CellUtil.getCell(currentRow, LICENSE_VALIDITY);
//			if (licenseValidityCell.getCellType() != Cell.CELL_TYPE_BLANK) {
//				try {
//					 DateUtility.stringToDateFormat(licenseValidityCell.toString());
//				} catch (Exception e) {
//					message.add("Wrong date format of :" + fieldNameRow.getCell(LICENSE_VALIDITY));
//					validationFlag = true;
//					dirtyFile = true;
//				}
//			}
//
//			Cell dateofbirth = CellUtil.getCell(currentRow, DATE_OF_BIRTH);
//			if (dateofbirth.getCellType() != Cell.CELL_TYPE_BLANK) {
//				try {
//					DateUtility.stringToDateFormat(dateofbirth.toString());
//				} catch (Exception e) {
//					message.add("Wrong date format of :" + fieldNameRow.getCell(DATE_OF_BIRTH));
//					validationFlag = true;
//					dirtyFile = true;
//				}
//			}
//
//			List<Integer> doubleNumberFormatCheckList = new ArrayList<Integer>(Arrays.asList(SALARY));
//			for (Integer fieldValue : doubleNumberFormatCheckList) {
//				Cell fieldCell = CellUtil.getCell(currentRow, fieldValue);
//				try {
//					if (fieldCell.getCellType() != Cell.CELL_TYPE_BLANK)
//						Double.parseDouble(fieldValue.toString());
//				} catch (Exception e) {
//					message.add("Please enter Decimal value for " + fieldNameRow.getCell(fieldValue));
//					validationFlag = true;
//					dirtyFile = true;
//				}
//			}
//
//			if (validationFlag) {
//				Cell validationCell = currentRow.createCell(ERROR_MSG);
//				validationCell = fillCell(wb, validationCell, message.toString(), IndexedColors.RED.getIndex());
//
//			} else {
//				Cell validationCell = currentRow.createCell(ERROR_MSG);
//				validationCell = fillCell(wb, validationCell, "Validated", IndexedColors.GREEN.getIndex());
//			}
//		}
//
//		// To mark duplicates within excel
//		for (int row = startRow; row <= lastRowNum; row++) {
//			Row currentRow = sheet.getRow(row);
//			String licenseNumberValue = currentRow.getCell(LICENSE_NUMBER).toString();
//			String defaultVehicleValue = currentRow.getCell(ASSIGN_VEHICLE).toString();
//			Cell validationCell = currentRow.getCell(ERROR_MSG);
//
//			if (duplicatelicenseNumberSet.contains(licenseNumberValue)) {
//				validationCell = fillCell(wb, validationCell, "Duplicate license number in excel", IndexedColors.RED.getIndex());
//				dirtyFile = true;
//			}
//			if (duplicateDefualtVehicleSet.contains(defaultVehicleValue)) {
//				validationCell = fillCell(wb, validationCell, "Duplicate assign vehicle in excel", IndexedColors.RED.getIndex());
//				dirtyFile = true;
//			}
//		}
//		// "/Uploads" + File.separator + "Loginext_Vehicle_Upload_Format" +
//		// clientId + "_" + userId + ".xls"
//
//		try {
//			if (!dirtyFile) {
//				FileOutputStream bos = new FileOutputStream(new File("/Users/vishwanath/Downloads/vishwa123.xls"));
//				wb.write(bos);
//				bos.close();
//			}
//		} catch (Exception e) {
//			System.out.println(e);
//		} finally {
//			wb.close();
//		}
//		return dirtyFile;
//
//	}
//
//	private Cell fillCell(Workbook wb, Cell cell, String cellValue, short backgroundColourIndex) {
//		Font font = wb.createFont();
//		font.setBold(true);
//		font.setFontName("Arial");
//
//		CellStyle style = wb.createCellStyle();
//		style.setFillForegroundColor(backgroundColourIndex);
//		style.setFillBackgroundColor(backgroundColourIndex);
//		style.setFont(font);
//		cell.setCellStyle(style);
//		cell.setCellValue(cellValue + "," + cell.toString());
//		return cell;
//	}
//
//	public Workbook create(InputStream inp) throws Exception {
//		if (!inp.markSupported()) {
//			inp = new PushbackInputStream(inp, 8);
//		}
//		if (POIFSFileSystem.hasPOIFSHeader(inp)) {
//			logger.error("2003 and below");
//			return new HSSFWorkbook(inp);
//		}
//		if (POIXMLDocument.hasOOXMLHeader(inp)) {
//			logger.error("2007 and above");
//			return new XSSFWorkbook(OPCPackage.open(inp));
//		}
//		logger.error("Your version of Excel is not POI analysis");
//		return null;
//	}
//
//	@RequestMapping(value = "/download", method = RequestMethod.POST)
//	public void downloadExcel(HttpServletRequest request, HttpServletResponse response) {
//
//		String path = request.getSession().getServletContext().getRealPath("/Uploads");
//		// String
//		path = "/Users/priyankshivhare/Documents/branch_workspace/TrackAPack_super/src/main/webapp/Uploads";
//		String filename;
//
//		int fileType = 2;// Integer.parseInt(request.getParameter("file"));
//
//		String name = "Loginext_Driver_Upload_Format.xls";
//		if (fileType == 1)
//			filename = "Loginext_Driver_Upload_Format.xls";
//		else {
//
//			//int clientId = 24;//
//			int clientId=Integer.parseInt(request.getSession().getAttribute("clientId").toString());
//			String loggedInUser = "12";// (String)(request.getSession().getAttribute("userid"));
//			int userId = Integer.parseInt(loggedInUser);
//			filename = "Loginext_Driver_Upload_Format" + clientId + "_" + userId + ".xls";
//		}
//
//		try {
//			OutputStream out = response.getOutputStream();
//			FileInputStream in = new FileInputStream(path + File.separator + filename);
//
//			response.setContentType("application/vnd.ms-excel");
//			response.addHeader("content-disposition", "attachment; filename=" + name);
//
//			int octet;
//			while ((octet = in.read()) != -1)
//				out.write(octet);
//
//			in.close();
//			out.close();
//			if (fileType != 1) {
//				File file = new File(path + File.separator + filename);
//				file.delete();
//			}
//		} catch (Exception e) {
//			logger.error("" + e);
//		}
//
//	}

}