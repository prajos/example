package com.loginext.ms.driver.manager;

import java.net.URI;
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
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

import com.loginext.commons.aspect.CurrentSession;
import com.loginext.commons.aspect.Log;
import com.loginext.commons.aspect.PropertyConfig;
import com.loginext.commons.cache.model.TripCache;
import com.loginext.commons.entity.Drivermaster;
import com.loginext.commons.entity.Session;
import com.loginext.commons.entity.Trackingrecord;
import com.loginext.commons.model.ClientpropertyDTO;
import com.loginext.commons.model.DriverContactUpdateDTO;
import com.loginext.commons.model.DriverDTO;
import com.loginext.commons.model.GenericTabularDTO;
import com.loginext.commons.model.PageDTO;
import com.loginext.commons.model.ResourceVehicleMapDTO;
import com.loginext.commons.model.TripRequestDTO;
import com.loginext.commons.model.UpdateDriverForTripDTO;
import com.loginext.commons.model.UpdateMediaDTO;
import com.loginext.commons.util.Constants;
import com.loginext.commons.util.Slf4jUtility;
import com.loginext.commons.util.Util;
import com.loginext.ms.driver.service.DriverService;

@Component
public class DriverManager implements Constants {

	@Autowired
	private DriverService driverService;

	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private PropertyConfig propertyConfig;

	@Value("${trip.service}")
	private String TRIP_MICROSERVICE_URL;

	@Value("${trip.driver.get}")
	private String TRIP_DRIVER_GET;

	@Value("${tracking.service}")
	private String TRACKING_MICROSERVICE_URL;

	@Value("${tracking.resource}")
	private String TRACKING_DRIVER;

	@Value("${client.service}")
	private String CLIENT_MICROSERVICE_URL;

	@Value("${clientproperty.service.getByClientIdAndPropKeys}")
	private String CLIENT_PROPERTY;

	@Value("${media.service}")
	private String MEDIA_MICROSERVICE_URL;

	@Value("${media.driver.get.all}")
	private String MEDIA_GET_ALL;

	@Value("${vehicle.service}")
	private String VEHICLE_MICROSERVICE_URL;

	@Value("${resource.vehicle.get}")
	private String GET_VEHCILE;

	@Value("${trip.driver.update}")
	private String TRIP_DRIVER_UPDATE;
	
	@Value("${tripcache.driver.contact.update}")
	private String TRIP_CACHE_DRIVER_CONTACT_UPDATE;

	private static @Log Logger logger;

	public DriverDTO getByDriverId(Integer driverId, Session session, String fetchType) {
		String modeltype = session.getModelType();
		Integer clientId = session.getClientId().intValue();
		List<DriverDTO> driverDTOs = new ArrayList<>();
		DriverDTO driver = driverService.getByDriverId(driverId, session, fetchType);
		if (null != driver) {
			driverDTOs.add(driver);
			if (Constants.MODELTYPE_LH.equals(modeltype)) {
				if (!PARTIAL.equalsIgnoreCase(fetchType)) {
					driverDTOs = fillVehicleDetails(driverDTOs);
					driverDTOs = fillTripDetailsByIds(driverDTOs, clientId);
				}
				driverDTOs = fillTrackingDetailsByTripIds(driverDTOs, clientId);
			}
			return getMediaListByDriverId(driverDTOs.get(0));
		}
		return null;
	}

	public GenericTabularDTO getDrivers(Session session, PageDTO page,String dashboardStatus) {
		GenericTabularDTO tabluarDTO = driverService.getDrivers(session, page,dashboardStatus);
		if (null != tabluarDTO) {
			@SuppressWarnings("unchecked")
			List<DriverDTO> drivers = (List<DriverDTO>) tabluarDTO.getResults();
			if (drivers != null && drivers.size() > 0) {
				drivers = fillVehicleDetails(drivers);
				drivers = fillTripDetailsByIds(drivers, session.getClientId().intValue());
				drivers = fillTrackingDetailsByTripIds(drivers, session.getClientId().intValue());
			}
			tabluarDTO.setResults(drivers);
			return tabluarDTO;
		}
		return null;
	}

	private List<DriverDTO> fillTripDetailsByIds(List<DriverDTO> drivers, Integer clientId) {
		List<Integer> driverIds = drivers.stream().map(r -> r.getDriverId()).collect(Collectors.toList());
		List<String> tripStatus = new ArrayList<String>();
		List<Integer> tripIds = new ArrayList<Integer>();
		tripStatus.add(TRIPSTATUS_STARTED);
		//tripStatus.add(TRIPSTATUS_NOTSTARTED);
		TripRequestDTO tripRequestDTO = new TripRequestDTO();
		tripRequestDTO.setDriverIds(driverIds);
		tripRequestDTO.setTripStatus(tripStatus);
		Map<Integer, TripCache> tripCacheMap = new HashMap<Integer, TripCache>();
		List<TripCache> tripDetails = getTripDetailsByDriverids(tripRequestDTO);
		if (null != tripDetails && tripDetails.size() > 0)
			for (TripCache t : tripDetails) {
				tripCacheMap.put(t.getDriverId(), t);
			}
		for (DriverDTO driver : drivers) {
			if (tripCacheMap.get(driver.getDriverId()) != null) {
				TripCache t = tripCacheMap.get(driver.getDriverId());
				if (t.getTripStatus() != null && t.getTripStatus().equals(Constants.STARTED)) {
					if (null != t.getTripId())
						tripIds.add(t.getTripId());
				}
				driver.setTripName(t.getTripName());
				driver.setTripId(t.getTripId());
				if (t.getVehicleNo() != null)
					driver.setVehicleNumber(t.getVehicleNo());
				driver.setDeviceId(t.getDeviceId());
				if (t.getDeviceBarCode() != null)
					driver.setDeviceBarcode(t.getDeviceBarCode());
			}
		}
		return drivers;
	}

	private List<DriverDTO> fillTrackingDetailsByTripIds(List<DriverDTO> drivers, Integer clientId) {

		try {
			List<Integer> tripIds = new ArrayList<>();
			for (DriverDTO driverDTO : drivers) {
				if (null != driverDTO && null != driverDTO.getTripId())
					tripIds.add(driverDTO.getTripId());
			}
			if (tripIds != null && tripIds.size() > 0) {
				logger.info(" fillTrackingDetailsByTripIds with clientid [{}] on [{}] ", clientId, new Date());
				List<Trackingrecord> trackingRecordList = getTrackingDetailsByTripids(tripIds);
				if (trackingRecordList != null && trackingRecordList.size() > 0) {
					List<String> propertiesKeys = new ArrayList<>();
					propertiesKeys.add(ONLINEMIN);
					propertiesKeys.add(IDLEMIN);
					List<ClientpropertyDTO> clientPropertiesList = getpropertiesByKeysAndClientId(clientId,
							propertiesKeys);
					Long onlineWindow = 14400000L;
					Long idleWindow = 86400000L;
					if (null != clientPropertiesList && clientPropertiesList.size() > 0) {
						Long propOnlineWindow = 0L;
						Long propIdleWindow = 0L;
						for (ClientpropertyDTO clientproperty : clientPropertiesList) {
							if (StringUtils.isNotBlank(clientproperty.getPropertyValue())) {
								String key = clientproperty.getPropertyKey();
								if (key.equals(Constants.ONLINEMIN))
									propOnlineWindow = (Long.parseLong(clientproperty.getPropertyValue()) * 60 * 1000);
								if (key.equals(Constants.IDLEMIN))
									propIdleWindow = (Long.parseLong(clientproperty.getPropertyValue()) * 60 * 1000);
							}
						}
						if (propOnlineWindow > 0 && propIdleWindow > 0) {
							onlineWindow = propOnlineWindow;
							idleWindow = propIdleWindow;
						}
					}

					Map<Integer, Trackingrecord> trackingrecordMap = new HashMap<Integer, Trackingrecord>();
					for (Trackingrecord tracking : trackingRecordList) {
						trackingrecordMap.put(tracking.getTripId(), tracking);
					}

					for (DriverDTO driverDTO : drivers) {
						Integer tripId = driverDTO.getTripId();
						if (tripId != null) {
							Trackingrecord trackingrecord = trackingrecordMap.get(tripId);
							if (trackingrecord != null) {
								driverDTO.setTrackingDate(trackingrecord.getTrackingDt());
								if (null != trackingrecord.getTrackingDt())
									driverDTO.setGpsStatus(getGpsStatusByTrackingDt(trackingrecord.getTrackingDt(),
											onlineWindow, idleWindow));
								driverDTO.setSpeed(trackingrecord.getSpeed());
								driverDTO.setLat(trackingrecord.getLatitude());
								driverDTO.setLng(trackingrecord.getLongitude());
								driverDTO.setBatteryPerc(trackingrecord.getBatteryPerc());
							}
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error while getting tracking data for drivers: ", e);
		}
		return drivers;
	}

	private String getGpsStatusByTrackingDt(Date trackingDt, Long onlineWindow, Long idleWindow) {
		String GpsStatus = null;

		if (((new Date().getTime() - trackingDt.getTime()) <= onlineWindow))
			GpsStatus = Constants.ONLINE;
		else if (((new Date().getTime() - trackingDt.getTime()) > onlineWindow
				&& (new Date().getTime() - trackingDt.getTime()) <= idleWindow))
			GpsStatus = Constants.IDLE;
		else if ((new Date().getTime() - trackingDt.getTime()) > idleWindow)
			GpsStatus = Constants.OFFLINE;

		return GpsStatus;
	}

	private List<TripCache> getTripDetailsByDriverids(TripRequestDTO tripRequestDTO) {
		List<TripCache> tripDetails = null;
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<TripRequestDTO> req = new HttpEntity<TripRequestDTO>(tripRequestDTO, headers);
			 String RETREIVE_TRIP = new
			 StringBuilder().append(propertyConfig.getUrl()).append(TRIP_MICROSERVICE_URL)
			 .append(TRIP_DRIVER_GET).toString();

			logger.info("Executing RestTempalte for DriverController.getTripDetails at url: [{}] on [{}]",
					RETREIVE_TRIP, new Date());
			TripCache[] tripDetailsArray = restTemplate.postForObject(RETREIVE_TRIP, req, TripCache[].class);
			if (tripDetailsArray != null && tripDetailsArray.length > 0)
				tripDetails = Arrays.asList(tripDetailsArray);
			logger.info("End getTripDetails call: {} " + new Date());
		} catch (Exception e) {
			logger.error("Error while getting TripDetails data for drivers: ", e);
		}
		return tripDetails;
	}

	private List<Trackingrecord> getTrackingDetailsByTripids(List<Integer> tripIds) {
		List<Trackingrecord> trackingRecordList = null;
		try {
			 String TRACKING_DETAILS_URL = new
			 StringBuilder().append(propertyConfig.getUrl())
			 .append(TRACKING_MICROSERVICE_URL).append(TRACKING_DRIVER).append("?tripIds=")
			 .append(tripIds.toString().substring(1,
			 tripIds.toString().length() - 1)).toString();

			logger.info("Executing RestTempalte for DriverController.getTrackingDetails at url: [{}] on [{}]",
					TRACKING_DETAILS_URL, new Date());
			trackingRecordList = Arrays.asList(restTemplate.getForObject(TRACKING_DETAILS_URL, Trackingrecord[].class));
			logger.info("End TrackingService call: {} " + new Date());
		} catch (Exception e) {
			logger.error("Error while getting tracking data for drivers: ", e);
		}
		return trackingRecordList;
	}

	private List<ClientpropertyDTO> getpropertiesByKeysAndClientId(Integer clientId, List<String> propKeys) {
		List<ClientpropertyDTO> clientPropertiesList = null;
		try {
			 String URL = new
			 StringBuilder(propertyConfig.getUrl()).append(CLIENT_MICROSERVICE_URL)
			 .append(CLIENT_PROPERTY).append("?clientId=").append(clientId).append("&propertyKeys=")
			 .append(propKeys.toString().substring(1,
			 propKeys.toString().length() - 1)).toString();

			logger.debug("Executing Resttemplate url: [{}] at: [{}]", Slf4jUtility.toObjArr(URL, new Date()));
			ClientpropertyDTO[] clientProperties = restTemplate.getForObject(URL.toString(), ClientpropertyDTO[].class);
			clientPropertiesList = Arrays.asList(clientProperties);
		} catch (Exception e) {
			logger.error("Error while getting properties by clientId: ", e);
		}
		return clientPropertiesList;
	}

	@SuppressWarnings("unchecked")
	private DriverDTO getMediaListByDriverId(DriverDTO driver) {
		try {
			if (driver != null) {
				 String GET_ALL_MEDIA_URL = new
				 StringBuilder().append(propertyConfig.getUrl())
				 .append(MEDIA_MICROSERVICE_URL).append(MEDIA_GET_ALL).append("?parentGuid=")
				 .append(driver.getGUID()).toString();

				HashMap<String, List<UpdateMediaDTO>> mediaDTOMap = restTemplate.getForObject(GET_ALL_MEDIA_URL,
						HashMap.class);

				driver.setAddressProofId(mediaDTOMap.get(Constants.DRIVER_ID_PROOF));
				driver.setLicenseProof(mediaDTOMap.get(Constants.DRIVING_LICENSE));

			}
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return driver;
	}

	private List<DriverDTO> fillVehicleDetails(List<DriverDTO> driverDTOs) {

		List<Integer> vehicleIds = new ArrayList<Integer>();
		for (DriverDTO dm : driverDTOs) {
			if (dm.getDefaultVehicle() != null)
				vehicleIds.add(dm.getDefaultVehicle());
		}
		List<ResourceVehicleMapDTO> vehicles = new ArrayList<>();
		if (vehicleIds != null && vehicleIds.size() > 0) {
			try {
				 String VEHICLE_DETAILS_URL = new
				 StringBuilder().append(propertyConfig.getUrl()).append(VEHICLE_MICROSERVICE_URL).append(GET_VEHCILE).append("?vehicleIds=")
				 .append(vehicleIds.toString().substring(1,
				 vehicleIds.toString().length() - 1)).toString();
				
				logger.info("Executing RestTempalte for DriverController.fillVehicleDetails at url: [{}] on [{}]",
						VEHICLE_DETAILS_URL, new Date());
				ResourceVehicleMapDTO[] vehicleMasterDTOs = restTemplate.getForObject(VEHICLE_DETAILS_URL,
						ResourceVehicleMapDTO[].class);
				if (vehicleMasterDTOs != null && vehicleMasterDTOs.length > 0) {
					vehicles = Arrays.asList(vehicleMasterDTOs);
					logger.debug("End getVehicles call on getdriver [{}] on [{}]", vehicles.size(), new Date());
					Map<Integer, ResourceVehicleMapDTO> vehicleMap = new HashMap<Integer, ResourceVehicleMapDTO>();
					for (ResourceVehicleMapDTO vMasterDTO : vehicles) {
						vehicleMap.put(vMasterDTO.getVehicleId(), vMasterDTO);
					}
					for (DriverDTO driverDTO : driverDTOs) {
						Integer vehicleId = driverDTO.getDefaultVehicle();
						if (vehicleId != null) {
							ResourceVehicleMapDTO reMapDTO = vehicleMap.get(vehicleId);
							if (reMapDTO != null) {
								driverDTO.setVehicleNumber(reMapDTO.getVehicleNumber());
								driverDTO.setDeviceBarcode(reMapDTO.getDeviceBarcode());
								driverDTO.setDeviceId(reMapDTO.getDeviceId());
							}
						}
					}
				}
			} catch (Exception ex) {
				logger.error(Util.errorMessage, ex);
			}
		}
		return driverDTOs;
	}

	public Boolean updateDrivers(List<DriverDTO> drivers, Session session) {
		Boolean isCacheUpdated = false;
		List<UpdateDriverForTripDTO> updateDriverForTripDTOList = new ArrayList<>();
		List<Integer> driverIds = new ArrayList<>();
		if (drivers != null && drivers.size() > 0) {
			for (DriverDTO driver : drivers) {
				Integer driverId = driver.getDriverId();
				driverIds.add(driverId);
				UpdateDriverForTripDTO u = new UpdateDriverForTripDTO();
				u.setDriverId(driver.getDriverId());
				u.setDriverName(driver.getDriverName());
				updateDriverForTripDTOList.add(u);
			}
			Boolean isupdated = driverService.updateDrivers(drivers,driverIds,session);
			if (isupdated) {
				isCacheUpdated = updateTripCache(updateDriverForTripDTOList);
			}
		}
		return isCacheUpdated;
	}

	private Boolean updateTripCache(List<UpdateDriverForTripDTO> updateDriverForTripDTOList) {
		Boolean isTripUpdate = false;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		try {
			logger.info("Start TripUpdate on driverupdate call on [{}]", new Date());
			HttpEntity<List<UpdateDriverForTripDTO>> tripCacehUpdateRequest = new HttpEntity<List<UpdateDriverForTripDTO>>(
					updateDriverForTripDTOList, headers);
			String UPDATE_TRIP_ON_DRIVER_UPDATE_URL = new StringBuilder().append(propertyConfig.getUrl())
					.append(TRIP_MICROSERVICE_URL).append(TRIP_DRIVER_UPDATE).toString();
			
			ResponseEntity<Boolean> isTripUpdated = restTemplate.exchange(UPDATE_TRIP_ON_DRIVER_UPDATE_URL,
					HttpMethod.PUT, tripCacehUpdateRequest, Boolean.class);
			logger.info("Finished TripUpdate on driverupdate call [{}] on [{}]", isTripUpdated.getBody().toString(),
					new Date());
			isTripUpdate = isTripUpdated.getBody();
			if (isTripUpdate) {
				return isTripUpdate;
			}
		} catch (Exception e) {
			logger.error("Error while updating TripCache on driverupdate:", e);
		}
		return isTripUpdate;
	}

	public Boolean updateDriverContactNumber(Session session,List<DriverContactUpdateDTO> driverContactUpdateDTO){
		try{
			List<Integer> driverIds = new ArrayList<>();
			Map<Integer,String> driverIdContactMap = new HashMap<>();
			for(DriverContactUpdateDTO dto : driverContactUpdateDTO){
				driverIds.add(dto.getDriverId());
				driverIdContactMap.put(dto.getDriverId(), dto.getPhoneNumber());
			}
			if(null != driverIds && driverIds.size() > 0){
				List<Drivermaster> drivermaster = driverService.getDriverByDriverIds(driverIds, session);
				if(null != drivermaster && drivermaster.size() > 0){
					for(Drivermaster d : drivermaster){
						if(driverIdContactMap.containsKey(d.getDriverId())){
							d.setPhoneNumber(driverIdContactMap.get(d.getDriverId()));
						}
					}
					Boolean updatedSqlFl = driverService.save(drivermaster);
					if(updatedSqlFl){
						DriverContactUpdateDTO mongoUpdateRequest = new DriverContactUpdateDTO();
						mongoUpdateRequest.setDriverIds(driverIds);
						mongoUpdateRequest.setDriverIdContactMap(driverIdContactMap);
						//now update same in mongo also
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.APPLICATION_JSON);
						HttpEntity<DriverContactUpdateDTO> validationRequest = new HttpEntity<DriverContactUpdateDTO>(mongoUpdateRequest, headers);
						StringBuilder CACHE_UPDATE_URL = new StringBuilder().append(propertyConfig.getUrl()).append(TRIP_MICROSERVICE_URL).append(TRIP_CACHE_DRIVER_CONTACT_UPDATE);
						URI uri = Util.buildUri(session, CACHE_UPDATE_URL.toString());
						logger.info("Executing RestTemplate call for TripController.updateDriverContactInCache at url: [{}] on [{}]",uri, new Date());
						ResponseEntity<Boolean> isCacheUpdated = restTemplate.exchange(uri, HttpMethod.PUT,validationRequest, Boolean.class);
						logger.info("RestTemplate call for TripController.updateDriverContactInCache at url: [{}] resulted in [{}]",uri, isCacheUpdated);
						return isCacheUpdated.getBody();
					}
					return updatedSqlFl;
				}
			}
			return false;
		}catch(Exception ex){
			logger.error(Util.errorMessage, ex);
		}
		return false;
	}
	
}
