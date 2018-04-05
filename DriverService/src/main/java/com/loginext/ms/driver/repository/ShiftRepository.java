package com.loginext.ms.driver.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.loginext.commons.entity.Shift;
import com.loginext.commons.model.ShiftDTO;

@Repository
public interface ShiftRepository extends JpaRepository<Shift, Long> {

	@Query("select s from Shift s where  s.isDeleteFl='N' and s.isActiveFl='Y' and s.driverId.driverId = (:driverId)")
	public List<Shift> getShiftById(@Param(value = "driverId") Integer driverId);
	
	@Query("select s from Shift s where  s.isDeleteFl='N' and s.isActiveFl='Y' and s.deliveryMediumMasterId.deliveryMediumMasterId = (:deliveryMediumMasterId)")
	public List<Shift> getShiftByDMId(@Param(value = "deliveryMediumMasterId") Integer deliveryMediumMasterId);
	
	@Query("SELECT  s from Shift s where s.isDeleteFl='N' and s.isActiveFl='Y' and s.deliveryMediumMasterId.deliveryMediumMasterId IN (:deliveryMediumMasterIds)")
	public List<Shift> getShiftByDMIds(@Param(value = "deliveryMediumMasterIds") List<Integer> deliveryMediumMasterIds);
	

}
