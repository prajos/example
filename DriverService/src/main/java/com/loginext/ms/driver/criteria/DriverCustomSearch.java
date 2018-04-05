package com.loginext.ms.driver.criteria;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.loginext.commons.entity.Session;
import com.loginext.commons.model.DriverDTO;
import com.loginext.commons.model.GenericTabularDTO;
import com.loginext.commons.model.PageDTO;
import com.loginext.commons.model.SearchDriverCol;
import com.loginext.commons.model.SortDirection;
import com.loginext.commons.util.Constants;
import com.loginext.commons.util.Util;

@Component
public class DriverCustomSearch {
	@Autowired
	private EntityManager entityManager;

	private static final String DRIVER_LIST = "SELECT d.driverId ,d.driverName,d.phoneNumber,d.defaultVehicle,"
			+ " d.driverStatus, d.workHour,d.isActiveFl,cbm.name FROM Drivermaster d INNER JOIN clientmaster c ON c.clientId = d.clientId "
			+ " LEFT JOIN clientbranchmaster cbm on cbm.clientbranchid = d.clientbranchid inner join userbranchmap ubm on d.clientbranchid=ubm.clientbranchid WHERE d.isDeleteFl='N' and  c.isDeleteFl='N'  AND c.clientId = :clientId AND ubm.distributioncenter = :currentClientBranchId";

	private static final String DRIVER_LIST_COUNT = "SELECT count(1) FROM Drivermaster d INNER JOIN clientmaster c ON c.clientId = d.clientId  inner join userbranchmap ubm on d.clientbranchid=ubm.clientbranchid WHERE d.isDeleteFl='N' and  c.isDeleteFl='N'  AND c.clientId = :clientId AND ubm.distributioncenter = :currentClientBranchId";

	public GenericTabularDTO bulidSearchCriteria(Session session, PageDTO page,String dashboardStatus) {
		String driverList = DRIVER_LIST;
		String driverCount = DRIVER_LIST_COUNT;
		GenericTabularDTO tabularDTO = null;
		if(null != dashboardStatus && !dashboardStatus.isEmpty()){
			switch(dashboardStatus){
			case Constants.status_available:
				driverList = driverList + " and d.driverStatus = 'Available' and d.isActiveFl = 'Y' ";
				driverCount = driverCount + " and d.driverStatus = 'Available' and d.isActiveFl = 'Y' ";
				break;
			case Constants.status_busy:
				driverList = driverList + " and d.driverStatus = 'Intransit' and d.isActiveFl = 'Y' ";
				driverCount = driverCount + " and d.driverStatus = 'Intransit' and d.isActiveFl = 'Y' ";
				break;
			}
		}
		HashMap<String, String> mapper = Util.buildSearchCriteria(page);
		for (SearchDriverCol col : SearchDriverCol.values()) {
			String column = (String)mapper.get(col.toString());
			if (!Util.isNullOrEmpty(column)) {
				String columnName = col.fromValue();
				columnName=Util.escapeSql(columnName);
				driverList = driverList + " AND d." + columnName + " LIKE '%" + column + "%'";
				driverCount = driverCount + " AND d." + columnName + " LIKE '%" + column + "%'";
			}
		}
		String sortCol = page.getSortBy().toUpperCase();
//		String sortCol = page.getSortBy();
		SearchDriverCol driCol = SearchDriverCol.getValue(sortCol);
		sortCol = driCol != null ? driCol.fromValue() : SearchDriverCol.DRIVERID.fromValue();
		sortCol=Util.escapeSql(sortCol);
		String direction = page.getSortOrder();
		SortDirection order = SortDirection.getValue(direction);
		direction = order != null ? order.toString() : SortDirection.DESC.toString();
		driverList = driverList + " ORDER BY d." + sortCol + " " + direction;

		Query query = entityManager.createNativeQuery(driverList);
		query.setParameter("currentClientBranchId", session.getCurrentClientBranchId().intValue());
		query.setParameter("clientId", session.getClientId().intValue());
		query.setFirstResult(((page.getNumber() < 1 ? 1 : page.getNumber()) - 1) * page.getSize());
		query.setMaxResults(page.getSize());
		@SuppressWarnings("unchecked")
		List<Object[]> objects = query.getResultList();
		if (null != objects) {
			List<DriverDTO> drivers = transformToDriverDTO(objects);
			query = entityManager.createNativeQuery(driverCount);
			query.setParameter("currentClientBranchId", session.getCurrentClientBranchId().intValue());
			query.setParameter("clientId", session.getClientId().intValue());
			BigInteger count = (BigInteger) query.getSingleResult();
			tabularDTO = new GenericTabularDTO();
			tabularDTO.setResults(drivers);
			tabularDTO.setTotalCount(count != null ? count.intValue() : 0);
			return tabularDTO;
		}
		return tabularDTO;
	}

	private List<DriverDTO> transformToDriverDTO(List<Object[]> objects) {
		List<DriverDTO> drivers = new ArrayList<DriverDTO>();
		for (Object[] obj : objects) {
			DriverDTO d = new DriverDTO();
			d.setDriverId((Integer) obj[0]);
			d.setDriverName((String) obj[1]);
			d.setPhoneNumber((String) obj[2]);
			d.setDefaultVehicle((Integer) obj[3]);
			String driverStatus=(String) obj[4];
			String attendance = null;
			if(!StringUtils.isEmpty(driverStatus)){
				if(!Constants.RESOURCE_ABSENT.equalsIgnoreCase(driverStatus)){
					attendance = Constants.RESOURCE_PRESENT;
				}
			}
			d.setAttendance(attendance);
			if(null!=attendance ){
				if(Constants.RESOURCE_PRESENT.equalsIgnoreCase(attendance)){
					d.setIsPresent(true);
				}else if(Constants.RESOURCE_ABSENT.equalsIgnoreCase(attendance)){
					d.setIsPresent(false);
				}
			}
			d.setWorkHour((Integer) obj[5]);
			Character isActiveFl = (Character) obj[6];
			if (isActiveFl != null && isActiveFl == 'Y') {
				d.setIsActiveFl(true);
			} else {
				d.setIsActiveFl(false);
			}
			d.setClientBranchName((String) obj[7]);
			d.setStatus(driverStatus);
			drivers.add(d);
		}
		return drivers;
	}
}
