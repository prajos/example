package com.loginext.ms.driver.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.loginext.commons.entity.Resourcelanguagemap;
import com.loginext.commons.model.ResourceLanguageMapDTO;

@Repository
public interface ResourceLanguageMapRepository extends JpaRepository<Resourcelanguagemap, Long>{

	@Query("select l FROM Resourcelanguagemap l   WHERE l.isDeleteFl='N' and l.isActiveFl='Y'  AND l.guid = :guid ")
	public List<Resourcelanguagemap> getlanguageIdsByGuid(@Param(value = "guid") String guid);
	
	@Query("select l FROM Resourcelanguagemap l   WHERE l.isDeleteFl='N' and l.isActiveFl='Y'  AND l.driverId.driverId = :driverId ")
	public List<Resourcelanguagemap> getlanguageIdsBydriverId(@Param(value = "driverId") Integer driverId);
	
	@Query("select l FROM Resourcelanguagemap l   WHERE l.isDeleteFl='N' and l.isActiveFl='Y'  AND l.deliveryMediumMasterId.deliveryMediumMasterId = :deliveryMediumMasterId ")
	public List<Resourcelanguagemap> getlanguageIdsBydmId(@Param(value = "deliveryMediumMasterId") Integer deliveryMediumMasterId);
	
	@Modifying
	@Query("UPDATE Resourcelanguagemap dlm SET dlm.isDeleteFl ='Y' ,dlm.updatedOnDt=(now()) WHERE dlm.driverId.driverId = (:driverId) and dlm.isDeleteFl='N' and dlm.isActiveFl='Y' ")
	public int deleteLanguageById(@Param(value = "driverId") Integer driverId);
	
	@Modifying
	@Query("UPDATE Resourcelanguagemap dlm SET dlm.isDeleteFl ='Y' ,dlm.updatedOnDt=(now()) WHERE dlm.deliveryMediumMasterId.deliveryMediumMasterId = (:deliveryMediumMasterId) and dlm.isDeleteFl='N' and dlm.isActiveFl='Y' ")
	public int deleteLanguageByDMId(@Param(value = "deliveryMediumMasterId") Integer deliveryMediumMasterId);
	
	@Query("select NEW com.loginext.commons.model.ResourceLanguageMapDTO( l.deliveryMediumMasterId.deliveryMediumMasterId ,l.name) FROM Resourcelanguagemap l   WHERE l.isDeleteFl='N' and l.isActiveFl='Y'  AND l.deliveryMediumMasterId.deliveryMediumMasterId IN (:deliveryMediumMasterIds) ")
	public List<ResourceLanguageMapDTO> getlanguageIdsBydmIds(@Param(value = "deliveryMediumMasterIds") List<Integer> deliveryMediumMasterIds);
	

}
