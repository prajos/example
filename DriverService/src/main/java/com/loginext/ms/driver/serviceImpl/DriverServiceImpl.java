package com.loginext.ms.driver.serviceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.loginext.commons.aspect.Log;
import com.loginext.commons.aspect.PropertyConfig;
import com.loginext.commons.entity.Address;
import com.loginext.commons.entity.Clientmaster;
import com.loginext.commons.entity.Drivermaster;
import com.loginext.commons.entity.Session;
import com.loginext.commons.entity.Trackingrecord;
import com.loginext.commons.entity.Vehiclemaster;
import com.loginext.commons.enums.ModuleTypeEnum;
import com.loginext.commons.model.AddressDTO;
import com.loginext.commons.model.DriverContactUpdateDTO;
import com.loginext.commons.model.DriverContactValidationDTO;
import com.loginext.commons.model.DriverCreateDTO;
import com.loginext.commons.model.DriverDTO;
import com.loginext.commons.model.GenericTabularDTO;
import com.loginext.commons.model.MediaDTO;
import com.loginext.commons.model.PageDTO;
import com.loginext.commons.model.RemoveMediaDTO;
import com.loginext.commons.model.RequestDeliveryMediumDTO;
import com.loginext.commons.model.ResourceLanguageMapDTO;
import com.loginext.commons.model.ResourceTrackingMapDTO;
import com.loginext.commons.model.ShiftDTO;
import com.loginext.commons.model.VehicleStatusDTO;
import com.loginext.commons.util.Constants;
import com.loginext.commons.util.Slf4jUtility;
import com.loginext.commons.util.Util;
import com.loginext.ms.driver.criteria.DriverCustomSearch;
import com.loginext.ms.driver.repository.AddressRepository;
import com.loginext.ms.driver.repository.DriverRepository;
import com.loginext.ms.driver.repository.ResourceLanguageMapRepository;
import com.loginext.ms.driver.service.DriverService;

@Service
@Transactional
public class DriverServiceImpl implements DriverService, Constants {

	@Autowired
	private DriverRepository driverRepository;
	@Autowired
	private AddressRepository addressRepository;
	@Autowired
	private ResourceLanguageMapRepository resourceLanguageMapRepository;
	@Autowired
	private ShiftServiceImpl shiftService;
	@Autowired
	private LanguageServiceImpl langaugeService;
	@Autowired
	private DriverCustomSearch driverCustomSearch;

	private static @Log Logger logger;

	@Autowired
	private RestTemplate restTemplate;
	@Autowired
	private PropertyConfig propertyConfig;
	@Value("${media.service}")
	private String MEDIA_MICROSERVICE_URL;
	@Value("${trip.service}")
	private String TRIP_MICROSERVICE_URL;
	@Value("${tracking.service}")
	private String TRACKING_MICROSERVICE_URL;
	@Value("${media.create}")
	private String MEDIA_CREATE;
	@Value("${trip.driver.get}")
	private String TRIP_DRIVER_GET;
	@Value("${tracking.resource}")
	private String TRACKING_DRIVER;
	@Value("${media.driver.get.all}")
	private String MEDIA_GET_ALL;
	@Value("${media.remove.list}")
	private String MEDIA_REMOVE_LIST;
	@Value("${trip.driver.update}")
	private String TRIP_DRIVER_UPDATE;
	@Value("${vehicle.service}")
	private String VEHICLE_MICROSERVICE_URL;
	@Value("${resource.vehicle.get}")
	private String GET_VEHCILE;
	@Value("${client.service}")
	private String CLIENT_MICROSERVICE_URL;
	@Value("${clientproperty.service.getByClientIdAndPropKeys}")
	private String CLIENT_PROPERTY;
	@Value("${tracking.vehicle.list}")
	private String AVAILABLE_DRIVER_TRACKING;

	// /**

	// * get drivers by client Id
	// */
	@Override
	public GenericTabularDTO getDrivers(Session session, PageDTO page,String dashboardStatus) {
		GenericTabularDTO tabluarDTO = driverCustomSearch.bulidSearchCriteria(session, page,dashboardStatus);
		return tabluarDTO;
	}

	/**
	 * update drivers in batch
	 */
	private Boolean updateDriverList(List<DriverDTO> driverDTOs, List<Integer> driverIds, Session session) {
		List<Drivermaster> driverMasters = new ArrayList<>();
		logger.debug("Start updatedrivers Query", new Date());

		List<Drivermaster> drivers = driverRepository.getByDriverIds(driverIds);
		if (drivers != null && drivers.size() == 1) {
			driverMasters = convertDTOsToEntitiesOnUpdate(driverDTOs, drivers, session);
		} else {
			driverMasters = convertDTOsToEntitiesOnBulkUpdate(driverDTOs, drivers, session);

		}
		if (driverMasters != null && driverMasters.size() > 0) {
			driverRepository.save(driverMasters);
			logger.debug("End updatedrivers Query", new Date());
			return true;
		}
		return false;
	}

	// @Override
	// public Boolean updateDrivers(List<DriverDTO> driverDTOs, List<Integer>
	// driverIds) {
	//
	// List<Drivermaster> drivers = driverRepository.getByDriverIds(driverIds);
	//
	// if (driverMasters != null && driverMasters.size() > 0) {
	// List<Drivermaster> driverEntities = driverRepository.save(driverMasters);
	// if (driverEntities != null && driverEntities.size() > 0) {
	// return true;
	// }
	// }
	// return false;
	// }

	/**
	 * create drivers in batch
	 * 
	 * @return
	 */
	private List<DriverDTO> createDriverList(List<DriverDTO> driverMasterDTO, List<Address> addresses) {
		List<Drivermaster> drivers = convertDTOsToEntitiesOnCreate(driverMasterDTO, addresses);
		logger.debug("Start createDriverList Query", new Date());
		List<Drivermaster> drivermasters = driverRepository.save(drivers);
		
		//DriverPhoneNo-DTO Map
		HashMap<String, DriverDTO> phoneNoDTOMap = new HashMap<String, DriverDTO>();
		for(DriverDTO driverDTO: driverMasterDTO){
			phoneNoDTOMap.put(driverDTO.getPhoneNumber(), driverDTO);
		}
		
		//Filling up driver Ids in dto
		for(Drivermaster driver: drivermasters){
			DriverDTO dto = phoneNoDTOMap.get(driver.getPhoneNumber());
			dto.setDriverId(driver.getDriverId());
			dto.setReferenceId(driver.getReferenceId());
		}
		
		logger.debug("End createDriverList Query", new Date());
		return driverMasterDTO;

	}

	/**
	 * delete drivers by driverIds
	 */
	@Override
	public Boolean deleteDriver(List<Integer> driverIds, Session session) {
		logger.debug("Start deleteDriver Query", new Date());
		propertyConfig.saveListInActivityLog(driverIds, ModuleTypeEnum.DRIVERS, "IsDeleteFl", "Y", "", session);
		int count = driverRepository.deleteDriverByDriverIds(driverIds);
		logger.debug("End deleteDriver Query", new Date());

		if (count > 0) {
			return true;
		}
		return false;
	}

	/**
	 * check duplicate driver
	 */
	@Override
	public List<String> checkIfExistsByPhoneNumber(Integer clientId, List<String> phoneNumber,List<String> referenceIds) {
		logger.debug("Start checkIfExistsByPhoneNumber Query", new Date());
		List<String> phoneNumberDuplicateList = new ArrayList<String>();
		if(referenceIds!=null){
			phoneNumberDuplicateList = driverRepository.getDuplicateByPhoneNumber(clientId, phoneNumber,referenceIds);
		}else{
			phoneNumberDuplicateList = driverRepository.getDuplicateByPhoneNumber(clientId, phoneNumber);
		}
		logger.debug("End checkIfExistsByPhoneNumber Query", new Date());
		return phoneNumberDuplicateList;

	}
	
	@Override
	public List<String> checkIfContactNumberAlreadyExist(Session session,DriverContactValidationDTO driverContactValidationDTO){
		List<String> existingContactNumbers = new ArrayList<>();
		try{
			existingContactNumbers = driverRepository.getExistingPhoneNumbers(session.getClientId().intValue(), driverContactValidationDTO.getPhoneNumbers(), driverContactValidationDTO.getDriverIds());
		}catch(Exception ex){
			logger.error(Util.errorMessage, ex);
		}
		return existingContactNumbers;
	}
	
	/**
	 * Check if inactive driver already exists with given phoneNumber
	 * 
	 * @Param phoneNumber
	 * @return Boolean
	 * 
	 *         true - if exists false - if does not exist
	 * 
	 */
	public List<String> checkIfInactiveExists(Integer clientId, List<String> phoneNumber) {
		logger.info("Start CheckIfExist Inactive Query: {}" + new Date());
		List<String> duplicateVehicleNumberList = null;
		duplicateVehicleNumberList = driverRepository.getDuplicateByInactivePhoneNumber(clientId, phoneNumber);
		logger.info("Finished CheckIfExist Query" + new Date());
		return duplicateVehicleNumberList;
	}
	
	/**
	 * Check if in transit driver already exists with given phoneNumber
	 * 
	 * @Param phoneNumber
	 * @return Boolean
	 * 
	 *         true - if exists false - if does not exist
	 * 
	 */
	public List<String> checkIfIntransitExists(Integer clientId, List<String> phoneNumber) {
		logger.info("Start CheckIfExist Intansit Query: {}" + new Date());
		List<String> duplicateVehicleNumberList = null;
		duplicateVehicleNumberList = driverRepository.findDuplicateIntransitPhoneNumber(clientId, phoneNumber);
		logger.info("Finished CheckIfExist Query" + new Date());
		return duplicateVehicleNumberList;
	}
	

	/**
	 * Check if driver already exists with given phoneNumber for another branch
	 * 
	 * @Param phoneNumber
	 * @return Boolean
	 * 
	 *         true - if exists false - if does not exist
	 * 
	 */
	public List<String> checkIfOtherBranchExists(Integer clientBranchId, Integer clientId, List<String> phoneNumber) {
		logger.info("Start CheckIfExist Other Branch Query: {}" + new Date());
		List<String> duplicateVehicleNumberList = null;
		duplicateVehicleNumberList = driverRepository.findDuplicateOtherBranchPhoneNumber(clientBranchId, clientId, phoneNumber);
		logger.info("Finished CheckIfExist Query" + new Date());
		return duplicateVehicleNumberList;
	}

	/**
	 * check duplicate license numbers
	 */
	@Override
	public List<String> checkIfExistsLicenseNumber(List<String> licenseNumbers, Integer clientId,List<String> referenceIds) {
		List<String> licenseNumberList = new ArrayList<String>();
		logger.debug("Start checkIfExistsLicenseNumber Query", new Date());
		if(referenceIds!=null){
			licenseNumberList = driverRepository.getDuplicateLicenseNumber(licenseNumbers, clientId,referenceIds);
		}else{
			 licenseNumberList = driverRepository.getDuplicateLicenseNumber(licenseNumbers, clientId);
		}
		logger.debug("End checkIfExistsLicenseNumber Query", new Date());

		return licenseNumberList;
	}

	/**
	 * check duplicate employeeids
	 */
	@Override
	public List<String> checkIfExistsEmployeeId(List<String> employeeId, Integer clientId,List<String> referenceIds) {
		logger.debug("Start checkIfExistsEmployeeId Query on [{}]", new Date());
		List<String> empIds = new ArrayList<String>();

		if(referenceIds!=null){
			empIds = driverRepository.getDuplicateEmployeeId(employeeId, clientId,referenceIds);
		}else{
			empIds = driverRepository.getDuplicateEmployeeId(employeeId, clientId);
		}
		logger.debug("End checkIfExistsEmployeeId Query [{}] on [{}]", empIds.size(), new Date());

		return empIds;
	}

	/**
	 * convert List<DriverDTO> to List<DriverMaster>
	 * 
	 * @param lang
	 * 
	 * @param driverMasterDTOs
	 * @return
	 */
	private List<Drivermaster> convertDTOsToEntitiesOnCreate(List<DriverDTO> driverDTOs, List<Address> addresses) {
		List<Drivermaster> drivers = new ArrayList<Drivermaster>();

		for (DriverDTO driverdto : driverDTOs) {
			Drivermaster driver = new Drivermaster();
			
			if (driverdto.getReferenceId() == null) {
				driver.setReferenceId(Util.getUUID32());
			} else {
				driver.setReferenceId(driverdto.getReferenceId());
			}
			Clientmaster cm = new Clientmaster();
			if (addresses != null && addresses.size() > 0) {
				for (Address address : addresses) {
					if (address.getIsCurrentAddress() && address.getGuid()!=null&&driverdto.getGUID()!=null&& address.getGuid().equals(driverdto.getGUID())) {
						driver.setCurrentAddressid(address);
					} else if (!address.getIsCurrentAddress()&& address.getGuid()!=null&&driverdto.getGUID()!=null && address.getGuid().equals(driverdto.getGUID())) {
						driver.setPermanentAddressId(address);
					}
				}
			}
			Integer vehicleId = driverdto.getDefaultVehicle();
			driver.setGUID(driverdto.getGUID());
			driver.setDriverName(driverdto.getDriverName());
			driver.setPhoneNumber(driverdto.getPhoneNumber());
			driver.setEmailId(driverdto.getEmailId());
			driver.setSalary(driverdto.getSalary());
			driver.setDateOfBirth(driverdto.getDateOfBirth());
			driver.setGender(driverdto.getGender());
			driver.setExperience(driverdto.getExperience());

			driver.setLicenseType(driverdto.getLicenseType());
			driver.setLicenseNumber(driverdto.getLicenseNumber());
			driver.setLicenseValidity(driverdto.getLicenseValidity());
			driver.setPreviousCompanyName(driverdto.getPreviousCompanyName());
			driver.setManagerEmailId(driverdto.getManagerEmailId());
			driver.setManagerPhoneNumber(driverdto.getManagerPhoneNumber());
			driver.setDriverEmployeeId(driverdto.getDriverEmployeeId());
			driver.setReportingManager(driverdto.getReportingManager());
			if (driverdto.getClientId() != null) {
				cm.setClientId(driverdto.getClientId());
				driver.setClientId(cm);
			}
			driver.setCreatedByUserId(driverdto.getCreatedByUserId());
			driver.setUpdatedByUserId(driverdto.getUpdatedByUserId());
			driver.setClientBranchId(driverdto.getClientBranchId());
			driver.setCreatedOnDt(new Date());
			driver.setDefaultVehicle(vehicleId);
			driver.setWorkHour(driverdto.getWorkHour());
			driver.setStatus(driverdto.getStatus());
			driver.setMaritalStatus(driverdto.getMaritalStatus());
			if(StringUtils.isEmpty(driverdto.getDriverStatus())){
				driver.setDriverStatus(RESOURCE_AVAILABLE);
			}else{
				driver.setDriverStatus(driverdto.getDriverStatus());
			}
			driver.setIsActiveFl(Y);
			driver.setLicenseIssueBy(driverdto.getLicenseIssueBy());
			driver.setIsDeleteFl(N);
			drivers.add(driver);

		}

		return drivers;
	}

	/**
	 * convert List<DriverDTO>,List<Drivermaster> To List<DriverMaster>
	 */
	private List<Drivermaster> convertDTOsToEntitiesOnUpdate(List<DriverDTO> driverDTOs, List<Drivermaster> drivers, Session session) {

		Map<Integer, Drivermaster> drivermap = new HashMap<>();
		for (Drivermaster drivermaster : drivers) {
			drivermap.put(drivermaster.getDriverId(), drivermaster);

		}
		for (DriverDTO driverdto : driverDTOs) {
			Drivermaster driver = drivermap.get(driverdto.getDriverId());
			if (driver != null) {
				if(Util.checkAndCompare(driverdto.getDriverName(), driver.getDriverName())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS,"DriverName", 
							driverdto.getDriverName(), driver.getDriverName(), session);
					driver.setDriverName(driverdto.getDriverName());
				}
				
				if(Util.checkAndCompare(driverdto.getPhoneNumber(), driver.getPhoneNumber())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "PhoneNumber", 
							driverdto.getPhoneNumber(), driver.getPhoneNumber(), session);
					driver.setPhoneNumber(driverdto.getPhoneNumber());
				}
				
				driver.setUpdatedByUserId(driverdto.getUpdatedByUserId());
				driver.setUpdatedOnDt(new Date());
				
				if(Util.checkAndCompare(driverdto.getEmailId(), driver.getEmailId())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "EmailId", 
							driverdto.getEmailId(), driver.getEmailId(), session);
					driver.setEmailId(driverdto.getEmailId());
				}
				
				if(Util.checkAndCompare(driverdto.getDateOfBirth(), driver.getDateOfBirth())){
					String before = driver.getDateOfBirth() == null ? "null" : driver.getDateOfBirth().toString();
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "DateOfBirth", 
							driverdto.getDateOfBirth()!=null?driverdto.getDateOfBirth().toString():"", before, session);
					driver.setDateOfBirth(driverdto.getDateOfBirth());
				}
				
				if(Util.checkAndCompare(driverdto.getExperience(), driver.getExperience())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "Experience", 
							driverdto.getExperience(), driver.getExperience(), session);
					driver.setExperience(driverdto.getExperience());
				}
				
				if(Util.checkAndCompare(driverdto.getSalary(), driver.getSalary())){
					String before = driver.getSalary() == null ? "null" : driver.getSalary().toString();
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "Salary", 
							driverdto.getSalary()!=null?driverdto.getSalary().toString():"", before, session);
					driver.setSalary(driverdto.getSalary());
				}
				
				if(Util.checkAndCompare(driverdto.getGender(), driver.getGender())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "Gender", 
							driverdto.getGender(), driver.getGender(), session);
					driver.setGender(driverdto.getGender());
				}
				
				if(Util.checkAndCompare(driverdto.getLicenseType(), driver.getLicenseType())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "LicenseType", 
							driverdto.getLicenseType(), driver.getLicenseType(), session);
					driver.setLicenseType(driverdto.getLicenseType());
				}
				
				if(Util.checkAndCompare(driverdto.getLicenseNumber(), driver.getLicenseNumber())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "LicenseNumber", 
							driverdto.getLicenseNumber(), driver.getLicenseNumber(), session);
					driver.setLicenseNumber(driverdto.getLicenseNumber());
				}
				
				if(Util.checkAndCompare(driverdto.getLicenseValidity(), driver.getLicenseValidity())){
					String before = driver.getLicenseValidity() == null ? "null" : driver.getLicenseValidity().toString();
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "LicenseValidity", 
							driverdto.getLicenseValidity()!=null?driverdto.getLicenseValidity().toString():"", before, session);
					driver.setLicenseValidity(driverdto.getLicenseValidity());
				}
				
				if(Util.checkAndCompare(driverdto.getPreviousCompanyName(), driver.getPreviousCompanyName())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "PreviousCompanyName", 
							driverdto.getPreviousCompanyName(), driver.getPreviousCompanyName(), session);
					driver.setPreviousCompanyName(driverdto.getPreviousCompanyName());
				}
				
				if(Util.checkAndCompare(driverdto.getManagerEmailId(), driver.getManagerEmailId())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "ManagerEmailId", 
							driverdto.getManagerEmailId(), driver.getManagerEmailId(), session);
					driver.setManagerEmailId(driverdto.getManagerEmailId());
				}
				
				if(Util.checkAndCompare(driverdto.getManagerPhoneNumber(), driver.getManagerPhoneNumber())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "ManagerPhoneNumber", 
							driverdto.getManagerPhoneNumber(), driver.getManagerPhoneNumber(), session);
					driver.setManagerPhoneNumber(driverdto.getManagerPhoneNumber());
				}
				
				if(Util.checkAndCompare(driverdto.getDriverEmployeeId(), driver.getDriverEmployeeId())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "DriverEmployeeId", 
							driverdto.getDriverEmployeeId(), driver.getDriverEmployeeId(), session);
					driver.setDriverEmployeeId(driverdto.getDriverEmployeeId());
				}
				
				if(Util.checkAndCompare(driverdto.getReportingManager(), driver.getReportingManager())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "ReportingManager", 
							driverdto.getReportingManager(), driver.getReportingManager(), session);
					driver.setReportingManager(driverdto.getReportingManager());
				}
				
				if (Util.checkAndCompare(driverdto.getDefaultVehicle(), driver.getDefaultVehicle())) {
					String before = driver.getDefaultVehicle() == null ? "null" : driver.getDefaultVehicle().toString();
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "DefaultVehicle", 
							driverdto.getDefaultVehicle()!=null?driverdto.getDefaultVehicle().toString():"", before, session);
					driver.setDefaultVehicle(driverdto.getDefaultVehicle());
				}
				
				if(Util.checkAndCompare(driverdto.getWorkHour(), driver.getWorkHour())){
					String before = driver.getWorkHour() == null ? "null" : driver.getWorkHour().toString();
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "WorkHour", 
							driverdto.getWorkHour()!=null?driverdto.getWorkHour().toString():"", before, session);
					driver.setWorkHour(driverdto.getWorkHour());
				}
				if(null != driverdto.getStatus()){
					if(Util.checkAndCompare(driverdto.getStatus(), driver.getStatus())){
						propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "Status", 
								driverdto.getStatus(), driver.getStatus(), session);
						driver.setStatus(driverdto.getStatus());
					}
				}
				if(Util.checkAndCompare(driverdto.getMaritalStatus(), driver.getMaritalStatus())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "MaritalStatus", 
							driverdto.getMaritalStatus(), driver.getMaritalStatus(), session);
					driver.setMaritalStatus(driverdto.getMaritalStatus());
				}
				
				if(Util.checkAndCompare(driverdto.getLicenseIssueBy(), driver.getLicenseIssueBy())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "LicenseIssueBy", 
							driverdto.getLicenseIssueBy(), driver.getLicenseIssueBy(), session);
					driver.setLicenseIssueBy(driverdto.getLicenseIssueBy());
				}
				
				String attendance = null;
				if(null != driverdto.getAttendance()){
					if(!Constants.RESOURCE_ABSENT.equalsIgnoreCase(driverdto.getAttendance())){
						attendance = Constants.RESOURCE_AVAILABLE;
					}else{
						attendance = Constants.RESOURCE_ABSENT;
					}
					if(Util.checkAndCompare(attendance, driver.getDriverStatus())){
						propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "driverStatus", 
								attendance, driver.getDriverStatus(), session);
						driver.setDriverStatus(attendance);
					}
				}
				
				drivers.add(driver);
			}

		}
		return drivers;}

	private List<Drivermaster> convertDTOsToEntitiesOnBulkUpdate(List<DriverDTO> driverDTOs, List<Drivermaster> drivers, Session session) {

		Map<Integer, Drivermaster> drivermap = new HashMap<>();
		for (Drivermaster drivermaster : drivers) {
			drivermap.put(drivermaster.getDriverId(), drivermaster);
		}
		for (DriverDTO driverdto : driverDTOs) {
			Drivermaster driver = drivermap.get(driverdto.getDriverId());
			if (driver != null) {
				if(Util.checkAndCompare(driverdto.getDriverName(), driver.getDriverName())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "DriverName", 
							driverdto.getDriverName(), driver.getDriverName(), session);
					driver.setDriverName(driverdto.getDriverName());
				}
				if(Util.checkAndCompare(driverdto.getPhoneNumber(), driver.getPhoneNumber())){
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "PhoneNumber", 
							driverdto.getPhoneNumber(), driver.getPhoneNumber(), session);
					driver.setPhoneNumber(driverdto.getPhoneNumber());
				}
				
				if (driverdto.getUpdatedByUserId() != null) {
					driver.setUpdatedByUserId(driverdto.getUpdatedByUserId());
				}
				driver.setUpdatedOnDt(new Date());
				
				if(Util.checkAndCompare(driverdto.getWorkHour(), driver.getWorkHour())){
					String before = driver.getWorkHour() == null ? "null" : driver.getWorkHour().toString();
					propertyConfig.saveInActivityLog(driverdto.getDriverId(), ModuleTypeEnum.DRIVERS, "WorkHour", 
							driverdto.getWorkHour()!=null?driverdto.getWorkHour().toString():"", before, session);
					driver.setWorkHour(driverdto.getWorkHour());
				}
				drivers.add(driver);
			}
		}
		return drivers;}

	@Override
	public List<ShiftDTO> getShifts(Integer driverId) {
		List<ShiftDTO> shifts =shiftService.getShiftsBydriverId(driverId);
		return shifts;
	}

	public Boolean createShift(List<ShiftDTO> shiftDTOs, Integer driverId) {
		for (ShiftDTO shiftDTO : shiftDTOs) {
			shiftDTO.setDriverId(driverId);
		}
		Boolean isShiftCreated = shiftService.createShifts(shiftDTOs);
		return isShiftCreated;
	}

//	@Override
//	public void deleteShift(Integer driverId, Integer userId) {
//		List<Shift> shifts = shiftRepository.getShiftById(driverId);
//		for (Shift shift : shifts) {
//			shift.setIsDeleteFl(Y);
//			shift.setIsActiveFl(N);
//			shift.setUpdatedByUserId(userId);
//			shift.setUpdatedOnDt(new Date());
//		}
//		shiftRepository.save(shifts);
//	}

	@Override
	public List<DriverDTO> getByGuidId(String guidId) {
		List<DriverDTO> drivers = driverRepository.getByGuidId(guidId);
		return drivers;
	}

	@Override
	public List<Address> createDriverAddresses(List<DriverDTO> driverDTOs) {
		List<Address> addresses = convertAddressDTOsToAddressEntities(driverDTOs);
		List<Address> addList = addressRepository.save(addresses);
		return addList;
	}

	@Override
	public List<Address> getAddressIdsByGuid(String guid) {
		List<Address> addresses = addressRepository.getAddressIdsByGuid(guid);
		return addresses;

	}

	/**
	 * convert List<AddressDTO> To List<Address>
	 */
	public List<Address> convertAddressDTOsToAddressEntities(List<DriverDTO> driverDTOs) {

		List<Address> addressList = new ArrayList<>();
		for (DriverDTO d : driverDTOs) {
			List<AddressDTO> addressDTOs = d.getAddressList();
			for (AddressDTO addressDTO : addressDTOs) {

				Address address = new Address();
				address.setGuid(addressDTO.getGuid());
				address.setApartment(addressDTO.getApartment());
				address.setStreetName(addressDTO.getStreetName());
				address.setAreaName(addressDTO.getAreaName());
				address.setLandmark(addressDTO.getLandmark());
				address.setCity(addressDTO.getCity());
				if(!StringUtils.isEmpty(addressDTO.getCountry())){
					address.setCountry(Integer.parseInt(addressDTO.getCountry()));
				}
				if(!StringUtils.isEmpty(addressDTO.getState())){
					address.setState(Integer.parseInt(addressDTO.getState()));
				}
				address.setPincode(addressDTO.getPincode());
				address.setIsCurrentAddress(addressDTO.getIsCurrentAddress());
				if (addressDTO.getCreatedByUserId() != null) {
					address.setCreatedByUserId(addressDTO.getCreatedByUserId());
				}
				if (addressDTO.getUpdatedByUserId() != null) {
					address.setUpdatedByUserId(addressDTO.getUpdatedByUserId());
				}
				address.setCreatedOnDt(new Date());
				address.setUpdatedOnDt(new Date());
				address.setIsActiveFl(Y);
				address.setIsDeleteFl(N);
				addressList.add(address);
			}

		}
		return addressList;

	}

	@Override
	public void deletelanguagesByDriverId(Integer driverId, Integer userId) {
		logger.debug("start  deletelanguagesByDriverId query on [{}]", new Date());
		resourceLanguageMapRepository.deleteLanguageById(driverId);
		logger.debug("End  deletelanguagesByDriverId query on [{}]", new Date());

	}

	@Override
	public Boolean updateAttendance(DriverDTO driverDTO, Session session) {
		Drivermaster driver = driverRepository.getByDriverId(driverDTO.getDriverId(),session.getClientId().intValue());
		String status = null;
		Character activeStatus = null;
		
		if (driverDTO.getIsActiveFl()) {
			driver.setIsActiveFl(Y);
		} else {
			driver.setIsActiveFl(N);
		}
		
		if (driverDTO.getIsPresent()) {
			driver.setDriverStatus(RESOURCE_AVAILABLE);
		} else {
			driver.setDriverStatus(RESOURCE_ABSENT);
		}
		driver.setUpdatedByUserId(driverDTO.getUpdatedByUserId());
		driver.setUpdatedOnDt(new Date());

		Drivermaster drivermaster = driverRepository.save(driver);
		//activity log
		if(null != driverDTO && null != driverDTO.getIsPresent()){
			if(driverDTO.getIsPresent()){
				status = RESOURCE_AVAILABLE;
			}else{
				status = RESOURCE_ABSENT;
			}
			propertyConfig.saveInActivityLog(driverDTO.getDriverId(), ModuleTypeEnum.DRIVERS,"driverStatus", 
					status, driver.getDriverStatus(), session);
		}
		if(null != driverDTO && null != driverDTO.getIsActiveFl()){
			String before = driver.getIsActiveFl() == null ? "null" : driver.getIsActiveFl().toString();
			if(driverDTO.getIsActiveFl()){
				activeStatus = Y;
			}else{
				activeStatus = N;
			}
			propertyConfig.saveInActivityLog(driverDTO.getDriverId(), ModuleTypeEnum.DRIVERS,"IsActiveFl", 
					activeStatus.toString(), before, session);
		}
		if (drivermaster != null) {
			return true;
		}
		return false;
	}

	public void createLangaugeList(List<ResourceLanguageMapDTO> languageList, Integer driverId) {
		for (ResourceLanguageMapDTO languageMapDTO : languageList) {
			languageMapDTO.setDriverId(driverId);
		}
		langaugeService.createLangaugeList(languageList);
	}
	//
	// /**
	// * convert List<LanguageDTO> To List<LanguageDTO>
	// */
	// public List<Driverlanguagemap>
	// convertLanguageDTOTolanguageEntities(List<DriverLanguageMapDTO>
	// languageDTOs) {
	// List<Driverlanguagemap> langList = new ArrayList<>();
	// for (DriverLanguageMapDTO langDTO : languageDTOs) {
	// Driverlanguagemap lang = new Driverlanguagemap();
	// Drivermaster dm = new Drivermaster();
	// dm.setDriverId(langDTO.getDriverId());
	// lang.setDriverId(dm);
	// lang.setGuid(langDTO.getGuid());
	// lang.setName(langDTO.getName());
	// lang.setCode(langDTO.getCode());
	// lang.setCreatedByUserId(langDTO.getCreatedByUserId());
	// lang.setCreatedOnDt(new Date());
	// lang.setUpdatedByUserId(langDTO.getUpdatedByUserId());
	// lang.setUpdatedOnDt(new Date());
	// lang.setIsActiveFl(Y);
	// lang.setIsDeleteFl(N);
	// langList.add(lang);
	// }
	//
	// return langList;
	//
	// }

	@Override
	public DriverDTO getByDriverId(Integer driverId, Session session, String fetchType) {
		Integer clientId=session.getClientId().intValue();
		String modelType=session.getModelType();
		if(MODELTYPE_LH.equals(modelType) && PARTIAL.equalsIgnoreCase(fetchType)){
		DriverDTO dto=getDriverDetailForLH(driverId, clientId);
		return dto;
		}else{
			Drivermaster driver = driverRepository.getByDriverId(driverId,clientId);
			if(driver!=null){
				List<DriverDTO> driverDTOs = convertDriverEntityToDTO(Arrays.asList(new Drivermaster[]{driver}));
				return driverDTOs.get(0);
			}
		}
		return null;
	}
	
	private DriverDTO getDriverDetailForLH(Integer driverId,Integer clientId){
		DriverDTO dto=null;
		Object[][] objs= driverRepository.getDriverDetailByClientIdAndDriverId(clientId, driverId);
		if(null !=objs && objs.length >0){
			for(Object[] obj : objs){
			dto=new DriverDTO();
			dto.setDriverId((Integer) obj[0]);
			dto.setDriverName((String) obj[1]);
			dto.setPhoneNumber((String) obj[2]);
			dto.setVehicleNumber((String) obj[3]);
			String attendance = (String) obj[4];
			if(!StringUtils.isEmpty(attendance)){
				if(!Constants.RESOURCE_ABSENT.equalsIgnoreCase(attendance)){
					attendance = Constants.RESOURCE_PRESENT; 
				}
				else if(Constants.RESOURCE_ABSENT.equalsIgnoreCase(attendance)){
					attendance = Constants.RESOURCE_ABSENT;
				}
				dto.setAttendance(attendance);
			}
			dto.setStatus((String) obj[4]);
			dto.setDeviceBarcode((String) obj[8]);
			Character isActiveFl=(Character) obj[5];
			if (isActiveFl != null) {
				if (isActiveFl == 'Y') {
					dto.setIsActiveFl(true);
				} else if (isActiveFl == 'N') {
					dto.setIsActiveFl(false);
				}
			}
			dto.setTripId((Integer) obj[6]);
			dto.setTripName((String) obj[7]);
			//List<DriverDTO>	dtos=fillTrackingDetailsByTripIds(Arrays.asList(new DriverDTO[]{dto}), clientId);
			//dto=dtos.get(0);
			}
			}
		return dto;
	}

	
	@Override
	public List<DriverDTO> getByDriverIds(List<Integer> driverIds, Integer clientId) {
		List<Drivermaster> drivers = driverRepository.getByDriverIds(driverIds,clientId);
		if(drivers!=null&&!drivers.isEmpty()){
			return convertDriverEntityToDTO(drivers);
		}
		return null;
	}

	// convert driverdto from driver master for update get call
	private List<DriverDTO> convertDriverEntityToDTO(List<Drivermaster> drivers) {
		
	List<DriverDTO> driverDTOs = new ArrayList<DriverDTO>();

	  for(Drivermaster drivermaster : drivers){
		List<AddressDTO> addressDTOs = new ArrayList<>();

		DriverDTO driver = new DriverDTO();
		Integer vehicleId = drivermaster.getDefaultVehicle();
		if(null != vehicleId)
		{
			String vehicleNumber = driverRepository.getDefaultVehicleNumber(vehicleId);
			driver.setVehicleNumber(vehicleNumber);
		}
		driver.setGUID(drivermaster.getGUID());
		driver.setDriverId(drivermaster.getDriverId());
		driver.setDriverName(drivermaster.getDriverName());
		driver.setPhoneNumber(drivermaster.getPhoneNumber());
		driver.setEmailId(drivermaster.getEmailId());
		driver.setSalary(drivermaster.getSalary());
		driver.setLicenseType(drivermaster.getLicenseType());
		driver.setLicenseNumber(drivermaster.getLicenseNumber());
		driver.setLicenseValidity(drivermaster.getLicenseValidity());
		driver.setManagerEmailId(drivermaster.getManagerEmailId());
		driver.setDateOfBirth(drivermaster.getDateOfBirth());
		driver.setManagerPhoneNumber(drivermaster.getManagerPhoneNumber());
		driver.setDriverEmployeeId(drivermaster.getDriverEmployeeId());
		driver.setReportingManager(drivermaster.getReportingManager());
		driver.setClientBranchId(drivermaster.getClientBranchId());
		driver.setDefaultVehicle(vehicleId);
		driver.setMaritalStatus(drivermaster.getMaritalStatus());
		if(!StringUtils.isEmpty(driver.getDriverStatus())){
			if(!Constants.RESOURCE_ABSENT.equalsIgnoreCase(driver.getDriverStatus())){
				driver.setAttendance(Constants.RESOURCE_PRESENT);
			}
			else if(Constants.RESOURCE_ABSENT.equalsIgnoreCase(driver.getDriverStatus())){
				driver.setAttendance(Constants.RESOURCE_ABSENT);
			}
		}
		driver.setStatus(drivermaster.getDriverStatus());
		driver.setLicenseIssueBy(drivermaster.getLicenseIssueBy());
		driver.setGender(drivermaster.getGender());
		driver.setExperience(drivermaster.getExperience());
		driver.setPreviousCompanyName(drivermaster.getPreviousCompanyName());
		Character isActiveFl=drivermaster.getIsActiveFl();
		if (isActiveFl != null) {
			if (isActiveFl == 'Y') {
				driver.setIsActiveFl(true);
			} else if (isActiveFl == 'N') {
				driver.setIsActiveFl(false);
			}
		}
		// List<Address>
		// addresses=addressRepository.getAddressIdsByGuid(drivermaster.getGUID());

		// AddressDTO addressDTO=new AddressDTO();
		if (drivermaster.getCurrentAddressid() != null) {
			AddressDTO curaddress = convertAddressToAddressDTO(drivermaster.getCurrentAddressid());
			addressDTOs.add(curaddress);
		}
		if (drivermaster.getPermanentAddressId() != null) {
			AddressDTO peraddress = convertAddressToAddressDTO(drivermaster.getPermanentAddressId());
			addressDTOs.add(peraddress);
		}
		List<ResourceLanguageMapDTO> langdto=langaugeService.getlanguagesBydriverId(drivermaster.getDriverId());
		if(langdto !=null && langdto.size() >0)	
		driver.setLanguageList(langdto);
		
		List<ShiftDTO> shifts = shiftService.getShiftsBydriverId(driver.getDriverId());
		driver.setAddressList(addressDTOs);
		driver.setShiftList(shifts);
		driver.setReferenceId(drivermaster.getReferenceId());
		driverDTOs.add(driver);
	  }
		
	  return driverDTOs;
	}

//	private List<DriverLanguageMapDTO> convertLanguagemapTolanguageDTO(List<Driverlanguagemap> languages) {
//		List<DriverLanguageMapDTO> langList = new ArrayList<>();
//
//		for (Driverlanguagemap language : languages) {
//			DriverLanguageMapDTO langDTO = new DriverLanguageMapDTO();
//			langDTO.setId(language.getId());
//			langDTO.setGuid(language.getGuid());
//			langDTO.setName(language.getName());
//			langDTO.setCode(language.getCode());
//			langList.add(langDTO);
//		}
//		return langList;
//
//	}

	private AddressDTO convertAddressToAddressDTO(Address address) {

		AddressDTO addressDTO = new AddressDTO();
		addressDTO.setGuid(address.getGuid());
		addressDTO.setApartment(address.getApartment());
		addressDTO.setStreetName(address.getStreetName());
		addressDTO.setAreaName(address.getAreaName());
		addressDTO.setLandmark(address.getLandmark());
		addressDTO.setCity(address.getCity());
		if(null != address.getCountry()){
		addressDTO.setCountry(address.getCountry().toString());
		}
		if(null != address.getState()){
			addressDTO.setState(address.getState().toString());
			AddressDTO stDTO = getStateByStateId(address.getState());
			addressDTO.setStateShortCode(stDTO.getStateShortCode());
			addressDTO.setStateName(stDTO.getStateName());
		}
		addressDTO.setPincode(address.getPincode());
		addressDTO.setId(address.getId());
		addressDTO.setIsCurrentAddress(address.getIsCurrentAddress());
		AddressDTO adDTO = getCountryByCountryId(address.getCountry());
		addressDTO.setCountryName(adDTO.getCountryName());
		addressDTO.setCountryShortCode(adDTO.getCountryShortCode());
		return addressDTO;

	}

//	private List<ShiftDTO> convertShiftEntityToShiftDTO(List<Shift> shifts) {
//		List<ShiftDTO> shiftsdto = new ArrayList<ShiftDTO>();
//		if (shifts != null && shifts.size() > 0) {
//			for (Shift shift : shifts) {
//				ShiftDTO shiftDTO = new ShiftDTO();
//				shiftDTO.setDriverId(shift.getDriverId().getDriverId());
//				shiftDTO.setShiftStartTime(DateUtility.getStringDateFormatfromDate(shift.getShiftStartTime()));
//				shiftDTO.setShiftEndTime(DateUtility.getStringDateFormatfromDate(shift.getShiftEndTime()));
//				shiftsdto.add(shiftDTO);
//			}
//		}
//		return shiftsdto;
//
//	}

	private AddressDTO getCountryByCountryId(Integer countryId)
	{
		AddressDTO dto = new AddressDTO();
		Object[][] countryData = driverRepository.getCountryByCountryId(countryId);
		if(null !=countryData && countryData.length >0){
			String cShortCode = (String) countryData[0][0];
			dto.setCountryShortCode(cShortCode != null ? cShortCode : "");
			String countryName = (String) countryData[0][1];
			dto.setCountryName(countryName != null ? countryName : "");
		}
			return dto;
	}
	
	private AddressDTO getStateByStateId(Integer stateId)
	{
		AddressDTO dto = new AddressDTO();
		Object[][] stateData = driverRepository.getStateByStateId(stateId);
		if(null !=stateData && stateData.length >0){
			String sShortCode = (String) stateData[0][0];
			dto.setStateShortCode(sShortCode != null ? sShortCode : "");
			String stateName = (String) stateData[0][1];
			dto.setStateName(stateName != null ? stateName : "");
		}
			return dto;
	}
	
	@Override
	public void updateAddressList(List<AddressDTO> addressList) {
		try {
			Integer state=null;
			
			for (AddressDTO address : addressList) {
				if(address.getState()!=null){
					state=Integer.parseInt(address.getState());
				}
				addressRepository.updateAddressByDriverId(address.getApartment(), address.getStreetName(),
						address.getLandmark(), address.getAreaName(), address.getCity(),
						state, Integer.parseInt(address.getCountry()),
						address.getPincode(), address.getId());
			}
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
	}

	public Boolean deleteMedia(DriverDTO driver) {
		RemoveMediaDTO removeMedia = new RemoveMediaDTO();
		removeMedia.setRemoveIdProofMediaId(driver.getRemoveAddressProofId());
		removeMedia.setRemoveLicenseMediaId(driver.getRemoveLicenseProof());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<RemoveMediaDTO> request = new HttpEntity<RemoveMediaDTO>(removeMedia, headers);
		try {
			String REMOVE_MEDIA_LIST_URL = new StringBuilder().append(propertyConfig.getUrl()).append(MEDIA_MICROSERVICE_URL).append(MEDIA_REMOVE_LIST).toString();
			logger.info("start deleteMedia on driver call with url [{}] on  [{}]", REMOVE_MEDIA_LIST_URL, new Date());

			ResponseEntity<Boolean> isMediaRemoved = restTemplate.exchange(REMOVE_MEDIA_LIST_URL, HttpMethod.POST, request, Boolean.class);
			logger.info("Finished delete Media on driver call [{}] on [{}]", isMediaRemoved.getBody().toString(), new Date());

			if (!isMediaRemoved.getBody()) {
				return true;
			}
		} catch (Exception e) {
			logger.error("Error while deleting media for driver: ", e);
		}
		return false;
	}

	@Override
	public List<DriverDTO> findLicenseExpiredDrivers() {
		Object[][] objectArray = driverRepository.findLicenseExpiredDrivers();
		List<DriverDTO> driverMasterDTOs = new ArrayList<DriverDTO>();
		for (Object[] array : objectArray) {
			DriverDTO driverMasterDTO = new DriverDTO();
			driverMasterDTO.setDriverId((Integer) array[0]);
			driverMasterDTO.setDriverName((String) array[1]);
			driverMasterDTO.setClientId((Integer) array[2]);
			driverMasterDTO.setLicenseNumber((String) array[3]);
			driverMasterDTO.setLicenseValidity((Date) array[4]);
			// java.sql.Timestamp t = (Timestamp) ((Date) array[4]);
			// SimpleDateFormat df = new SimpleDateFormat("YYYY-MM-dd");
			// String s = df.format(t);
			// driverMasterDTO.setLicenseValidity(s);
			driverMasterDTO.setLastLicenseAlertSentDt((Date) array[5]);
			driverMasterDTO.setLicenseAlertWindow(Integer.parseInt((String) array[6]));
			driverMasterDTOs.add(driverMasterDTO);
		}
		return driverMasterDTOs;
	}

	public Boolean updateLastLicenseAlertSentDt(List<Integer> driverIds, Session session) {

		logger.info("Start findByDriverIdIn Query driverIds: [{}] on [{}]", driverIds.toString(), new Date());
		List<Drivermaster> drivers = driverRepository.findByDriverIdIn(driverIds);
		if (drivers != null && drivers.size() > 0) {
			logger.debug("Drivers for update found: [{}]", drivers.size());
			for (Drivermaster dr : drivers) {
				String date = "null";
				if(dr.getLastLicenseAlertSentDt()!=null)
					date = dr.getLastLicenseAlertSentDt().toString();
				propertyConfig.saveInActivityLog(dr.getDriverId(), ModuleTypeEnum.DRIVERS,"LastLicenseAlertSentDt", new Date().toString(),date, session);
				dr.setLastLicenseAlertSentDt(new Date());
			}
			driverRepository.save(drivers);
			logger.info("Finished findByDriverIdIn Query [{}]", new Date());

			return true;
		}

		return false;
	}

	@Override
	public List<DriverCreateDTO> createDrivers(List<DriverDTO> drivers) {

		List<String> referenceIds = new ArrayList<String>();
		try {
				logger.info("Start createDrivers with size [{}] on [{}]", drivers.size(), new Date());

				List<Address> addresses = createDriverAddresses(drivers);
				List<DriverDTO> driverList = createDriverList(drivers, addresses);
				
				for(DriverDTO driver: driverList){
					if(driver.getShiftList()!=null && !(driver.getShiftList().isEmpty()))
						createShift(driver.getShiftList(), driver.getDriverId());
					
					if (driver.getLanguageList() != null && driver.getLanguageList().size() > 0) {
						createLangaugeList(driver.getLanguageList(), driver.getDriverId());
					}
					
					List<MediaDTO> mediadtos = driver.getMediaList();
					if (mediadtos != null && mediadtos.size() > 0) {
						createMedia(driver);
					}
				}
				//referenceIds = driverList.stream().filter(s -> s.getReferenceId() != null).map(s -> s.getReferenceId()).collect(Collectors.toList());
				List<DriverCreateDTO> driverDTOList = new ArrayList<DriverCreateDTO>();
				for(DriverDTO driver:driverList){
					DriverCreateDTO d = new DriverCreateDTO();
					d.setDriverId(driver.getDriverId());
					d.setReferenceId(driver.getReferenceId());
					d.setPhoneNumber(driver.getPhoneNumber());
					driverDTOList.add(d);
				}
				/*	if (driverList != null && driverList.size() > 0) {
					Drivermaster driver = driverList.get(0);
					for (DriverDTO driverdt : drivers) {
						if (driverdt.getShiftList() != null && driverdt.getShiftList().size() > 0) {

							createShift(driverdt.getShiftList(), driver.getDriverId());
							if (driverdt.getLanguageList() != null && driverdt.getLanguageList().size() > 0) {
								createLangaugeList(driverdt.getLanguageList(), driver.getDriverId());
							}
						}
					}
					if (mediadtos != null && mediadtos.size() > 0) {
						createMedia(driverDTO);
					}
					return true;

				}*/
				return driverDTOList;

		} catch (Exception e) {
			logger.error(Util.errorMessage, e);
		}
		
		return new ArrayList<>();
	}

	@Override
	public Boolean updateDrivers(List<DriverDTO> drivers,List<Integer> driverIds, Session session) {
		Boolean isupdated = false;
		try {
				logger.info("Start update driver : [{}] on [{}] ", drivers.size(), new Date());
				isupdated = updateDriverList(drivers, driverIds, session);
				for (DriverDTO driver : drivers) {
					if (driver.getShiftList() != null && driver.getShiftList().size() > 0) {
						shiftService.deleteShiftByDriverId(driver.getDriverId(), driver.getUpdatedByUserId());
						if(driver.getShiftList()!=null && !(driver.getShiftList().isEmpty()))
							createShift(driver.getShiftList(), driver.getDriverId());
					}
					if (driver.getLanguageList() != null ) {
						deletelanguagesByDriverId(driver.getDriverId(), driver.getUpdatedByUserId());
						createLangaugeList(driver.getLanguageList(), driver.getDriverId());
					}
					if (driver.getAddressList() != null ) {
						updateAddressList(driver.getAddressList());
					}
					// remove deleted media from media table
					if ((driver.getRemoveAddressProofId() != null && driver.getRemoveAddressProofId().size() > 0)
							|| (driver.getRemoveLicenseProof() != null && driver.getRemoveLicenseProof().size() > 0)) {
						deleteMedia(driver);

					}
					if (driver.getMediaList() != null && driver.getMediaList().size() > 0) {
						createMedia(driver);
					}
				}
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return isupdated;
	}

	public Boolean createMedia(DriverDTO driver) {
		List<MediaDTO> mediaDTOs = driver.getMediaList();
		Boolean isMediaCreated = false;
		for (MediaDTO media : mediaDTOs) {
			media.setParentId(driver.getDriverId());
			media.setClientId(driver.getClientId());
			media.setParentGuid(driver.getGUID());
			media.setCreatedByUserId(driver.getCreatedByUserId());
		}
		try {
			String CREATE_MEDIA = propertyConfig.getUrl() + MEDIA_MICROSERVICE_URL + MEDIA_CREATE;
			logger.info("Start create Media on driver call [{}] on [{}]", CREATE_MEDIA, new Date());

			isMediaCreated = restTemplate.postForObject(CREATE_MEDIA, mediaDTOs, Boolean.class);
			logger.info("Finished create Media on driver call [{}] on [{}]", isMediaCreated.toString(), new Date());

			return isMediaCreated;
		} catch (RestClientException ex) {
			logger.error(Util.errorMessage, ex);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return isMediaCreated;
	}

	@Override
	public List<RequestDeliveryMediumDTO> getByVehicleIds(List<Integer> vehicleIds) {
		List<Drivermaster> drivers = driverRepository.getByVehicleIds(vehicleIds);
		List<RequestDeliveryMediumDTO> dmList = new ArrayList<>();
		if (drivers != null && drivers.size() > 0) {
			for (Drivermaster drivermaster : drivers) {
				RequestDeliveryMediumDTO dmDTO = new RequestDeliveryMediumDTO();
				dmDTO.setVehicleId(drivermaster.getDefaultVehicle());
				dmDTO.setDriverName(drivermaster.getDriverName());
				dmDTO.setDriverId(drivermaster.getDriverId());
				dmList.add(dmDTO);
			}
		}
		return dmList;
	}

	@Override
	public Boolean updateAttendanceForFmlm(List<DriverDTO> driverDTOs,Session session) {
		List<Integer> driverIds = driverDTOs.stream().map(r -> r.getDriverId())
				.collect(Collectors.toList());
		Map<Integer, DriverDTO> driverMap = driverDTOs.stream()
				.collect(Collectors.toMap(DriverDTO::getDriverId, (r) -> r));
		
		List<Drivermaster> driverMaster = driverRepository.findByDriverIdIn(driverIds);
		if(null != driverMaster)
		{
			List<Drivermaster> newDriverData = new ArrayList<>();
			for(Drivermaster dm : driverMaster)
			{
				String status= null;
				DriverDTO driver = driverMap.get(dm.getDriverId());
				if(!driver.getIsPresent()){
					dm.setDriverStatus(RESOURCE_ABSENT);
					status = RESOURCE_ABSENT;
				}
				else{
					dm.setDriverStatus(RESOURCE_AVAILABLE);
					status = RESOURCE_AVAILABLE;
				}
				propertyConfig.saveInActivityLog(dm.getDriverId(), ModuleTypeEnum.DRIVERS,"driverStatus", 
						status, dm.getDriverStatus(), session);
				dm.setUpdatedByUserId(driver.getUpdatedByUserId());
				newDriverData.add(dm);
			}
			driverRepository.save(newDriverData);
			logger.info("End update Attendance for fmlm ServiceImpl Successful: [{}]", Slf4jUtility.toObjArr(new Date()));
			return true;
		}
		logger.info("End update Attendance for fmlm ServiceImpl Failed: [{}]", Slf4jUtility.toObjArr(new Date()));
		return false;
	}

	@Override
	public Boolean updateActiveStatusForFmlm(List<DriverDTO> driverDTOs, Session session) {
		List<Integer> driverIds = driverDTOs.stream().map(r -> r.getDriverId())
				.collect(Collectors.toList());
		Map<Integer, DriverDTO> driverMap = driverDTOs.stream()
				.collect(Collectors.toMap(DriverDTO::getDriverId, (r) -> r));
		
		List<Drivermaster> driverMaster = driverRepository.findByDriverIdIn(driverIds);
		if(null != driverMaster)
		{
			List<Drivermaster> newDriverData = new ArrayList<>();
			for(Drivermaster dm : driverMaster)
			{
				DriverDTO driver = driverMap.get(dm.getDriverId());
				Character isactive = null;
				if(driver.getIsActiveFl()){
					isactive = Y;
				}else{
					isactive = N;
				}
				propertyConfig.saveInActivityLog(dm.getDriverId(), ModuleTypeEnum.DRIVERS,"IsActiveFl", 
						isactive!= null ? isactive.toString() : null,dm.getIsActiveFl() != null ? dm.getIsActiveFl().toString():null , session);
				if(driver.getIsActiveFl()){
					dm.setIsActiveFl(Y);
					dm.setDriverStatus(Constants.RESOURCE_AVAILABLE);
				}
				else if(!driver.getIsActiveFl()){
					dm.setIsActiveFl(N);
					dm.setDriverStatus(Constants.RESOURCE_INACTIVE);
				}
				dm.setUpdatedByUserId(driver.getUpdatedByUserId());
				newDriverData.add(dm);
			}
			driverRepository.save(newDriverData);
			logger.info("End update Active Status for fmlm ServiceImpl Successful: [{}]", Slf4jUtility.toObjArr(new Date()));
			return true;
		}
		logger.info("End update Active Status for fmlm ServiceImpl Failed: [{}]", Slf4jUtility.toObjArr(new Date()));
		return false;	
		}

	@Override
	public Boolean updateDriversList(List<DriverDTO> driverDTOs, Session session) {
		try{
			List<Integer> driverIds = driverDTOs.stream().map(r -> r.getDriverId())
					.collect(Collectors.toList());
			Map<Integer, DriverDTO> driverMap = driverDTOs.stream()
					.collect(Collectors.toMap(DriverDTO::getDriverId, (r) -> r));
			List<Drivermaster> driverMaster = driverRepository.findByDriverIdIn(driverIds);
			
			if(null != driverMaster)
			{
				List<Drivermaster> newDriverData = new ArrayList<>();
				for(Drivermaster dm : driverMaster)
				{
					DriverDTO driver = driverMap.get(dm.getDriverId());
					if(Util.checkAndCompare(driver.getDriverName(), dm.getDriverName())){
						propertyConfig.saveInActivityLog(dm.getDriverId(), ModuleTypeEnum.DRIVERS,"DriverName", 
								driver.getDriverName(), driver.getDriverName(), session);
						dm.setDriverName(driver.getDriverName());
					}
					
					if(Util.checkAndCompare(driver.getPhoneNumber(), dm.getPhoneNumber())){
						propertyConfig.saveInActivityLog(dm.getDriverId(), ModuleTypeEnum.DRIVERS, "PhoneNumber", 
								driver.getPhoneNumber(), driver.getPhoneNumber(), session);
						dm.setPhoneNumber(driver.getPhoneNumber());
					}
					dm.setUpdatedByUserId(driver.getUpdatedByUserId());
					newDriverData.add(dm);
				}
				driverRepository.save(newDriverData);
			return true;
			}
		}
		catch(Exception ex){
			logger.error(Util.errorMessage, ex);
		}
		return false;
	}

	@Override
	public List<DriverDTO> getDriverIds(List<String> referenceIds) {
		return driverRepository.getDriverIdByReferenceIdIn(referenceIds);
	}

	@Override
	public AddressDTO findDefaultDriverAddressByClientId(Integer clientId) {
		AddressDTO defaultAddressDTO=null;
		Address defaultAddress = addressRepository.findByClientId(clientId);
		if(null ==defaultAddress){
			defaultAddress = addressRepository.findByClientId(0);
		}
		if(null !=defaultAddress)
		 defaultAddressDTO = convertAddressEntityToDTO(defaultAddress);

		return defaultAddressDTO;
	}

	private AddressDTO convertAddressEntityToDTO(Address defaultAddress) {
		AddressDTO addressDTO = new AddressDTO();
		addressDTO.setGuid(defaultAddress.getGuid());
		addressDTO.setApartment(defaultAddress.getApartment());
		addressDTO.setStreetName(defaultAddress.getStreetName());
		addressDTO.setAreaName(defaultAddress.getAreaName());
		addressDTO.setLandmark(defaultAddress.getLandmark());
		addressDTO.setCity(defaultAddress.getCity());
		addressDTO.setCountry(defaultAddress.getCountry()!=null ?defaultAddress.getCountry().toString():null);
		addressDTO.setState(defaultAddress.getState()!=null?defaultAddress.getState().toString():null);
		addressDTO.setPincode(defaultAddress.getPincode());
		return addressDTO;
	}
	
	private List<Trackingrecord> getTrackingDetailsByTripIds(List<Integer> tripIds) {
		List<Trackingrecord> trackingrecords = new ArrayList<>();
		try {
			if(tripIds.size() >0){
			String TRACKING_DETAILS_URL = new StringBuilder().append(propertyConfig.getUrl()).append(TRACKING_MICROSERVICE_URL).append(TRACKING_DRIVER).append("?tripIds=")
					.append(tripIds.toString().substring(1, tripIds.toString().length() - 1)).toString();
			logger.info("Executing RestTempalte for DriverController.getTrackingDetails at url: [{}] on [{}]", TRACKING_DETAILS_URL, new Date());
			trackingrecords = Arrays.asList(restTemplate.getForObject(TRACKING_DETAILS_URL, Trackingrecord[].class));
			}
			logger.info("End TrackingService call: {} " + new Date());
		} catch (Exception e) {
			logger.error("Error while getting tracking data for vehicles: ", e);
		}
		return trackingrecords;
	}
	
	@Override
	public List<ResourceTrackingMapDTO> getDriversByClientIdAndBranches(Session session) {
		List<ResourceTrackingMapDTO> resourceTrackingMapDTOs=new ArrayList<>();
		resourceTrackingMapDTOs= driverRepository.getDriversByClientIdAndBranches(session.getClientId().intValue(), session.getCurrentClientBranchId().intValue());
		return resourceTrackingMapDTOs;
	}
	
	@Override
	public List<ResourceTrackingMapDTO> fillAvailableDriverTracking(List<ResourceTrackingMapDTO> resourceTrackingMapDTOs) {
		List<Integer> tripIds = new ArrayList<>();
		for (ResourceTrackingMapDTO TrackingMapDTO : resourceTrackingMapDTOs) {
			if(null !=TrackingMapDTO.getTripId())
				tripIds.add(TrackingMapDTO.getTripId());
		}
		if (tripIds != null && tripIds.size() > 0) {
			logger.info(" fillAvailableDriverTracking on [{}] ", new Date());
			List<Trackingrecord> trackingRecordList = getTrackingDetailsByTripIds(tripIds);
			if (trackingRecordList != null && trackingRecordList.size() > 0) {
				Map<Integer, Trackingrecord> trackingMap = new HashMap<Integer, Trackingrecord>();
				for (Trackingrecord t : trackingRecordList) {
					trackingMap.put(t.getTripId(), t);
				}
				for (ResourceTrackingMapDTO v : resourceTrackingMapDTOs) {
					Integer tripId = v.getTripId();
					if (tripId != null) {
						Trackingrecord driverTrackingLog = trackingMap.get(tripId);
						if (driverTrackingLog != null) {
							v.setLatitude(driverTrackingLog.getLatitude());
							v.setLongitude(driverTrackingLog.getLongitude());
						}
					}
				}
			}
			return resourceTrackingMapDTOs;

		}
		return resourceTrackingMapDTOs;

	}
	@Override
	public Boolean updateDriverStatus(VehicleStatusDTO driverStatusDTO){
		try{
			List<Integer> driverIds = new ArrayList<Integer>();
			Map<Integer,String> intransitMap = new HashMap<Integer,String>();
			Map<Integer,String> availableMap = new HashMap<Integer,String>();
			if(null != driverStatusDTO){
				if(null != driverStatusDTO.getAvailableResourceList() &&  driverStatusDTO.getAvailableResourceList().size() > 0){
					for(Integer driverId : driverStatusDTO.getAvailableResourceList()){
						if(driverId != null){
							driverIds.add(driverId);
							availableMap.put(driverId,Constants.VEHICLE_AVAILABLE);
						}
					}
				}
				if(null != driverStatusDTO.getIntransitResourceList() &&  driverStatusDTO.getIntransitResourceList().size() > 0){
					for(Integer driverId : driverStatusDTO.getIntransitResourceList()){
						if(driverId != null){
							driverIds.add(driverId);
							intransitMap.put(driverId,Constants.VEHICLE_INTRANSIT);
						}
					}
				}
			
				if(null != driverIds && driverIds.size() > 0){
					logger.info("updating Driver Status at : [{}] " + new Date() + " with IntransitIds:"+Arrays.toString(intransitMap.values().toArray()) + " and AvailableIds: "+Arrays.toString(availableMap.values().toArray()));
					List<Drivermaster> driverList = new ArrayList<Drivermaster>();
					List<Drivermaster> drivers = driverRepository.findByDriverIdIn(driverIds);
					for(Drivermaster d : drivers){
						if(intransitMap.containsKey(d.getDriverId())){
							d.setDriverStatus(VEHICLE_INTRANSIT);
						}
						else if(availableMap.containsKey(d.getDriverId())){
							d.setDriverStatus(VEHICLE_AVAILABLE);
						}
						d.setUpdatedOnDt(new Date());
						driverList.add(d);
					}
					driverRepository.save(driverList);
					logger.info("Driver Status updated at : [{}]" + new Date());
					return true;
				}
			}
		}
		catch(Exception ex){
			logger.error(Util.errorMessage, ex);
		}
		return false;
	}
	
	@Override
	public List<Integer> ValidateContactOnStartTrip(Session session,List<String> phoneNumbers){
		List<Integer> dupContactDriverId = new ArrayList<>();
		try{
			if(null != phoneNumbers && phoneNumbers.size() > 0){
				dupContactDriverId = driverRepository.getDuplicateBusyDriverContact(session.getClientId().intValue(), phoneNumbers);
				return dupContactDriverId;
			}else{
				return dupContactDriverId;
			}
		}catch(Exception ex){
			logger.error(Util.errorMessage, ex);	
		}
		return dupContactDriverId;
	}
	
	@Override
	public List<Integer> validateContactForIntransitDrivers(Session session,DriverContactUpdateDTO driverContactUpdateDTO){
		List<Integer> dupContactDriverId = new ArrayList<>();
		try{
			if(null != driverContactUpdateDTO && null != driverContactUpdateDTO.getPhoneNumbers() && driverContactUpdateDTO.getPhoneNumbers().size() > 0 && null != driverContactUpdateDTO.getDriverIds() && driverContactUpdateDTO.getDriverIds().size() > 0){
				dupContactDriverId = driverRepository.getDuplicateNumberForIntransitDrivers(session.getClientId().intValue(), driverContactUpdateDTO.getPhoneNumbers(),driverContactUpdateDTO.getDriverIds());
				return dupContactDriverId;
			}else{
				return dupContactDriverId;
			}
		}catch(Exception ex){
			logger.error(Util.errorMessage, ex);	
		}
		return dupContactDriverId;
	}
	
	@Override
	public List<Integer> validateContactForOtherDrivers(Session session,DriverContactUpdateDTO driverContactUpdateDTO){
		List<Integer> dupContactDriverId = new ArrayList<>();
		try{
			if(null != driverContactUpdateDTO && null != driverContactUpdateDTO.getPhoneNumbers() && driverContactUpdateDTO.getPhoneNumbers().size() > 0 && null != driverContactUpdateDTO.getDriverIds() && driverContactUpdateDTO.getDriverIds().size() > 0){
				dupContactDriverId = driverRepository.validateForDuplicateExcludingDriver(session.getClientId().intValue(), driverContactUpdateDTO.getPhoneNumbers(),driverContactUpdateDTO.getDriverIds());
				return dupContactDriverId;
			}else{
				return dupContactDriverId;
			}
		}catch(Exception ex){
			logger.error(Util.errorMessage, ex);	
		}
		return dupContactDriverId;
	}
	
	@Override
	public List<String> getIntransitDriverContactNumber(Session session,List<String> phoneNumbers){
		List<String> intransitDriverContact = new ArrayList<>();
		try{
			if(null != phoneNumbers && phoneNumbers.size() > 0){
				intransitDriverContact = driverRepository.getIntransitDriverContactNumber(session.getClientId().intValue(), phoneNumbers);
				return intransitDriverContact;
			}else{
				return intransitDriverContact;
			}
		}catch(Exception ex){
			logger.error(Util.errorMessage, ex);	
		}
		return intransitDriverContact;
	}
	
	
	
	@Override
	public List<String> getContactNumberByDriverId(Session session,List<Integer> driverIds){
		List<String> phoneNumbers = new ArrayList<>();
		try{
			phoneNumbers = driverRepository.getPhoneNumberByDriverId(session.getClientId().intValue(),driverIds);
		}catch(Exception ex){
			logger.error(Util.errorMessage, ex);
		}
		return phoneNumbers;
	}
	
	@Override
	public List<Drivermaster> getDriverByDriverIds(List<Integer> driverIds,Session session){
		try{
			List<Drivermaster> drivermaster = driverRepository.getDrivermasterByDriverId(driverIds,session.getClientId().intValue());
			return drivermaster;
		}catch(Exception ex){
			logger.error(Util.errorMessage, ex);
		}
		return null;
	}

	@Override
	public Boolean save(List<Drivermaster> drivermaster){
		try{
			driverRepository.save(drivermaster);
			return true;
		}catch(Exception ex){
			logger.error(Util.errorMessage, ex);
		}
		return false;
	}
}
