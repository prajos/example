package com.loginext.ms.driver.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.loginext.commons.aspect.CurrentSession;
import com.loginext.commons.aspect.Log;
import com.loginext.commons.entity.Session;
import com.loginext.commons.model.AddressDTO;
import com.loginext.commons.model.DriverContactUpdateDTO;
import com.loginext.commons.model.DriverContactValidationDTO;
import com.loginext.commons.model.DriverCreateDTO;
import com.loginext.commons.model.DriverDTO;
import com.loginext.commons.model.GenericTabularDTO;
import com.loginext.commons.model.PageDTO;
import com.loginext.commons.model.RequestDeliveryMediumDTO;
import com.loginext.commons.model.ResourceTrackingMapDTO;
import com.loginext.commons.model.ShiftDTO;
import com.loginext.commons.model.VehicleStatusDTO;
import com.loginext.commons.util.Constants;
import com.loginext.commons.util.Slf4jUtility;
import com.loginext.commons.util.Util;
import com.loginext.ms.driver.manager.DriverManager;
import com.loginext.ms.driver.service.DriverService;

@RequestMapping("/driver")
@Controller
public class DriverController {

	private static @Log Logger logger;

	@Autowired
	private DriverService driverService;
	
	@Autowired
	private DriverManager driverManager;
	
	@RequestMapping(value = "/getdriverids", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Integer>> getDriverIds(@RequestBody List<String> referenceIds) {
		logger.debug("Inside getDriverIds for referenceIds: " + referenceIds);
		
		List<Integer> driverIds = new ArrayList<Integer>();
		if(referenceIds!=null&&referenceIds.size()>0){
			List<DriverDTO> driverDTOs = driverService.getDriverIds(referenceIds);
			driverIds = driverDTOs.stream().filter(d -> d.getDriverId() != null).map(d -> d.getDriverId()).collect(Collectors.toList());
		}
		return new ResponseEntity<List<Integer>>(driverIds,HttpStatus.OK);
	}

	@RequestMapping(value = "/list/{client_id}", method = RequestMethod.GET)
	public  ResponseEntity<GenericTabularDTO> getDrivers(@CurrentSession Session session,@RequestParam (required=false)String dashboardStatus, PageDTO page) {
 		try {
 			String token = session.getToken();
 			logger.info("Executing service layer DriverController.getDrivers for session: [{}] on: [{}]", token, new Date());
 			GenericTabularDTO tabluarDTO = driverManager.getDrivers(session, page,dashboardStatus);
 			logger.info("Executed  service layer DriverController.getDrivers for session: [{}] on: [{}]", token, new Date());
 			return new ResponseEntity<GenericTabularDTO>(tabluarDTO,HttpStatus.OK);
 		} catch (Exception ex) {
 			logger.error(Util.errorMessage, ex);
 		}
 		return new ResponseEntity<GenericTabularDTO>(HttpStatus.INTERNAL_SERVER_ERROR);
 	}
	
	@RequestMapping(value = "/download/{client_id}", method = RequestMethod.GET)
	public  ResponseEntity<List<DriverDTO>> downloadExcelReport(@CurrentSession Session session,@RequestParam(required=false)String dashboardStatus, PageDTO page) {
 		try {
 			String token = session.getToken();
 			logger.info("Executing service layer DriverController.downloadExcelReport for session: [{}] on: [{}]", token, new Date());
 			GenericTabularDTO tabluarDTO = driverManager.getDrivers(session, page,dashboardStatus);
 			logger.info("Executed  service layer DriverController.downloadExcelReport for session: [{}] on: [{}]", token, new Date());
 			return new ResponseEntity<List<DriverDTO>>((List<DriverDTO>)tabluarDTO.getResults(),HttpStatus.OK);
 		} catch (Exception ex) {
 			logger.error(Util.errorMessage, ex);
 		}
 		return new ResponseEntity<List<DriverDTO>>(HttpStatus.INTERNAL_SERVER_ERROR);
 	}
	
	
	@RequestMapping(value = "/update", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> updateDrivers(@RequestBody List<DriverDTO> drivers, @CurrentSession Session session) {
		try {
			if (drivers != null && drivers.size() > 0) {
				
			  //condition to make sure the code piece runs only for API	
			  if(drivers.get(0).getReferenceId()!=null){
				List<String> referenceIds = drivers.stream().filter(d -> d.getReferenceId() != null).map(d -> d.getReferenceId()).collect(Collectors.toList());
				List<DriverDTO> driverDTOs = driverService.getDriverIds(referenceIds);
				
				//case when driver ids are not found
				if(driverDTOs.isEmpty()){
					logger.info("Driver Ids not found for given reference ids.");
					return new ResponseEntity<Boolean>(Boolean.FALSE,HttpStatus.OK);
				}
				
				HashMap<String,Integer> referenceIdDTOMap = new HashMap<String,Integer>();
				for(DriverDTO dto: driverDTOs){
					referenceIdDTOMap.put(dto.getReferenceId(), dto.getDriverId());
				}
				for(DriverDTO dto: drivers){
					dto.setDriverId(referenceIdDTOMap.get(dto.getReferenceId()));
				}
			  }
				
	 			logger.info("Executing service layer DriverController.updateDrivers with size : [{}] on: [{}]", drivers.size(), new Date());
				Boolean isupdated = driverManager.updateDrivers(drivers, session);
	 			logger.info("Executed service layer DriverController.updateDrivers  : [{}] on: [{}]",isupdated, new Date());
				return new ResponseEntity<Boolean>(isupdated,isupdated==true? HttpStatus.OK:HttpStatus.INTERNAL_SERVER_ERROR);
			}

		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/get/{driver_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<DriverDTO> getByDriverId(@CurrentSession Session session, @PathVariable(value="driver_id") Integer driverId,@RequestParam(required=false,defaultValue=Constants.ALL) String fetchType) {
		try {
			logger.info("Executing service layer DriverController.getByDriverId [{}] on: [{}]",driverId.toString() ,new Date());
			DriverDTO driver = driverManager.getByDriverId( driverId, session,fetchType); 
			logger.info("Executed service layer DriverController.getByDriverId  on: [{}]" ,new Date());
			return new ResponseEntity<DriverDTO>(driver, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		
	}
	
	@RequestMapping(value = "/getbyreferenceids", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<DriverDTO>> getByReferenceIds(@CurrentSession Session session,@RequestBody List<String> referenceIds) {
		try {
			List<DriverDTO> drivers = new ArrayList<DriverDTO>();
			List<DriverDTO> driverDTOs = driverService.getDriverIds(referenceIds);
			List<Integer> driverIds = driverDTOs.stream().filter(d -> d.getDriverId() != null).map(d -> d.getDriverId()).collect(Collectors.toList());
			Integer clientId=session.getClientId().intValue();
			logger.info("Executing service layer DriverController getDriverIds [{}] on: [{}]",driverIds.toString() ,new Date());
			
			if(driverIds.isEmpty())
				return new ResponseEntity<List<DriverDTO>>(drivers, HttpStatus.OK);
			drivers = driverService.getByDriverIds(driverIds, clientId); 
			logger.info("Executed service layer DriverController getDriverIds  on: [{}]. Found [{}] drivers" ,new Date(),drivers!=null?drivers.size():-1);
			return new ResponseEntity<List<DriverDTO>>(drivers, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		
	}
	
	
	@RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<DriverCreateDTO>> createDrivers(@RequestBody List<DriverDTO> drivers,@CurrentSession Session session) {

		try {
 			logger.info("Executing service layer DriverController.createDrivers on: [{}]", new Date());
 			
 			if (drivers != null && drivers.size() > 0) {
				List<DriverCreateDTO> referenceIds =driverService.createDrivers(drivers);
	 			logger.info("Executed service layer DriverController.createDrivers with size [{}] on: [{}]", referenceIds.size(), new Date());
				return new ResponseEntity<List<DriverCreateDTO>>(referenceIds,HttpStatus.OK);
			}
 			
			/*if (drivers != null && drivers.size() > 0) {
				Boolean isDriverCreated=driverService.createDrivers(drivers);
	 			logger.info("Executed service layer DriverController.createDrivers [{}] on: [{}]",isDriverCreated.toString(), new Date());
				return new ResponseEntity<Boolean>(isDriverCreated,isDriverCreated==true? HttpStatus.OK:HttpStatus.INTERNAL_SERVER_ERROR);
			}*/
		} catch (Exception e) {
			logger.error(Util.errorMessage, e);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

	}

	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	public ResponseEntity<Boolean> deleteDriver(@RequestBody List<Integer> driverIds,@CurrentSession Session session) {
		try{
		logger.info("Executing service layer DriverController.deleteDriver [{}] on: [{}]",driverIds.toString(), new Date());
		if (driverIds != null && driverIds.size() > 0) {
			Boolean isDriverDeleted=driverService.deleteDriver(driverIds, session);
			logger.info("Executed service layer DriverController.deleteDriver [{}] on: [{}]",isDriverDeleted, new Date());
			return new ResponseEntity<Boolean>(isDriverDeleted,isDriverDeleted==true? HttpStatus.OK:HttpStatus.INTERNAL_SERVER_ERROR);
		}} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/exists", method = RequestMethod.GET)
	public ResponseEntity<List<String>> checkIfExistsPhoneNumner(@RequestParam Integer clientId, @RequestParam List<String> phoneNumber,@RequestParam(required=false) List<String> referenceIds) {
		try {
			logger.info("Executing service layer DriverController.checkIfExistsPhoneNumner [{}] on: [{}]",phoneNumber, new Date());
			List<String> duplicatePhoneNumbers = driverService.checkIfExistsByPhoneNumber(clientId,phoneNumber,referenceIds);
			logger.info("Executed service layer DriverController.checkIfExistsPhoneNumner [{}] on: [{}]",duplicatePhoneNumbers.size(), new Date());
			return new ResponseEntity<List<String>>(duplicatePhoneNumbers, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/exist/contact", method = RequestMethod.POST)
	public ResponseEntity<List<String>> checkIfContactNumberExisted(@CurrentSession Session session,@RequestBody DriverContactValidationDTO driverContactValidationDTO) {
		try {
			logger.info("Executing service layer DriverController.checkIfContactNumberExisted [{}] on: [{}]",driverContactValidationDTO.getPhoneNumbers(), new Date());
			List<String> duplicatePhoneNumbers = driverService.checkIfContactNumberAlreadyExist(session, driverContactValidationDTO);
			logger.info("Executed service layer DriverController.checkIfContactNumberExisted [{}] on: [{}]",duplicatePhoneNumbers.size(), new Date());
			return new ResponseEntity<List<String>>(duplicatePhoneNumbers, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	/**
	 * Retrieves duplicate inactive vehicles number for exists check.
	 * 
	 * 
	 * @param clientId
	 * @param vehicleNumber
	 * @return
	 */
	@RequestMapping(value = "/inactive/exists", method = RequestMethod.GET)
	public ResponseEntity<List<String>> checkIfInactiveExists(@RequestParam Integer clientId,
			@RequestParam List<String> phoneNumber,@RequestParam(required=false)List<Integer> driverIds) {
		if (phoneNumber != null && !phoneNumber.equals("")) {
			logger.info("Start EXISTS checks for driver [DRIVER SERVICE]: {} ",Slf4jUtility.toObjArr(phoneNumber.toString()));
			if(driverIds == null){
				driverIds = new ArrayList<Integer>();
			}
			List<String> duplicatePhoneNumberList = driverService.checkIfInactiveExists(clientId, phoneNumber);
			logger.info("End EXISTS check for driver [DRIVER SERVICE]: {}",Slf4jUtility.toObjArr("duplicate vehicle nos: "+duplicatePhoneNumberList!=null?duplicatePhoneNumberList.toString():null));
			return new ResponseEntity<List<String>>(duplicatePhoneNumberList, HttpStatus.OK);
		}
		return new ResponseEntity<List<String>>(HttpStatus.BAD_REQUEST);
	}
	
	/**
	 * Retrieves duplicate in transit vehicles.
	 * 
	 * 
	 * @param clientId
	 * @param vehicleNumber
	 * @return
	 */
	@RequestMapping(value = "/intransit/exists", method = RequestMethod.GET)
	public ResponseEntity<List<String>> checkIfIntransitExists(@RequestParam Integer clientId,
			@RequestParam List<String> phoneNumber,@RequestParam(required=false)List<Integer> driverIds) {
		if (phoneNumber != null && !phoneNumber.equals("")) {
			logger.info("Start EXISTS checks for driver [DRIVER SERVICE]: {} ",Slf4jUtility.toObjArr(phoneNumber.toString()));
			if(driverIds == null){
				driverIds = new ArrayList<Integer>();
			}
			List<String> duplicatePhoneNumberList = driverService.checkIfIntransitExists(clientId, phoneNumber);
			logger.info("End EXISTS check for driver [DRIVER SERVICE]: {}",Slf4jUtility.toObjArr("duplicate vehicle nos: "+duplicatePhoneNumberList!=null?duplicatePhoneNumberList.toString():null));
			return new ResponseEntity<List<String>>(duplicatePhoneNumberList, HttpStatus.OK);
		}
		return new ResponseEntity<List<String>>(HttpStatus.BAD_REQUEST);
	}

	/**
	 * Retrieves duplicate in transit vehicles.
	 * 
	 * 
	 * @param clientId
	 * @param vehicleNumber
	 * @return
	 */
	@RequestMapping(value = "/otherbranch/exists", method = RequestMethod.GET)
	public ResponseEntity<List<String>> checkIfOtherbranchExists(@RequestParam Integer clientId, @RequestParam Integer clientBranchId,
			@RequestParam List<String> phoneNumber,@RequestParam(required=false)List<Integer> driverIds) {
		if (phoneNumber != null && !phoneNumber.equals("")) {
			logger.info("Start EXISTS checks for driver [DRIVER SERVICE]: {} ",Slf4jUtility.toObjArr(phoneNumber.toString()));
			if(driverIds == null){
				driverIds = new ArrayList<Integer>();
			}
			List<String> duplicatePhoneNumberList = driverService.checkIfOtherBranchExists(clientId, clientBranchId, phoneNumber);
			logger.info("End EXISTS check for driver [DRIVER SERVICE]: {}",Slf4jUtility.toObjArr("duplicate vehicle nos: "+duplicatePhoneNumberList!=null?duplicatePhoneNumberList.toString():null));
			return new ResponseEntity<List<String>>(duplicatePhoneNumberList, HttpStatus.OK);
		}
		return new ResponseEntity<List<String>>(HttpStatus.BAD_REQUEST);
	}
	

	@RequestMapping(value = "/exists/license/{client_id}", method = RequestMethod.GET)
	public ResponseEntity<List<String>> checkIfExistsLicenseNumber(@RequestParam List<String> licenseNumber,@PathVariable(value="client_id") Integer clientId,@RequestParam(required=false)
			 List<String> referenceIds) {
		try {
			logger.info("Executing service layer DriverController.checkIfExistsLicenseNumber [{}] on: [{}]",licenseNumber, new Date());
			List<String> existsLicenseNumbers = driverService.checkIfExistsLicenseNumber(licenseNumber,clientId,referenceIds);
			logger.info("Executed service layer DriverController.checkIfExistsLicenseNumber [{}] on: [{}]",existsLicenseNumbers.size(), new Date());
			return new ResponseEntity<List<String>>(existsLicenseNumbers, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/exists/employeeid/{client_id}", method = RequestMethod.GET)
	public ResponseEntity<List<String>> checkIfExistsEmployeeId(@RequestParam List<String> employeeIds,@PathVariable(value="client_id") Integer clientId,
			@RequestParam(required=false) List<String> referenceIds) {
		try {
			logger.info("Executing service layer DriverController.checkIfExistsEmployeeId [{}] on: [{}]",employeeIds, new Date());
			List<String> existsEmpIds = driverService.checkIfExistsEmployeeId(employeeIds,clientId,referenceIds);
			logger.info("Executed service layer DriverController.checkIfExistsEmployeeId [{}] on: [{}]",existsEmpIds.size(), new Date());
			return new ResponseEntity<List<String>>(existsEmpIds, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/shifts/list", method = RequestMethod.GET)
	public ResponseEntity<List<ShiftDTO>> getShifts(@RequestParam Integer driverId) {
		try {
			logger.info("Executing service layer DriverController.getShifts [{}] on: [{}]",driverId.toString(), new Date());
			if (driverId != null) {
				List<ShiftDTO> shifts = driverService.getShifts(driverId);
				logger.info("Executed service layer DriverController.getShifts [{}] on: [{}]",shifts.size(), new Date());
				return new ResponseEntity<List<ShiftDTO>>(shifts, HttpStatus.OK);
			}

		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/update/attendance", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> updateAttendance(@RequestBody DriverDTO driverDTO, @CurrentSession Session session) {
		try {
			logger.info("Executing service layer DriverController.updateAttendance on: [{}]", new Date());
			if (driverDTO != null) {
				Boolean isAttendanceUpdated = driverService.updateAttendance(driverDTO, session);
				logger.info("Executed service layer DriverController.updateAttendance [{}] on: [{}]",isAttendanceUpdated.toString(), new Date());
				return new ResponseEntity<Boolean>(isAttendanceUpdated,isAttendanceUpdated==true? HttpStatus.OK:HttpStatus.INTERNAL_SERVER_ERROR);
			}
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<Boolean>(Boolean.FALSE, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@RequestMapping(value = "/getLicenseExpiredDrivers", method = RequestMethod.GET,produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<DriverDTO>> getLicenseExpiredDrivers() {
		try {
 			logger.info("Start getLicenseExpiredDrivers on [{}]",new Date());
 				List<DriverDTO> expiredDrivers = driverService.findLicenseExpiredDrivers(); 				
 				logger.info("End getLicenseExpiredDrivers :[{}] on [{}] " , expiredDrivers.size(), new Date());
 				return new ResponseEntity<List<DriverDTO>>(expiredDrivers,HttpStatus.OK);
 		} catch (Exception ex) {
 			logger.error(Util.errorMessage, ex);
 		}
		return new ResponseEntity<List<DriverDTO>>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/updateLastLicenseAlertSentDt", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> updateLastLicenseAlertSentDt(@RequestParam("driverIds") List<Integer> driverIds,@CurrentSession Session session) {
		try{
		if (driverIds != null && driverIds.size()>0) {
			logger.info("Start updateLastLicenseAlertSentDt :[{}] on [{}] " , driverIds.size(), new Date());
			Boolean isDriverUpdated = driverService.updateLastLicenseAlertSentDt(driverIds, session);
			logger.info("Executed service layer DriverController.updateLastLicenseAlertSentDt [{}] on: [{}]",isDriverUpdated.toString(), new Date());
			return new ResponseEntity<Boolean>(isDriverUpdated,isDriverUpdated==true? HttpStatus.OK:HttpStatus.INTERNAL_SERVER_ERROR);
		}
		}catch(Exception ex){
 			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<Boolean>(Boolean.FALSE,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/getbyvehicleids", method = RequestMethod.GET)
	public ResponseEntity<List<RequestDeliveryMediumDTO>> getDriverDetailsByVehicleIds(@RequestParam List<Integer> vehicleIds) {
		try {
			logger.info("Executing service layer DriverController.getbyvehicleids [{}] on: [{}]",vehicleIds.toString(), new Date());
			if (vehicleIds != null && vehicleIds.size() >0) {
				List<RequestDeliveryMediumDTO> driverDetails = driverService.getByVehicleIds(vehicleIds);
				logger.info("Executed service layer DriverController.getbyvehicleids [{}] on: [{}]",driverDetails.size(), new Date());
				return new ResponseEntity<List<RequestDeliveryMediumDTO>>(driverDetails, HttpStatus.OK);
			}
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/update/fmlm/attendance", method = RequestMethod.PUT)
	public ResponseEntity<Boolean> updateAttendanceForFmlm(@RequestBody List<DriverDTO> driverDTOs, @CurrentSession Session session) {
		try {
			Boolean isUpdated=false;
			logger.info("Executing service layer DriverController.updateAttendanceForFmlm on: [{}]", new Date());
			if (null != driverDTOs)
			{
				isUpdated = driverService.updateAttendanceForFmlm(driverDTOs,session);
			}
			logger.info("Executed service layer DriverController.updateAttendanceForFmlm on: [{}]", new Date());
			return new ResponseEntity<Boolean>(isUpdated,isUpdated==true? HttpStatus.OK:HttpStatus.INTERNAL_SERVER_ERROR);
			}
		catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<Boolean>(Constants.FALSE,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/update/fmlm/activestate", method = RequestMethod.PUT)
	public ResponseEntity<Boolean> updateActiveStatusForFmlm(@RequestBody List<DriverDTO> driverDTOs,@CurrentSession Session session) {
		try {
			Boolean isUpdated=false;
			logger.info("Executing service layer DriverController.updateActiveStatusForFmlm on: [{}]", new Date());
			if (null != driverDTOs)
			{
				isUpdated = driverService.updateActiveStatusForFmlm(driverDTOs, session);
			}
			logger.info("Executed service layer DriverController.updateActiveStatusForFmlm on: [{}]", new Date());
			return new ResponseEntity<Boolean>(isUpdated,isUpdated==true? HttpStatus.OK:HttpStatus.INTERNAL_SERVER_ERROR);
			}
		catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<Boolean>(Constants.FALSE,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/update/list", method = RequestMethod.PUT)
	public ResponseEntity<Boolean> updateListDrivers(@RequestBody List<DriverDTO> driverDTOs, @CurrentSession Session session) {
		try {
			Boolean isUpdated=false;
			logger.info("Executing service layer DriverController.updateDrivers FMLM List on: [{}]", new Date());
			if (null != driverDTOs)
			{
				isUpdated = driverService.updateDriversList(driverDTOs, session);
			}
			logger.info("Executed service layer DriverController.updateDrivers FMLM List on: [{}]", new Date());
			return new ResponseEntity<Boolean>(isUpdated,isUpdated==true? HttpStatus.OK:HttpStatus.INTERNAL_SERVER_ERROR);
			}
		catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<Boolean>(Constants.FALSE,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/address", method = RequestMethod.GET,produces=MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<AddressDTO> findDefaultDriverAddressByClientId(@CurrentSession Session session) {
		try {
 			logger.info("Start findDefaultDriverAddressByClientId on [{}]",new Date());
 				AddressDTO defaultAddress = driverService.findDefaultDriverAddressByClientId(session.getClientId().intValue()); 				
 				logger.info("End findDefaultDriverAddressByClientId : on [{}] " , new Date());
 				return new ResponseEntity<AddressDTO>(defaultAddress,HttpStatus.OK);
 		} catch (Exception ex) {
 			logger.error(Util.errorMessage, ex);
 		}
		return new ResponseEntity<AddressDTO>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/list/available", method = RequestMethod.GET)
	public @ResponseBody ResponseEntity<List<ResourceTrackingMapDTO>> getDriversByClientIdAndBranches(@CurrentSession Session session) {
		try {
			String token = session.getToken();
			logger.info("Executing DriverController.getDriversByClientIdAndBranches for session: [{}] on: [{}]", token, new Date());
			List<ResourceTrackingMapDTO> resourceTrackingMapDTOs = driverService.getDriversByClientIdAndBranches(session);
			if (null != resourceTrackingMapDTOs && resourceTrackingMapDTOs.size() > 0) {
				resourceTrackingMapDTOs = driverService.fillAvailableDriverTracking(resourceTrackingMapDTOs);
			}
			logger.info("Executed DriverController.getDriversByClientIdAndBranches for session: [{}] on: [{}]", token, new Date());
			return new ResponseEntity<List<ResourceTrackingMapDTO>>(resourceTrackingMapDTOs, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<List<ResourceTrackingMapDTO>>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/status", method = RequestMethod.PUT)
	public @ResponseBody ResponseEntity<Boolean> updateDriverStatus(@RequestBody VehicleStatusDTO driverStatusDTO) {
		try {
			logger.info("Executing DriverController.updateDriverStatus on: [{}]", new Date());
			Boolean isStatusUpdated = driverService.updateDriverStatus(driverStatusDTO);
			logger.info("Executed DriverController.updateDriverStatus on: [{}]", new Date());
			return new ResponseEntity<Boolean>(isStatusUpdated, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<Boolean>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/validate/contact", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<List<Integer>> validateDriverContact(@CurrentSession Session session,@RequestBody List<String> phoneNumbers) {
		List<Integer> dupContactDriverIds = new ArrayList<>();
		try {
			logger.info("Executing DriverController.validateDriverContact on: [{}]", new Date());
			dupContactDriverIds = driverService.ValidateContactOnStartTrip(session,phoneNumbers);
			logger.info("Executed DriverController.validateDriverContact on: [{}]", new Date());
			return new ResponseEntity<List<Integer>>(dupContactDriverIds, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<List<Integer>>(dupContactDriverIds,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/validate/contact/intransit", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<List<Integer>> validateContactOfIntransitDriver(@CurrentSession Session session,@RequestBody DriverContactUpdateDTO driverContactUpdateDTO) {
		List<Integer> dupContactDriverIds = new ArrayList<>();
		try {
			logger.info("Executing DriverController.validateContactOfIntransitDriver on: [{}]", new Date());
			dupContactDriverIds = driverService.validateContactForIntransitDrivers(session,driverContactUpdateDTO);
			logger.info("Executed DriverController.validateContactOfIntransitDriver on: [{}]", new Date());
			return new ResponseEntity<List<Integer>>(dupContactDriverIds, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<List<Integer>>(dupContactDriverIds,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/validate/contact/other", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<List<Integer>> validateContactForOtherDrivers(@CurrentSession Session session,@RequestBody DriverContactUpdateDTO driverContactUpdateDTO) {
		List<Integer> dupContactDriverIds = new ArrayList<>();
		try {
			logger.info("Executing DriverController.validateContactForOtherDrivers on: [{}]", new Date());
			dupContactDriverIds = driverService.validateContactForOtherDrivers(session,driverContactUpdateDTO);
			logger.info("Executed DriverController.validateContactForOtherDrivers on: [{}]", new Date());
			return new ResponseEntity<List<Integer>>(dupContactDriverIds, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<List<Integer>>(dupContactDriverIds,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/intransit/contact", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<List<String>> getIntransitDriverContactNumbers(@CurrentSession Session session,@RequestBody List<String> phoneNumbers) {
		List<String> intransitDriverContact = new ArrayList<>();
		try {
			logger.info("Executing DriverController.validateDriverContact on: [{}]", new Date());
			intransitDriverContact = driverService.getIntransitDriverContactNumber(session,phoneNumbers);
			logger.info("Executed DriverController.validateDriverContact on: [{}]", new Date());
			return new ResponseEntity<List<String>>(intransitDriverContact, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<List<String>>(intransitDriverContact,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/validate/contact/byid", method = RequestMethod.POST)
	public @ResponseBody ResponseEntity<List<Integer>> validateDriverContactByDriverId(@CurrentSession Session session,@RequestBody List<Integer> driverIds) {
		List<Integer> dupContactDriverIds = new ArrayList<>();
		try {
			logger.info("Executing DriverController.validateDriverContact on: [{}]", new Date());
			List<String> driverPhoneNumbers = driverService.getContactNumberByDriverId(session,driverIds);
			if(null != driverPhoneNumbers && driverPhoneNumbers.size() > 0){
				dupContactDriverIds = driverService.ValidateContactOnStartTrip(session,driverPhoneNumbers);
			}
			logger.info("Executed DriverController.validateDriverContact on: [{}]", new Date());
			return new ResponseEntity<List<Integer>>(dupContactDriverIds, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<List<Integer>>(dupContactDriverIds,HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/contact", method = RequestMethod.PUT)
	public @ResponseBody ResponseEntity<Boolean> updateDriverContactNumber(@CurrentSession Session session,@RequestBody List<DriverContactUpdateDTO> driverContactUpdateDTO) {
		Boolean isUpdated = false;
		try {
			logger.info("Executing DriverController.updateDriverContactNumber on : [{}] for token : [{}]", new Date(),session.getToken());
			if(null != driverContactUpdateDTO && driverContactUpdateDTO.size() > 0){
				isUpdated = driverManager.updateDriverContactNumber(session, driverContactUpdateDTO);
			}
			logger.info("Executed DriverController.updateDriverContactNumber on: [{}] for token : [{}]", new Date(),session.getToken());
			return new ResponseEntity<Boolean>(isUpdated, HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<Boolean>(isUpdated,HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
