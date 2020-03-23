/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.commonreports.data.converter;

import java.util.List;

import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.layout.address.AddressSupport;
import org.openmrs.layout.address.AddressTemplate;
import org.openmrs.module.commonreports.common.ContactInfo;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converter to return a formated address appended with the phone numbers
 */
public class AddressAndPhoneConverter implements DataConverter {
	
	/**
	 * @return returns a formatted address + phone numbers based on a provided ContactInfo object.
	 */
	@Override
	public Object convert(Object original) {
		ContactInfo contactInfo = (ContactInfo) original;
		
		// get and format the address
		PersonAddress address = contactInfo.getAddress();
		String addressStr = "";
		for (String format : getFormatLines()) {
			for (String entry : format.split(" ")) {
				String addressEntry = getAddressByFieldName(address, entry);
				if (addressEntry != null && !addressEntry.equals("")) {
					addressStr = !addressStr.equals("") ? addressStr + ", " + addressEntry : addressEntry;
				}
			}
		}
		
		// get and format the phone numbers
		String phoneNumbersStr = "";
		for (PersonAttribute phoneNumber : contactInfo.getPhoneNumbers()) {
			if (phoneNumber != null) {
				phoneNumbersStr = (!phoneNumbersStr.equals("") ? ", " + phoneNumber.getValue() : phoneNumber.getValue());
			}
		}
		
		return !phoneNumbersStr.equals("") ? addressStr + " | " + phoneNumbersStr : addressStr;
	}
	
	@Override
	public Class<?> getInputDataType() {
		return ContactInfo.class;
	}
	
	@Override
	public Class<?> getDataType() {
		return String.class;
	}
	
	/**
	 * @return the format lines of the address template
	 */
	protected List<String> getFormatLines() {
		
		// Read the Address Template format
		AddressSupport addressSupport = AddressSupport.getInstance();
		AddressTemplate addressTemplate = addressSupport.getAddressTemplate().get(0);
		
		List<String> formatLines = addressTemplate.getLineByLineFormat();
		
		return formatLines;
	}
	
	/**
	 * @param address
	 * @param fieldName
	 * @return the address value corresponding to the field name provided
	 */
	private String getAddressByFieldName(PersonAddress address, String fieldName) {
		
		if (fieldName.equals("address1")) {
			return address.getAddress1();
		} else if (fieldName.equals("address2")) {
			return address.getAddress2();
		} else if (fieldName.equals("address3")) {
			return address.getAddress3();
		} else if (fieldName.equals("address4")) {
			return address.getAddress4();
		} else if (fieldName.equals("address5")) {
			return address.getAddress5();
		} else if (fieldName.equals("address6")) {
			return address.getAddress6();
		} else if (fieldName.equals("cityVillage")) {
			return address.getCityVillage();
		} else if (fieldName.equals("country")) {
			return address.getCountry();
		} else if (fieldName.equals("countyDistrict")) {
			return address.getCountyDistrict();
		} else if (fieldName.equals("postalCode")) {
			return address.getPostalCode();
		} else if (fieldName.equals("stateProvince")) {
			return address.getStateProvince();
		}
		return null;
		
	}
	
}
