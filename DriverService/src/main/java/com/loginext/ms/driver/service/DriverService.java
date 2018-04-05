package com.loginext.ms.driver.service;

import java.util.List;

import com.loginext.commons.entity.Address;
import com.loginext.commons.entity.Drivermaster;
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


public interface DriverService {

//	public List<ShiftDTO> createShift(List<ShiftDTO> shiftDTOs, Integer driverid) ;
		
	//public void deleteShift(Integer driverId,Integer userId);

	public List<ShiftDTO> getShifts(Integer driverId);

	public List<DriverDTO> getByGuidId(String guidId);

	public List<Address> createDriverAddresses(List<DriverDTO> driverDTOs);

	public List<Address> getAddressIdsByGuid(String guid);

	public Boolean updateAttendance(DriverDTO driverDTO, Session session);

	//public Boolean createLangaugeList(List<DriverLanguageMapDTO> languageList, Integer driverid);

	public List<String> checkIfExistsLicenseNumber(List<String> licenseNumber, Integer clientId,List<String> referenceIds);

	public List<String> checkIfExistsEmployeeId(List<String> driverEmployeeId, Integer clientId,List<String> referenceIds);

	public void updateAddressList(List<AddressDTO> addressList);

	public void deletelanguagesByDriverId(Integer driverId,Integer userId);

	public List<String> checkIfExistsByPhoneNumber(Integer clientId, List<String> phoneNumber,List<String> referenceIds);
	
	public List<String> checkIfContactNumberAlreadyExist(Session session,DriverContactValidationDTO driverContactValidationDTO);

	public GenericTabularDTO getDrivers(Session session, PageDTO page,String dashboardStatus);

	List<DriverDTO> findLicenseExpiredDrivers();
	
	public Boolean updateLastLicenseAlertSentDt(List<Integer> driverIds, Session session);

	public Boolean deleteMedia(DriverDTO driver);

	Boolean deleteDriver(List<Integer> driverIds, Session session);

	public List<DriverCreateDTO> createDrivers(List<DriverDTO> drivers);

	public Boolean updateDrivers(List<DriverDTO> drivers,List<Integer> driverIds, Session session);

	public List<RequestDeliveryMediumDTO> getByVehicleIds(List<Integer> vehicleIds);

	public Boolean updateAttendanceForFmlm(List<DriverDTO> driverDTOs,Session session);
	
	public Boolean updateActiveStatusForFmlm(List<DriverDTO> driverDTOs, Session session);
	
	public Boolean updateDriversList(List<DriverDTO> drivers, Session session);

	public List<DriverDTO> getDriverIds(List<String> referenceIds);

	public List<DriverDTO> getByDriverIds(List<Integer> driverIds, Integer clientId);
	
	public AddressDTO findDefaultDriverAddressByClientId(Integer clientId);

	public DriverDTO getByDriverId(Integer driverId, Session session, String fetchType);

	public List<ResourceTrackingMapDTO> getDriversByClientIdAndBranches(Session session);

	public List<ResourceTrackingMapDTO> fillAvailableDriverTracking(List<ResourceTrackingMapDTO> resourceTrackingMapDTOs);

	public List<String> checkIfInactiveExists(Integer clientId, List<String> phoneNumber);

	public List<String> checkIfIntransitExists(Integer clientId, List<String> phoneNumber);

	public List<String> checkIfOtherBranchExists(Integer clientBranchId, Integer clientId, List<String> phoneNumber);
	
	public Boolean updateDriverStatus(VehicleStatusDTO driverStatusDTO);
	
	public List<Integer> ValidateContactOnStartTrip(Session session,List<String> phoneNumbers);
	
	public List<Integer> validateContactForIntransitDrivers(Session session,DriverContactUpdateDTO driverContactUpdateDTO);
	
	public List<Integer> validateContactForOtherDrivers(Session session,DriverContactUpdateDTO driverContactUpdateDTO);
	
	public List<String> getIntransitDriverContactNumber(Session session,List<String> phoneNumbers);
	
	public List<String> getContactNumberByDriverId(Session session,List<Integer> driverIds);

	public List<Drivermaster> getDriverByDriverIds(List<Integer> driverIds,Session session);
	
	public Boolean save(List<Drivermaster> drivermaster);
}
