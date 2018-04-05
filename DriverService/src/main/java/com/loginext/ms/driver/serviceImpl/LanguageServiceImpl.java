package com.loginext.ms.driver.serviceImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginext.commons.aspect.Log;
import com.loginext.commons.entity.Deliverymediummaster;
import com.loginext.commons.entity.Resourcelanguagemap;
import com.loginext.commons.entity.Drivermaster;
import com.loginext.commons.model.ResourceLanguageMapDTO;
import com.loginext.commons.util.Constants;
import com.loginext.ms.driver.repository.ResourceLanguageMapRepository;


@Service
@Transactional
public class LanguageServiceImpl implements Constants{

	@Autowired
	private ResourceLanguageMapRepository resourceLanguageMapRepository;
	public static @Log Logger logger;
	
	public Boolean createLangaugeList(List<ResourceLanguageMapDTO> languageList) {
		List<Resourcelanguagemap> langList = convertLanguageDTOTolanguageEntities(languageList);
		if (langList != null && langList.size() > 0) {
			resourceLanguageMapRepository.save(langList);
			return true;
		}
		return false;

	}

	public List<ResourceLanguageMapDTO> getlanguageIdsBydmIds(List<Integer> dmIds) {
		return  resourceLanguageMapRepository.getlanguageIdsBydmIds(dmIds);
	}
	
	

	/**
	 * convert List<LanguageDTO> To List<LanguageDTO>
	 */
	public List<Resourcelanguagemap> convertLanguageDTOTolanguageEntities(List<ResourceLanguageMapDTO> languageDTOs) {
		List<Resourcelanguagemap> langList = new ArrayList<>();
		for (ResourceLanguageMapDTO langDTO : languageDTOs) {
			Resourcelanguagemap lang = new Resourcelanguagemap();
			
			if(langDTO.getDeliveryMediumMasterId() !=null){
			Deliverymediummaster  dmm = new Deliverymediummaster();
			dmm.setDeliveryMediumMasterId(langDTO.getDeliveryMediumMasterId());
			lang.setDeliveryMediumMasterId(dmm);
			}
			if(langDTO.getDriverId() !=null){
			Drivermaster dm = new Drivermaster();
			dm.setDriverId(langDTO.getDriverId());
			lang.setDriverId(dm);
			}
			lang.setGuid(langDTO.getGuid());
			lang.setName(langDTO.getName());
			lang.setCode(langDTO.getCode());
			lang.setCreatedByUserId(langDTO.getCreatedByUserId());
			lang.setCreatedOnDt(new Date());
			lang.setUpdatedByUserId(langDTO.getUpdatedByUserId());
			lang.setUpdatedOnDt(new Date());
			lang.setIsActiveFl(Y);
			lang.setIsDeleteFl(N);
			langList.add(lang);
		}

		return langList;

	}
	public List<ResourceLanguageMapDTO>  getlanguagesByDMId(Integer dmId){
	List<Resourcelanguagemap> langauges = resourceLanguageMapRepository.getlanguageIdsBydmId(dmId);
	List<ResourceLanguageMapDTO> languageMapDTOs=new ArrayList<>();
	if (langauges != null && langauges.size() > 0) {
		  languageMapDTOs= convertLanguagemapTolanguageDTO(langauges);
	}
	return languageMapDTOs;
	}
	
	public List<ResourceLanguageMapDTO> getlanguagesBydriverId(Integer driverId) {
		logger.debug("start  getlanguagesBydriverId query on [{}]", new Date());
		List<Resourcelanguagemap> langauges = resourceLanguageMapRepository.getlanguageIdsBydriverId(driverId);
		List<ResourceLanguageMapDTO> languageMapDTOs = new ArrayList<>();
		if (langauges != null && langauges.size() > 0) {
			languageMapDTOs = convertLanguagemapTolanguageDTO(langauges);
		}
		logger.debug("End  getlanguagesBydriverId query on [{}]", new Date());

		return languageMapDTOs;
	}

	public Boolean deleteLanguagesByDMId(Integer dmId) {
		logger.debug("start  deletelanguagesByDriverId query on [{}]", new Date());
		int count = resourceLanguageMapRepository.deleteLanguageByDMId(dmId);
		if (count > 0) {
			return true;
		}
		logger.debug("End  deletelanguagesByDriverId query on [{}]", new Date());
		return false;
	}
	public  List<ResourceLanguageMapDTO> convertLanguagemapTolanguageDTO(List<Resourcelanguagemap> languages) {
		List<ResourceLanguageMapDTO> langList = new ArrayList<>();

		for (Resourcelanguagemap language : languages) {
			ResourceLanguageMapDTO langDTO = new ResourceLanguageMapDTO();
			langDTO.setId(language.getId());
			langDTO.setGuid(language.getGuid());
			langDTO.setName(language.getName());
			langDTO.setCode(language.getCode());
			langList.add(langDTO);
		}
		return langList;

	}
}
