package org.openmrs.module.commonreports.definition.data;

import org.openmrs.module.commonreports.common.ContactInfo;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;

public class ContactInfoDataDefinition extends BaseDataDefinition implements PatientDataDefinition {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * Default Constructor
	 */
	public ContactInfoDataDefinition() {
		super();
	}
	
	@Override
	public Class<?> getDataType() {
		return ContactInfo.class;
	}
	
}
