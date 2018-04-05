package com.loginext.ms.driver.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginext.commons.entity.Address;
import com.loginext.commons.model.AddressDTO;
import com.loginext.commons.util.Constants;
import com.loginext.ms.driver.repository.AddressRepository;
import com.loginext.ms.driver.service.AddressService;

@Service
@Transactional
public class AddressServiceImpl implements AddressService,Constants{

	@Autowired
	private AddressRepository addressRepository;

	@Override
	public List<AddressDTO> createAddressForSaas(List<AddressDTO> addressDTOs){
		List<Address> addressList=fillAddressDetails(addressDTOs);
		addressList =addressRepository.save(addressList);
		if(null != addressList && addressList.size() >0){
			for(Address address: addressList){
				for(AddressDTO addressDTO:  addressDTOs){
					if(null !=address.getIsBillingAddress() && address.getIsBillingAddress()){
						addressDTO.setId(address.getId());
					}else{
						addressDTO.setId(address.getId());
					}
					addressDTO.setCreatedOnDt(address.getCreatedOnDt());
					addressDTO.setUpdatedOnDt(address.getUpdatedOnDt());
				}
			}
		}
		return addressDTOs;
		
	}
	
	private List<Address> fillAddressDetails(List<AddressDTO> addressDTOs){
		List<Address> addressList=new ArrayList<>();
			for (AddressDTO addressDTO : addressDTOs) {
				Address address = new Address();
				address.setGuid(addressDTO.getGuid());
				address.setApartment(addressDTO.getApartment());
				address.setStreetName(addressDTO.getStreetName());
				address.setAreaName(addressDTO.getAreaName());
				address.setLandmark(addressDTO.getLandmark());
				address.setCity(addressDTO.getCity());
				if(StringUtils.isNotBlank(addressDTO.getCountry())){
				address.setCountry(Integer.parseInt(addressDTO.getCountry()));
				}
				if(StringUtils.isNotBlank(addressDTO.getState())){
					address.setState(Integer.parseInt(addressDTO.getState()));
					}
				address.setPincode(addressDTO.getPincode());
				address.setIsBillingAddress(addressDTO.getIsBillingAddress());
				address.setCreatedOnDt(new Date());
				address.setUpdatedOnDt(new Date());
				address.setIsActiveFl(Y);
				address.setIsDeleteFl(N);
				addressList.add(address);
		}

		return addressList;
		
	}

	
}
