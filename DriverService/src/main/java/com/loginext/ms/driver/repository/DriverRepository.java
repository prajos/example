package com.loginext.ms.driver.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.loginext.commons.entity.Drivermaster;
import com.loginext.commons.model.DriverDTO;
import com.loginext.commons.model.ResourceTrackingMapDTO;

@Repository
public interface DriverRepository extends JpaRepository<Drivermaster, Long> {

//	@Query("select NEW com.loginext.commons.model.DriverDTO(d.driverId ,d.driverName,d.phoneNumber,d.defaultVehicle,"
//			+ " d.driverStatus, d.workHour,d.isActiveFl) FROM Drivermaster d inner JOIN d.clientId c  WHERE d.isDeleteFl='N' and  c.isDeleteFl='N'  AND d.clientId.clientId = :clientId AND d.clientBranchId in (:clientBranchIds)")
//	public List<DriverDTO> findDriverByClientId(@Param(value = "clientBranchIds") List<Integer> clientBranchIds, @Param(value = "clientId") Integer clientId, Pageable page);

	@Query("select d.phoneNumber from Drivermaster d  where d.isDeleteFl='N'  AND d.clientId.clientId = :clientId AND d.phoneNumber in (:phoneNumber) ")
	public List<String> getDuplicateByPhoneNumber(@Param(value = "clientId") Integer clientId, @Param(value = "phoneNumber") List<String> phoneNumber);

	@Query("select d.phoneNumber from Drivermaster d  where d.isDeleteFl='N'  AND d.clientId.clientId = (:clientId) AND d.phoneNumber in (:phoneNumber) and d.driverId not in (:driverIds) ")
	public List<String> getExistingPhoneNumbers(@Param(value = "clientId") Integer clientId, @Param(value = "phoneNumber") List<String> phoneNumber,@Param(value = "driverIds") List<Integer> driverIds);
	
	@Query("select d.phoneNumber from Drivermaster d  where d.isDeleteFl='N'  AND d.clientId.clientId = :clientId AND d.phoneNumber in (:phoneNumber) and d.referenceId not in :referenceIds ")
	public List<String> getDuplicateByPhoneNumber(@Param(value = "clientId") Integer clientId, @Param(value = "phoneNumber") List<String> phoneNumber,
			@Param(value = "referenceIds") List<String> referenceIds);

	@Query("select d.phoneNumber from Drivermaster d  where d.isDeleteFl='N'  AND d.clientId.clientId = :clientId AND d.phoneNumber in (:phoneNumber) AND d.isActiveFl='N' ")
	public List<String> getDuplicateByInactivePhoneNumber(@Param(value = "clientId") Integer clientId, @Param(value = "phoneNumber") List<String> phoneNumber);
	
	@Query("SELECT d.phoneNumber FROM Drivermaster d INNER JOIN d.clientId c "
			+ " INNER JOIN d.tripdetails t WHERE c.isDeleteFl='N' AND d.isDeleteFl='N' AND d.isActiveFl='Y' "
			+ " AND t.isDeleteFl='N' AND t.isActiveFl='Y' AND t.tripStatus <> 'ENDED' AND d.phoneNumber IN :phoneNumbers AND c.clientId = (:clientId)")
	public List<String> findDuplicateIntransitPhoneNumber(@Param(value = "clientId") Integer clientId,
			@Param(value = "phoneNumbers") List<String> phoneNumbers);
	
	@Query("SELECT d.phoneNumber FROM Drivermaster d INNER JOIN d.clientId c WHERE c.isDeleteFl='N' "
			+ " AND d.isDeleteFl='N' AND d.isActiveFl='Y' AND d.clientBranchId <> (:clientBranchId) "
			+ " AND d.phoneNumber IN :phoneNumbers AND c.clientId = (:clientId)")
	public List<String> findDuplicateOtherBranchPhoneNumber(@Param(value = "clientBranchId") Integer clientBranchId, @Param(value = "clientId") Integer clientId,
			@Param(value = "phoneNumbers") List<String> phoneNumbers);
	
	@Query("select NEW com.loginext.commons.model.DriverDTO(d.driverId ,d.GUID) FROM Drivermaster d  inner JOIN d.clientId c  WHERE d.isDeleteFl='N' AND c.isDeleteFl='N'  AND d.GUID = :guidId")
	public List<DriverDTO> getByGuidId(@Param(value = "guidId") String guidId);

	@Query("select d FROM Drivermaster d where d.isDeleteFl='N' and d.driverId = (:driverId) and d.clientId.clientId = :clientId")
	public Drivermaster getByDriverId(@Param(value = "driverId") Integer driverId,@Param(value = "clientId") Integer clientId);
	

	@Query(value = "select v.vehicleNumber FROM vehiclemaster v inner join drivermaster d on d.defaultVehicle = v.vehicleId where v.vehicleId = (:vehicleId) and d.isDeleteFl = 'N' and v.isDeleteFl = 'N'", nativeQuery = true)
	public String getDefaultVehicleNumber(@Param(value = "vehicleId")Integer vehicleId);
	
	@Query("select d FROM Drivermaster d where d.driverId in (:driverIds)")
	public List<Drivermaster> getByDriverIds(@Param(value = "driverIds") List<Integer> driverIds);

	@Query("select d.licenseNumber from Drivermaster d  where d.isDeleteFl='N' and  d.licenseNumber in (:licenseNumber) AND d.clientId.clientId = :clientId ")
	public List<String> getDuplicateLicenseNumber(@Param(value = "licenseNumber") List<String> licenseNumber,@Param(value = "clientId") Integer clientId);


	@Query("select d.licenseNumber from Drivermaster d  where d.isDeleteFl='N' and  d.licenseNumber in (:licenseNumber) AND d.clientId.clientId = :clientId and d.referenceId not in :referenceIds ")
	public List<String> getDuplicateLicenseNumber(@Param(value = "licenseNumber") List<String> licenseNumber,@Param(value = "clientId") Integer clientId,
			@Param(value = "referenceIds") List<String> referenceIds);

	@Query("select d.driverEmployeeId from Drivermaster d  where d.isDeleteFl='N' AND  d.driverEmployeeId in (:employeeId) AND d.clientId.clientId = :clientId")
	public List<String> getDuplicateEmployeeId(@Param(value = "employeeId") List<String> employeeId, @Param(value = "clientId") Integer clientId);

	@Query("select d.driverEmployeeId from Drivermaster d  where d.isDeleteFl='N' AND  d.driverEmployeeId in (:employeeId) AND d.clientId.clientId = :clientId and d.referenceId not in :referenceIds ")
	public List<String> getDuplicateEmployeeId(@Param(value = "employeeId") List<String> employeeId, @Param(value = "clientId") Integer clientId,
			@Param(value = "referenceIds") List<String> referenceIds);

	
	@Query(value=" select dm.driverId,dm.driverName,dm.clientId,dm.licenseNumber,dm.licenseValidity,dm.lastLicenseAlertSentDt,  "
			+ " case when (dm.licenseAlertWindow is not null) then dm.licenseAlertWindow  "
			+ " when (cp.propertievalue is not null) then  cp.propertievalue else 7 end licenseAlertWindow "
			+ " from drivermaster dm "
			+ " left join clientproperties cp on cp.clientid=dm.clientid and cp.isDeleteFl='N' and cp.propertiekey='LICENSEALERTWINDOW' "
			+ " where  dm.isDeleteFl='N' and DATEDIFF(dm.licenseValidity,now()) <= "
			+ " (case when (dm.licenseAlertWindow is not null) then dm.licenseAlertWindow  "
			+ " when (cp.propertievalue is not null) then  cp.propertievalue else 7 end) and DATEDIFF(dm.licenseValidity,now())>=0 ", nativeQuery = true)
			public Object[][] findLicenseExpiredDrivers();
	
	public List<Drivermaster> findByDriverIdIn(List<Integer> driverId);
	

	@Modifying
	@Query("UPDATE Drivermaster d  SET d.isDeleteFl='Y' ,d.updatedOnDt=now(), d.isActiveFl='N' WHERE d.driverId in (:driverIds) ")
	public int deleteDriverByDriverIds(@Param(value = "driverIds") List<Integer> driverIds);

	@Query("select d FROM Drivermaster d where d.defaultVehicle in (:vehicleIds) and d.isDeleteFl='N'")
	public List<Drivermaster> getByVehicleIds(@Param(value = "vehicleIds") List<Integer> vehicleIds);

	@Query(value = "select countryShortCode,countryName FROM countrymaster where countryId = (:countryId)", nativeQuery = true)
	public Object[][] getCountryByCountryId(@Param(value = "countryId")Integer countryId);
	
	@Query(value = "select stateShortCode,stateName FROM statemaster where stateId = (:stateId)", nativeQuery = true)
	public Object[][] getStateByStateId(@Param(value = "stateId")Integer stateId);

	public int countByReferenceId(String referenceId);

	@Query("select NEW com.loginext.commons.model.DriverDTO(d.referenceId,d.driverId) FROM Drivermaster d where d.referenceId in :referenceIds AND d.isDeleteFl='N'")
	public List<DriverDTO> getDriverIdByReferenceIdIn(@Param(value = "referenceIds")List<String> referenceIds);

	@Query("select d FROM Drivermaster d where d.isDeleteFl='N' and d.driverId in (:driverIds) and d.clientId.clientId = :clientId")
	public List<Drivermaster> getByDriverIds(@Param(value = "driverIds")List<Integer> driverIds,@Param(value = "clientId") Integer clientId);
	
	@Query("SELECT NEW com.loginext.commons.model.ResourceTrackingMapDTO(d.driverId,d.driverName,td.tripId) FROM Drivermaster d LEFT JOIN d.tripdetails td WITH td.tripStatus IN ('STARTED','NOTSTARTED') AND td.isDeleteFl='N' "
			+ "AND td.isActiveFl='Y' inner join Userbranchmap ubm on ubm.clientBranchId=d.clientBranchId and ubm.distributionCenter = :clientBranchId "
			+ "inner join Clientmapping cmp on cmp.subClientId=d.clientId.clientId and cmp.clientId.clientId = :clientId "
			+ "WHERE d.isDeleteFl='N' AND d.isActiveFl='Y' AND td.driverId IS NULL") 
	public List<ResourceTrackingMapDTO> getDriversByClientIdAndBranches(@Param(value = "clientId") Integer clientId,@Param(value = "clientBranchId") Integer clientBranchId);
	
	@Query(value="select dm.driverId ,dm.driverName,dm.phoneNumber,v.vehicleNumber,"
			+ " dm.driverStatus,dm.isActiveFl,t.tripId,t.tripName,d.barcode from  drivermaster dm left join vehiclemaster v on dm.defaultVehicle = v.vehicleId and v.isDeleteFl='N' AND v.isActiveFl='Y'"
			+ " left join devicemaster d on d.deviceId=v.deviceId  and d.isDeleteFl='N'  inner join tripdetails t on t.driverId=dm.driverId"
			+ " where  t.isDeleteFl='N'  and dm.isDeleteFl='N' AND dm.isActiveFl='Y'  and dm.clientId =(:clientId) "
			+ " and dm.driverId =(:driverId)  order by t.tripId desc  limit 1", nativeQuery = true)
	public Object[][] getDriverDetailByClientIdAndDriverId(@Param(value = "clientId") Integer clientId,@Param(value = "driverId") Integer driverId);

	@Query("SELECT d.driverId FROM Drivermaster d where d.clientId.clientId = (:clientId) and d.phoneNumber in (:contacts) and d.isDeleteFl = 'N' and d.isActiveFl ='Y' and d.driverStatus = 'Intransit'") 
	public List<Integer> getDuplicateBusyDriverContact(@Param(value = "clientId") Integer clientId,@Param(value = "contacts") List<String> phoneNumbers);
	
	@Query("SELECT d.driverId FROM Drivermaster d where d.clientId.clientId = (:clientId) and d.phoneNumber in (:contacts) and d.isDeleteFl = 'N' and d.isActiveFl ='Y' and d.driverStatus = 'Intransit' and d.driverId not in (:driverIds)")
	public List<Integer> getDuplicateNumberForIntransitDrivers(@Param(value = "clientId") Integer clientId,@Param(value = "contacts") List<String> phoneNumbers,@Param(value = "driverIds") List<Integer> driverIds);
	
	@Query("SELECT d.driverId FROM Drivermaster d where d.clientId.clientId = (:clientId) and d.phoneNumber in (:contacts) and d.isDeleteFl = 'N' and d.isActiveFl ='Y' and d.driverId not in (:driverIds)") 
	public List<Integer> validateForDuplicateExcludingDriver(@Param(value = "clientId") Integer clientId,@Param(value = "contacts") List<String> phoneNumbers,@Param(value = "driverIds") List<Integer> driverIds);
	
	@Query("SELECT d.phoneNumber FROM Drivermaster d where d.clientId.clientId = (:clientId) and d.phoneNumber in (:contacts) and d.isDeleteFl = 'N' and d.isActiveFl ='Y' and d.driverStatus = 'Intransit'") 
	public List<String> getIntransitDriverContactNumber(@Param(value = "clientId") Integer clientId,@Param(value = "contacts") List<String> phoneNumbers);
	
	@Query("SELECT d.phoneNumber FROM Drivermaster d where d.clientId.clientId = (:clientId) and d.driverId in (:driverIds)") 
	public List<String> getPhoneNumberByDriverId(@Param(value = "clientId") Integer clientId,@Param(value = "driverIds") List<Integer> driverIds);
	
	@Query("select d FROM Drivermaster d where d.driverId in (:driverIds) and d.isDeleteFl='N' and d.isActiveFl = 'Y' and d.clientId.clientId = (:clientId)")
	public List<Drivermaster> getDrivermasterByDriverId(@Param(value = "driverIds") List<Integer> driverIds,@Param(value = "clientId") Integer clientId);
	
}
