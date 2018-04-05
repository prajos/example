package com.loginext.ms.driver.serviceImpl;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loginext.commons.entity.Deliverymediummaster;
import com.loginext.commons.entity.Drivermaster;
import com.loginext.commons.entity.Shift;
import com.loginext.commons.model.ShiftDTO;
import com.loginext.commons.util.Constants;
import com.loginext.commons.util.DateUtility;
import com.loginext.ms.driver.repository.ShiftRepository;

@Service
@Transactional
public class ShiftServiceImpl implements Constants{

	@Autowired
	private ShiftRepository shiftRepository;

	
	public Boolean createShifts(List<ShiftDTO> shiftDTOs) {
		List<Shift> shifts = new ArrayList<Shift>();
		for (ShiftDTO shiftDTO : shiftDTOs) {
			Shift st = new Shift();
			if(shiftDTO.getDeliveryMediumMasterId() !=null){
			Deliverymediummaster dmm = new Deliverymediummaster();
			dmm.setDeliveryMediumMasterId(shiftDTO.getDeliveryMediumMasterId());
			st.setDeliveryMediumMasterId(dmm);
					}
			if(shiftDTO.getDriverId() !=null ){
				Drivermaster dm = new Drivermaster();
				dm.setDriverId(shiftDTO.getDriverId());	
				st.setDriverId(dm);
			}
			st.setShiftStartTime(shiftDTO.getShiftStartTime());
			st.setShiftEndTime(shiftDTO.getShiftEndTime());
			st.setCreatedByUserId(shiftDTO.getCreatedByUserId());
			st.setCreatedOnDt(new Date());
			st.setUpdatedOnDt(new Date());
			st.setIsActiveFl(Y);
			st.setIsDeleteFl(N);
		
			shifts.add(st);
		}
		
		List<Shift> shiftsList = shiftRepository.save(shifts);
		if (shiftsList != null && shiftsList.size() > 0) {
			return true;
		}
		return false;
	}
	
	public List<ShiftDTO> getShiftsBydriverId(Integer driverId) {
		List<Shift> shift = shiftRepository.getShiftById(driverId);
		List<ShiftDTO> shifts = convertShiftEntityToShiftDTO(shift);
		return shifts;
	}
	public List<ShiftDTO> getShiftsByDMId(Integer id) {
		List<Shift> shift = shiftRepository.getShiftByDMId(id);
		List<ShiftDTO> shifts = convertShiftEntityToShiftDTO(shift);
		return shifts;
	}
	
	public List<ShiftDTO> getShiftByDMIds(List<Integer> getShiftByDMIds) {
		List<Shift> shifts = shiftRepository.getShiftByDMIds(getShiftByDMIds);
		return (this.convertShiftEntityToShiftDTO(shifts));
	}

	
	private List<ShiftDTO> convertShiftEntityToShiftDTO(List<Shift> shifts) {
		List<ShiftDTO> shiftsdto = new ArrayList<ShiftDTO>();
		if (shifts != null && shifts.size() > 0) {
			for (Shift shift : shifts) {
				ShiftDTO shiftDTO = new ShiftDTO();

				if (shift.getDriverId() != null)
					shiftDTO.setDriverId(shift.getDriverId().getDriverId());
				if (shift.getDeliveryMediumMasterId() != null)
					shiftDTO.setDeliveryMediumMasterId(shift.getDeliveryMediumMasterId().getDeliveryMediumMasterId());
				if (null != shift.getShiftStartTime() && null != shift.getShiftEndTime()) {
					Date shiftstr = DateUtility.formatDateForShiftTiming(shift.getShiftStartTime());
					Date shiftend = DateUtility.formatDateForShiftTiming(shift.getShiftEndTime());
					shiftDTO.setShiftStartTime(shiftstr);
					shiftDTO.setShiftEndTime(shiftend);
					shiftDTO.setStartTime(shiftstr);
					shiftDTO.setEndTime(shiftend);
				}
				shiftsdto.add(shiftDTO);
			}
		}
		return shiftsdto;

	}
	
	public Boolean deleteShiftByDMId(Integer dmId, Integer userId) {
		List<Shift> shifts = shiftRepository.getShiftByDMId(dmId);
		if(shifts !=null && shifts.size() >0){
			List<Shift> shift =updateShiftEntity(shifts,userId);
		shiftRepository.save(shift);
		return true;
		}
		return false;
	}
	
	public void deleteShiftByDriverId(Integer driverId, Integer userId) {
		List<Shift> shifts = shiftRepository.getShiftById(driverId);
		if(shifts !=null && shifts.size() >0){
			List<Shift> shift =updateShiftEntity(shifts,userId);
		shiftRepository.save(shift);
		}
	}
	
	private List<Shift> updateShiftEntity(List<Shift> shifts,Integer userId){
		for (Shift shift : shifts) {
			shift.setIsDeleteFl(Y);
			shift.setIsActiveFl(N);
			//shift.setUpdatedByUserId(userId);
			shift.setUpdatedOnDt(new Date());
		}
		return shifts;
	}

}
