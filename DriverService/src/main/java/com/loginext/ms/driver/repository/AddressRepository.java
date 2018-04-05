package com.loginext.ms.driver.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.loginext.commons.entity.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long>{

	@Query("select d FROM Address d   WHERE d.isDeleteFl='N' and d.isActiveFl='Y'  AND d.guid = :guid ")
	public List<Address> getAddressIdsByGuid(@Param(value = "guid") String guid);
	
	
	@Modifying
	@Transactional
	@Query("UPDATE Address d  SET d.apartment =(:apartment) ,d.streetName =(:streetName), d.landmark =(:landmark), d.areaName =(:areaName), d.city =(:city),"
			+ " d.state =(:state) ,d.country =(:country), d.pincode =(:pincode)  WHERE d.isDeleteFl='N' and d.isActiveFl='Y'  AND d.id = :id ")
	public void updateAddressByDriverId(@Param(value = "apartment") String apartment,@Param(value = "streetName") String streetName,@Param(value = "landmark") String landmark,@Param(value = "areaName") String areaName,
			@Param(value = "city") String city,@Param(value = "state") Integer state,
			@Param(value = "country") Integer country,@Param(value = "pincode") String pincode,@Param(value = "id") Integer id
			);
	
	@Query("SELECT d FROM Address d WHERE d.isDeleteFl='N' AND d.isActiveFl='Y' AND d.clientId.clientId = (:clientId) ")
	public Address findByClientId(@Param(value = "clientId")Integer clientId);
	 

}
