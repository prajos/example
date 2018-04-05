package com.loginext.ms.driver.controller;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.loginext.commons.aspect.Log;
import com.loginext.commons.model.AddressDTO;
import com.loginext.commons.util.Util;
import com.loginext.ms.driver.service.AddressService;

@RequestMapping("/address")
@Controller
public class AddressController {
	
	@Autowired
	private AddressService addressService;
	
	private static @Log Logger logger;
	
	@RequestMapping(value = "/saas/create", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<AddressDTO>> createAddressForSaas(@RequestBody List<AddressDTO> addressDTOs) {
		try {
			if (addressDTOs != null && addressDTOs.size() > 0) {
				logger.info("Executing service layer AddressController.createAddressForSaas [{}] on: [{}]",
						addressDTOs.size(), new Date());
				addressDTOs = addressService.createAddressForSaas(addressDTOs);
				logger.info("Executed service layer AddressController.createAddressForSaas [{}] on: [{}]",
						addressDTOs.size(), new Date());
				return new ResponseEntity<List<AddressDTO>>(addressDTOs, HttpStatus.OK);
			}
		} catch (Exception ex) {
			logger.error(Util.errorMessage, ex);
		}
		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	
}