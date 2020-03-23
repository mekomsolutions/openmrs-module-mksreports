package org.openmrs.module.commonreports.common;

import java.util.List;

import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;

public class ContactInfo {
	
	private PersonAddress address;
	
	private List<PersonAttribute> phoneNumbers;
	
	public ContactInfo() {
		super();
	}
	
	public PersonAddress getAddress() {
		return address;
	}
	
	public void setAddress(PersonAddress address) {
		this.address = address;
	}
	
	public List<PersonAttribute> getPhoneNumbers() {
		return phoneNumbers;
	}
	
	public void setPhoneNumbers(List<PersonAttribute> phoneNumbers) {
		this.phoneNumbers = phoneNumbers;
	}
	
}
