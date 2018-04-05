package com.loginext.web.driver.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.loginext.commons.aspect.Log;
import com.loginext.commons.aspect.PropertyConfig;
import com.loginext.commons.entity.Session;
import com.loginext.commons.model.ClientpropertyDTO;
import com.loginext.commons.model.DriverDTO;
import com.loginext.commons.model.Label;
import com.loginext.commons.util.Constants;
import com.loginext.commons.util.Util;

@Component
public class DriverExcel implements Constants{
	
	private static @Log Logger logger;	
	
	
	public static SXSSFWorkbook buildExcel(List<DriverDTO> drivers, String sourceFileAddress,Session currentSession,Map<String, Label> labelsDataMap, RestTemplate restTemplate,PropertyConfig propertyConfig, String CLIENT_MICROSERVICE_URL, String CLIENTPROPERTY_GETBYCLIENTID_AND_PROPERTYKEYS) throws IOException {
		FileOutputStream fileOut = null;
		SXSSFWorkbook workbook = new SXSSFWorkbook(1000);;
		try {
			logger.info("Start writing trips list view data to excel.");
			fileOut = new FileOutputStream(sourceFileAddress);
			SXSSFSheet worksheet = workbook.createSheet("Drivers");

			if(null == drivers || drivers.isEmpty()){
				workbook.write(fileOut);
				logger.info("Excel created with no trips list view data.");
				return workbook;
			}
			
			int cellNumber = 0;
			int rowNumber = 1 ;
			
			//Headings
			String[] headings = {"name","clientBranchName","contactNo","workHrs","trackerID","status","lastTracking","tripNo","vehicleNo","attendance","active"};
			SXSSFRow row = worksheet.createRow(0);
			for(String heading: headings){
				Label label = labelsDataMap.get(heading);
				if(null != label){
					row.createCell(cellNumber++).setCellValue(label.getValue());
				}else{
					row.createCell(cellNumber++).setCellValue(heading);
				}
			}
				
			//Globalization changes
			String dateFormat = "dd, MMM yyyy";
			String dateFormatTime = dateFormat;
			
			List<String> propertyKeys = new ArrayList<String>();
			propertyKeys.add(DATE_FORMAT);
			String REST_URL = new StringBuilder().append(propertyConfig.getUrl()).append(CLIENT_MICROSERVICE_URL)
					.append(CLIENTPROPERTY_GETBYCLIENTID_AND_PROPERTYKEYS).append("?clientId=").append(currentSession.getClientId())
					.append("&propertyKeys=").append(propertyKeys.toString().substring(1,propertyKeys.toString().length()-1)).toString();
			logger.info("Fetching clientproperty for clientId {} and url {} ",currentSession.getClientId(),REST_URL);
			ClientpropertyDTO[] clientPropertyDTOArr = restTemplate.getForObject(REST_URL,ClientpropertyDTO[].class);
			if(clientPropertyDTOArr !=null && clientPropertyDTOArr.length>0){
				for(ClientpropertyDTO c:clientPropertyDTOArr){
					switch(c.getPropertyKey()){
						case DATE_FORMAT:
							dateFormat = c.getPropertyValue();
							break;
					}
				}
			}
			dateFormatTime = dateFormat + " "+DEFAULT_TIME_FORMAT;
			
			//Data
			for(DriverDTO driver: drivers){
				cellNumber = 0;
				row = worksheet.createRow(rowNumber++);
				row.createCell(cellNumber++).setCellValue(driver.getDriverName());
				row.createCell(cellNumber++).setCellValue(driver.getClientBranchName());
				row.createCell(cellNumber++).setCellValue(driver.getPhoneNumber());
				row.createCell(cellNumber++).setCellValue(driver.getWorkHour()!=null?driver.getWorkHour().toString():"");
				row.createCell(cellNumber++).setCellValue(driver.getDeviceBarcode());
				String status = driver.getStatus()!=null?driver.getStatus():"";
				status = labelsDataMap.get(status.toUpperCase())!=null?labelsDataMap.get(status.toUpperCase()).getValue():status;
				row.createCell(cellNumber++).setCellValue(status);
				row.createCell(cellNumber++).setCellValue(driver.getTrackingDate()!=null?Util.getDateInString(driver.getTrackingDate(), Constants.UTC,currentSession.getTimeZone(),dateFormatTime):"");
				row.createCell(cellNumber++).setCellValue(driver.getTripName());
				row.createCell(cellNumber++).setCellValue(driver.getVehicleNumber());
				String driverAttendance = null;
				if(!StringUtils.isEmpty(driver.getDriverStatus())){
					if(!RESOURCE_ABSENT.equals(driver.getDriverStatus())){
						driverAttendance = RESOURCE_PRESENT;
					}else{
						driverAttendance = RESOURCE_ABSENT;
					}
				}
				row.createCell(cellNumber++).setCellValue(driverAttendance);
				String activeInactive = driver.getIsActiveFl()==true?Constants.ACTIVE:Constants.INACTIVE;
				activeInactive =labelsDataMap.get(activeInactive)!=null?labelsDataMap.get(activeInactive).getValue():activeInactive;
				row.createCell(cellNumber++).setCellValue(activeInactive);
			}
			workbook.write(fileOut);
			logger.info("Excel created with trips data having row count:{}", rowNumber);
			return workbook;	

		} catch (Exception e) {
			logger.error("Error Occured while writing trips list view report excel file", e);
		} finally{
			if(null != workbook)
				workbook.close();
			if(null != fileOut){
				fileOut.flush();
				fileOut.close();
			}
		}
		return workbook;
	}
}