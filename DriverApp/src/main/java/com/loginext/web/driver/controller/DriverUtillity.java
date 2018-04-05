package com.loginext.web.driver.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loginext.commons.aspect.Log;
import com.loginext.commons.media.util.AmazonS3Utility;
import com.loginext.commons.media.util.MediaFactory;
import com.loginext.commons.media.util.MediaUtility;
import com.loginext.commons.model.AddressDTO;
import com.loginext.commons.model.DriverDTO;
import com.loginext.commons.model.ResourceLanguageMapDTO;
import com.loginext.commons.model.DriverListHolderDTO;
import com.loginext.commons.model.MediaDTO;
import com.loginext.commons.model.ShiftDTO;
import com.loginext.commons.util.Constants;
import com.loginext.commons.util.DateUtility;
import com.loginext.commons.util.Util;


@Component
public class DriverUtillity implements DriverConstants {

private MediaUtility mediaUtility;

private static @Log Logger logger;

	public  DriverDTO getdriverDTOfromRequest(DriverDTO driverDTO, MultipartFile licenseFileUpload, MultipartFile idProofUpload,
	HttpServletRequest request){
		Gson gson=new Gson();
		
		if (!Util.isNullOrEmpty(request.getParameter("guid"))) {
			driverDTO.setGUID(request.getParameter("guid"));
		}
		String uuid=driverDTO.getGUID();
		if(uuid ==null){
	      uuid = UUID.randomUUID().toString().replaceAll("-", "");
		}
	
	if (!Util.isNullOrEmpty(request.getParameter("driverName"))) {
		driverDTO.setDriverName(request.getParameter("driverName"));
	}
	if (!Util.isNullOrEmpty(request.getParameter("clientBranchId"))) {
		driverDTO.setClientBranchId(Integer.parseInt(request.getParameter("clientBranchId")));
	}
	
	if (!Util.isNullOrEmpty(request.getParameter("phoneNumber"))) {
		driverDTO.setPhoneNumber(request.getParameter("phoneNumber"));
	}

	if (!Util.isNullOrEmpty(request.getParameter("emailId"))) {
		driverDTO.setEmailId(request.getParameter("emailId"));
	}
	if (!Util.isNullOrEmpty(request.getParameter("assignVehicle"))) {
		driverDTO.setDefaultVehicle(Integer.parseInt(request.getParameter("assignVehicle")));
	}

	if (!Util.isNullOrEmpty(request.getParameter("salary"))) {
		driverDTO.setSalary(Double.parseDouble(request.getParameter("salary")));
	}

	if (!Util.isNullOrEmpty(request.getParameter("licenseTypes"))) {
		driverDTO.setLicenseType((request.getParameter("licenseTypes")));
	}

	if (!Util.isNullOrEmpty(request.getParameter("experience"))) {
		driverDTO.setExperience(request.getParameter("experience"));
	}
	if (!Util.isNullOrEmpty(request.getParameter("gender"))) {
		driverDTO.setGender(request.getParameter("gender"));
	}
	if (!Util.isNullOrEmpty(request.getParameter("dateOfBirth"))) {
		Date d = DateUtility.stringToDateFormat(request.getParameter("dateOfBirth"));
		driverDTO.setDateOfBirth(d);
	}
	if (!Util.isNullOrEmpty(request.getParameter("maritalStatus"))) {
		driverDTO.setMaritalStatus(request.getParameter("maritalStatus"));
	}
	if (!Util.isNullOrEmpty(request.getParameter("licenseNumber"))) {
		driverDTO.setLicenseNumber(request.getParameter("licenseNumber"));
	}
	if (!Util.isNullOrEmpty(request.getParameter("licenseIssuedBy"))) {
		driverDTO.setLicenseIssueBy(request.getParameter("licenseIssuedBy"));
	}

	if (!Util.isNullOrEmpty(request.getParameter("licenseValidity"))) {
		Date d = DateUtility.stringToDateFormat(request.getParameter("licenseValidity"));
		driverDTO.setLicenseValidity(d);
	}

	AddressDTO curaddressDTO = new AddressDTO();
	AddressDTO peraddressDTO = new AddressDTO();
	if (!Util.isNullOrEmpty(request.getParameter("cur_apartment"))) {
		curaddressDTO.setApartment(request.getParameter("cur_apartment"));
		curaddressDTO.setIsCurrentAddress(true);
	}

	if (!Util.isNullOrEmpty(request.getParameter("curAddressId"))) {
		curaddressDTO.setId(Integer.parseInt(request.getParameter("curAddressId")));
	}
	if (!Util.isNullOrEmpty(request.getParameter("perAddressId"))) {
		peraddressDTO.setId(Integer.parseInt(request.getParameter("perAddressId")));
	}
	if (!Util.isNullOrEmpty(request.getParameter("cur_streetName"))) {
		curaddressDTO.setStreetName(request.getParameter("cur_streetName"));
	}

	if (!Util.isNullOrEmpty(request.getParameter("cur_landmark"))) {
		curaddressDTO.setLandmark(request.getParameter("cur_landmark"));
	}

	if (!Util.isNullOrEmpty(request.getParameter("cur_locality"))) {
		curaddressDTO.setAreaName(request.getParameter("cur_locality"));
	}

	if (!Util.isNullOrEmpty(request.getParameter("cur_city"))) {
		curaddressDTO.setCity(request.getParameter("cur_city"));
	}
	if (!Util.isNullOrEmpty(request.getParameter("cur_country"))) {
		curaddressDTO.setCountry(request.getParameter("cur_country"));
		
	}

	if (!Util.isNullOrEmpty(request.getParameter("cur_state"))) {
		curaddressDTO.setState(request.getParameter("cur_state"));
	}

	if (!Util.isNullOrEmpty(request.getParameter("cur_pincode"))) {
		curaddressDTO.setPincode(request.getParameter("cur_pincode"));
	}
	if (!Util.isNullOrEmpty(request.getParameter("per_apartment"))) {
		peraddressDTO.setApartment(request.getParameter("per_apartment"));
			}

	if (!Util.isNullOrEmpty(request.getParameter("per_streetName"))) {
		peraddressDTO.setStreetName(request.getParameter("per_streetName"));
	}

	if (!Util.isNullOrEmpty(request.getParameter("per_landmark"))) {
		peraddressDTO.setLandmark(request.getParameter("per_landmark"));
	}

	if (!Util.isNullOrEmpty(request.getParameter("per_locality"))) {
		peraddressDTO.setAreaName(request.getParameter("per_locality"));
	}

	if (!Util.isNullOrEmpty(request.getParameter("per_city"))) {
		peraddressDTO.setCity(request.getParameter("per_city"));
	}
	if (!Util.isNullOrEmpty(request.getParameter("per_country"))) {
		peraddressDTO.setCountry(request.getParameter("per_country"));	
	}

	if (!Util.isNullOrEmpty(request.getParameter("per_state"))) {
		peraddressDTO.setState(request.getParameter("per_state"));
	}

	if (!Util.isNullOrEmpty(request.getParameter("per_pincode"))) {
		peraddressDTO.setPincode(request.getParameter("per_pincode"));
	}
	if (!Util.isNullOrEmpty(request.getParameter("companyName"))) {
		driverDTO.setPreviousCompanyName(request.getParameter("companyName"));
	}

	if (!Util.isNullOrEmpty(request.getParameter("driverEmployeeId"))) {
		driverDTO.setDriverEmployeeId(request.getParameter("driverEmployeeId"));
	}
	if (!Util.isNullOrEmpty(request.getParameter("reportingManager"))) {
		driverDTO.setReportingManager(request.getParameter("reportingManager"));
	}

	if (!Util.isNullOrEmpty(request.getParameter("managerPhoneNumber"))) {
		driverDTO.setManagerPhoneNumber(request.getParameter("managerPhoneNumber"));
	}
	if (!Util.isNullOrEmpty(request.getParameter("managerEmailId"))) {
		driverDTO.setManagerEmailId(request.getParameter("managerEmailId"));
	}
	if (!Util.isNullOrEmpty(request.getParameter("attendance"))) {
		if(Constants.RESOURCE_ABSENT.equals(request.getParameter("attendance"))){
			driverDTO.setDriverStatus(Constants.RESOURCE_ABSENT);
		}
		else if(Constants.RESOURCE_PRESENT.equals(request.getParameter("attendance"))){
			driverDTO.setDriverStatus(Constants.RESOURCE_AVAILABLE);
		}
	}
	if (!Util.isNullOrEmpty(request.getParameter("isActiveFl"))) {
		Boolean isactive=Boolean.valueOf(request.getParameter("isActiveFl"));
		driverDTO.setIsActiveFl(isactive);
	}

	
	Boolean curadd=true;
	Boolean peradd=false;
	curaddressDTO.setCreatedByUserId(driverDTO.getCreatedByUserId());
	peraddressDTO.setCreatedByUserId(driverDTO.getCreatedByUserId());
	curaddressDTO.setIsCurrentAddress(curadd);
	peraddressDTO.setIsCurrentAddress(peradd);;
	peraddressDTO.setGuid(uuid);
	curaddressDTO.setGuid(uuid);
	List<AddressDTO> addresses = new ArrayList<>();
	addresses.add(peraddressDTO);
	addresses.add(curaddressDTO);
	driverDTO.setAddressList(addresses);
	List<ShiftDTO> shiftList = new ArrayList<ShiftDTO>(); 

	List<ResourceLanguageMapDTO> langList=new ArrayList<>();
	if (!Util.isNullOrEmpty(request.getParameter("shiftTiming"))) {
		String	shiftDurationList=request.getParameter("shiftTiming");
		 shiftList= gson.fromJson(shiftDurationList, new TypeToken<ArrayList<ShiftDTO>>() {}.getType());
		   if(shiftList.size() > 0){
			   driverDTO.setShiftList(shiftList);
		   } 
	} 
if (!Util.isNullOrEmpty(request.getParameter("shiftTimingList"))) {
		String	shiftDurationList=request.getParameter("shiftTimingList");
		 shiftList= gson.fromJson(shiftDurationList, new TypeToken<ArrayList<ShiftDTO>>() {}.getType());
		   if(shiftList.size() > 0){
			   driverDTO.setShiftList(shiftList);
		   } 
	}
	if (!Util.isNullOrEmpty(request.getParameter("languagelist"))) {
		String[] languageList=request.getParameter("languagelist").split(",");
		for(String language: languageList){
			ResourceLanguageMapDTO langDTO=new ResourceLanguageMapDTO();
			langDTO.setName(language);
			langDTO.setGuid(uuid);
			langList.add(langDTO);
		}
	}
	List<Integer> addressprooflist=new ArrayList<>();
	if (!Util.isNullOrEmpty(request.getParameter("removedAddressProof"))) {
		String[] addproofList=request.getParameter("removedAddressProof").split(",");
		for(String addproofid: addproofList){
			Integer id=Integer.parseInt(addproofid);
			addressprooflist.add(id);
		}
	}
	List<Integer> licenseprooflist=new ArrayList<>();
	if (!Util.isNullOrEmpty(request.getParameter("removedLicenseProof"))) {
		String[] licenseproofids=request.getParameter("removedLicenseProof").split(",");
		for(String licenseproofid: licenseproofids){
			Integer id=Integer.parseInt(licenseproofid);
			licenseprooflist.add(id);
		}
	}
	
	driverDTO.setRemoveAddressProofId(addressprooflist);
	driverDTO.setRemoveLicenseProof(licenseprooflist);
	driverDTO.setGUID(uuid);

	Long hours=0L;
	for(ShiftDTO  shiftDTO2 : shiftList){
		if(null !=shiftDTO2.getShiftStartTime() && null !=shiftDTO2.getShiftEndTime()){
   long hour =DateUtility.subtractHoursFromDate(shiftDTO2.getShiftStartTime(),shiftDTO2.getShiftEndTime());
    hours += hour;
		}
	}
	List<MediaDTO> mediaList= getMediaInfo(driverDTO, licenseFileUpload, idProofUpload);
	driverDTO.setWorkHour(hours.intValue());
	driverDTO.setLanguageList(langList);
	driverDTO.setMediaList(mediaList);
	return driverDTO;
	}
	
	public DriverListHolderDTO getDriverDTOFromExcel(Workbook wb, Integer clientId,Integer clientBranchId,
			Integer createdByUserId) throws ParseException {
		DriverListHolderDTO holderDTO=new DriverListHolderDTO();
		List<DriverDTO> driverDTOs = new ArrayList<>();
		List<AddressDTO> addressDTOs=new ArrayList<>();
		Sheet sheet = wb.getSheetAt(0);
		int startRow = 6;
		int lastRow = sheet.getLastRowNum();
		
		for(int rowNum=startRow;rowNum<=lastRow;rowNum++){
			Row currentRow = sheet.getRow(rowNum);
			DriverDTO driverDTO = new DriverDTO();
			AddressDTO addressDTO=new AddressDTO();
			AddressDTO addressDTO1=new AddressDTO();
			driverDTO.setDriverName(currentRow.getCell(DriverConstants.DRIVER_NAME).toString());
			driverDTO.setPhoneNumber(currentRow.getCell(CONTACT_NUMBER).toString());
			driverDTO.setEmailId(currentRow.getCell(EMAIL_ID).toString());
			driverDTO.setDateOfBirth(DateUtility.stringToDateFormat(currentRow.getCell(DATE_OF_BIRTH).toString()));
			//driverDTO.setLanguage(currentRow.getCell(LANGUAGE).toString());
			driverDTO.setSalary(Double.parseDouble(currentRow.getCell(SALARY).toString()));
			driverDTO.setStatus(currentRow.getCell(STATUS).toString());
			driverDTO.setGender(currentRow.getCell(GENDER).toString());
			driverDTO.setExperience(currentRow.getCell(EXPERIENCE).toString());
			addressDTO.setApartment(currentRow.getCell(CUR_APARTMENT).toString());
			addressDTO.setStreetName(currentRow.getCell(CUR_STREET_NAME).toString());
			addressDTO.setLandmark(currentRow.getCell(CUR_LANDMARK).toString());
			addressDTO.setAreaName(currentRow.getCell(CUR_AREA_NAME).toString());
			addressDTO.setCountry(currentRow.getCell(CUR_COUNTRY).toString());
			addressDTO.setState(currentRow.getCell(CUR_STATE).toString());
			addressDTO.setCity(currentRow.getCell(CUR_CITY).toString());
			addressDTO.setPincode(currentRow.getCell(CUR_PINCODE).toString());
			addressDTO1.setApartment(currentRow.getCell(PER_APARTMENT).toString());
			addressDTO1.setStreetName(currentRow.getCell(PER_STREET_NAME).toString());
			addressDTO1.setLandmark(currentRow.getCell(PER_LANDMARK).toString());
			addressDTO1.setAreaName(currentRow.getCell(PER_AREA_NAME).toString());
			addressDTO1.setCountry(currentRow.getCell(PER_COUNTRY).toString());
			addressDTO1.setState(currentRow.getCell(PER_STATE).toString());
			addressDTO1.setCity(currentRow.getCell(PER_CITY).toString());
			addressDTO1.setPincode(currentRow.getCell(PER_PINCODE).toString());
			driverDTO.setLicenseType(currentRow.getCell(LICENSE_TYPE).toString());
			driverDTO.setLicenseNumber(currentRow.getCell(LICENSE_NUMBER).toString());
			driverDTO.setDateOfBirth(DateUtility.stringToDateFormat(currentRow.getCell(LICENSE_VALIDITY).toString()));
			driverDTO.setLicenseIssueBy(currentRow.getCell(LICENSE_ISSUED_BY).toString());
			driverDTO.setDriverEmployeeId(currentRow.getCell(DRIVER_EMPLOYEE_ID).toString());
			driverDTO.setPreviousCompanyName(currentRow.getCell(COMPANY_NAME).toString());
			driverDTO.setReportingManager(currentRow.getCell(REPORTING_MANAGER).toString());
			driverDTO.setManagerPhoneNumber(currentRow.getCell(MANAGER_CONTACT_NUMBER).toString());
			driverDTO.setManagerEmailId(currentRow.getCell(MANAGER_EMAIL_ID).toString());
			driverDTO.setClientBranchId(clientBranchId);
			driverDTO.setClientId(clientId);
			driverDTO.setCreatedByUserId(createdByUserId);
			addressDTOs.add(addressDTO);
			addressDTOs.add(addressDTO1);
			driverDTOs.add(driverDTO);
			
		}
		holderDTO.setDriverList(driverDTOs);
		holderDTO.setAddressList(addressDTOs);
		return holderDTO;
	}

	
	private List<MediaDTO> getMediaInfo(DriverDTO driverDTO, MultipartFile licenseFileUpload, MultipartFile idProofUpload) {

		try{
		Integer clientId = driverDTO.getClientId();
		//TODO : Create a new function
		List<MediaDTO> mediaList = new ArrayList<MediaDTO>();
		String driverName=driverDTO.getDriverName();
		if(driverName !=null)
			driverName = driverName.replaceAll(" ","_");

		if (licenseFileUpload != null && licenseFileUpload.getSize() > 0) {
			MediaDTO mediaDTO = getUploadInfo(licenseFileUpload, Constants.DRIVING_LICENSE, driverName, clientId);
			if (mediaDTO != null) {
				mediaUtility = (AmazonS3Utility) MediaFactory.getMediaFactory("AMAZON_S3_STORAGE");
				mediaUtility.save(licenseFileUpload.getInputStream(), mediaDTO.getFileName(), mediaDTO.getMediaLocation(), false);
				mediaDTO.setParentGuid(driverDTO.getGUID());
				mediaList.add(mediaDTO);
			}

		}
		if (idProofUpload != null && idProofUpload.getSize() > 0) {
			MediaDTO mediaDTO = getUploadInfo(idProofUpload, Constants.DRIVER_ID_PROOF, driverName, clientId);
			if (mediaDTO != null) {
				mediaUtility = (AmazonS3Utility) MediaFactory.getMediaFactory("AMAZON_S3_STORAGE");
				mediaUtility.save(idProofUpload.getInputStream(), mediaDTO.getFileName(), mediaDTO.getMediaLocation(), false);
				mediaDTO.setParentGuid(driverDTO.getGUID());
				mediaList.add(mediaDTO);
			}
		}
		return mediaList;
		}
		catch(Exception e){
			logger.error(e.getMessage());
		}
		return null;
	}
	private MediaDTO getUploadInfo(MultipartFile mediaPart, String type, String driver, Integer clientId) {
		String savePath = Constants.DRIVER_MEDIA_UPLOAD_PATH;
		String currentDateString = DateUtility.dateFormatForImageFile1(new Date());

		String fileName = mediaPart.getOriginalFilename();
		String fileExtension = fileName.substring(fileName.lastIndexOf('.'));
		String newFileUploadName = driver + '_' + clientId + "_" + type + "_" + currentDateString + fileExtension;

		MediaDTO mediaDTO = new MediaDTO();
		mediaDTO.setMediaLocation(savePath);
		mediaDTO.setMediaPurposeCode(type);
		mediaDTO.setFileFormat(fileExtension);
		mediaDTO.setEntity(Constants.MEDIA_ENTITY_TYPE_DRIVER);
		mediaDTO.setFileName(newFileUploadName);
		return mediaDTO;
	}
	
}
