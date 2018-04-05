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

import com.loginext.commons.aspect.Log;
import com.loginext.commons.model.ResourceLanguageMapDTO;
import com.loginext.commons.util.Util;
import com.loginext.ms.driver.serviceImpl.LanguageServiceImpl;

@RequestMapping("/language")
@Controller
public class LanguageController {

	@Autowired
	private LanguageServiceImpl languageService;
	
	private static @Log Logger logger;
	
	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Boolean> createLanguage(@RequestBody List<ResourceLanguageMapDTO> deList) {
		Boolean isCreated = languageService.createLangaugeList(deList);
		return new ResponseEntity<Boolean>(isCreated,HttpStatus.OK);
	}
	
	@RequestMapping(value = "/list/bydmIds", method = RequestMethod.POST)
	public  ResponseEntity<List<ResourceLanguageMapDTO>> getlanguageIdsBydmIds(@RequestBody List<Integer> dmIds) {
		try {
			logger.info("Executing service layer LanguageController.getLangaugesByDMId [{}] ",dmIds);
			if (null !=dmIds && !dmIds.isEmpty()) {
				List<ResourceLanguageMapDTO> langauges = languageService.getlanguageIdsBydmIds(dmIds);
				logger.info("Executed service layer LanguageController.getLangaugesById [{}] ",langauges.size());
				return new ResponseEntity<>(langauges, HttpStatus.OK);
			}

		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	@RequestMapping(value = "/list/{dm_id}", method = RequestMethod.GET)
	public  ResponseEntity<List<ResourceLanguageMapDTO>> getLangaugesByDMId(@PathVariable("dm_id") Integer dmId) {
		try {
			logger.info("Executing service layer LanguageController.getLangaugesByDMId [{}] on: [{}]",dmId.toString(), new Date());
			if (dmId != null) {
				List<ResourceLanguageMapDTO> langauges = languageService.getlanguagesByDMId(dmId);
				logger.info("Executed service layer LanguageController.getLangaugesById [{}] on: [{}]",langauges.size(), new Date());
				return new ResponseEntity<List<ResourceLanguageMapDTO>>(langauges, HttpStatus.OK);
			}

		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	@RequestMapping(value = "/dm/delete", method = RequestMethod.POST)
	public ResponseEntity<Boolean> deleteLanguagesByDMId(@RequestBody Integer dmId) {
		try{
		logger.info("Executing service layer LanguageController.deleteLanguagesByDMId [{}] on: [{}]",dmId.toString(), new Date());
		if (dmId != null) {
			Boolean isLanguageDeleted=languageService.deleteLanguagesByDMId(dmId);
			logger.info("Executed service layer LanguageController.deleteLanguagesByDMId [{}] on: [{}]",isLanguageDeleted, new Date());
			return new ResponseEntity<Boolean>(isLanguageDeleted,HttpStatus.OK);
		}} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
}