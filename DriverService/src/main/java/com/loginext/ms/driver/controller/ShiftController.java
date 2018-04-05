package com.loginext.ms.driver.controller;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.loginext.commons.aspect.CurrentSession;
import com.loginext.commons.aspect.Log;
import com.loginext.commons.entity.Session;
import com.loginext.commons.model.ShiftDTO;
import com.loginext.commons.util.Util;
import com.loginext.ms.driver.serviceImpl.ShiftServiceImpl;

@RequestMapping("/shift")
@Controller
public class ShiftController {
	
	@Autowired
	private ShiftServiceImpl shiftService;
	private static @Log Logger logger;
	
	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> createShift(@RequestBody List<ShiftDTO> shiftDTOs) {
		try {
			if (shiftDTOs != null && shiftDTOs.size() >0) {
			logger.info("Executing service layer ShiftController.createShift [{}] on: [{}]",shiftDTOs.size(), new Date());
		Boolean isCreated = shiftService.createShifts(shiftDTOs);
		logger.info("Executed service layer ShiftController.createShift [{}] on: [{}]",isCreated.toString(), new Date());
		return new ResponseEntity<Boolean>(isCreated,HttpStatus.OK);
			}} catch (Exception ex) {
				logger.error(Util.errorMessage, ex);
			}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/list/{dm_id}", method = RequestMethod.GET)
	public ResponseEntity<List<ShiftDTO>> getShiftsByDMId(@PathVariable("dm_id") Integer id) {
		try {
			logger.info("Executing service layer ShiftController.getShiftsByDMId [{}] on: [{}]",id.toString(), new Date());
			if (id != null) {
				List<ShiftDTO> shifts = shiftService.getShiftsByDMId(id);
				if(shifts !=null && shifts.size() >0)
				logger.info("Executed service layer ShiftController.getShiftsByDMId [{}] on: [{}]",shifts.size(), new Date());
				return new ResponseEntity<List<ShiftDTO>>(shifts, HttpStatus.OK);
			}

		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/list/bydmIds", method = RequestMethod.POST)
	public ResponseEntity<List<ShiftDTO>> getShiftsByDMId(@RequestBody List<Integer> dmIds) {
		try {
			logger.info("Executing service layer ShiftController.getShiftsByDMId [{}]",dmIds);
			return new ResponseEntity<>(shiftService.getShiftByDMIds(dmIds),HttpStatus.OK);
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/deliverymedium/delete", method = RequestMethod.POST)
	public ResponseEntity<Boolean> deleteShiftByDMId(@RequestBody Integer dmId) {
		try{
		logger.info("Executing service layer ShiftController.deleteShiftByDMId [{}] on: [{}]",dmId.toString(), new Date());
		if (dmId != null ) {
			Boolean isShiftDeleted=shiftService.deleteShiftByDMId(dmId,null);
			logger.info("Executed service layer ShiftController.deleteShiftByDMId [{}] on: [{}]",isShiftDeleted, new Date());
			return new ResponseEntity<Boolean>(isShiftDeleted,HttpStatus.OK);
		}} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}