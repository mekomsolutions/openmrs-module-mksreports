package org.openmrs.module.commonreports.definition.data;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;

public class IMCIProgramDataDefinition extends BaseDataDefinition implements VisitDataDefinition {
	
	/**
	 * TODO: Not implemented. Should describe the IMCI program status
	 */
	
	private static final long serialVersionUID = 1L;
	
	// ***** CONSTRUCTORS *****
	
	public IMCIProgramDataDefinition() {
	}
	
	@Override
	public Class<?> getDataType() {
		return String.class;
	}
	
}
