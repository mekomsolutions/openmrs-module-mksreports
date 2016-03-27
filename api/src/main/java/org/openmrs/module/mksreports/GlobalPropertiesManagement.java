package org.openmrs.module.mksreports;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.EncounterType;
import org.openmrs.api.context.Context;
import org.openmrs.module.mksreports.util.MetadataLookup;

public class GlobalPropertiesManagement {
	
	protected final static Log log = LogFactory.getLog(GlobalPropertiesManagement.class);
	
	public EncounterType getEncounterType(String globalPropertyName) {
		String globalProperty = Context.getAdministrationService().getGlobalProperty(globalPropertyName);
		return MetadataLookup.getEncounterType(globalProperty);
	}
	
	//Encounters
	public final static String REGISTRATION_ENCOUNTER_TYPE = "mksreports.registration.encountertype";
	
}
