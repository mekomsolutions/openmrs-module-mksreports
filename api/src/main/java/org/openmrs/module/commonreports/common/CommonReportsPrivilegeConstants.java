package org.openmrs.module.commonreports.common;

import org.openmrs.annotation.AddOnStartup;
import org.openmrs.annotation.HasAddOnStartupPrivileges;

@HasAddOnStartupPrivileges
public class CommonReportsPrivilegeConstants {
	
	@AddOnStartup(description = "Able to view patient history report")
	public static final String VIEW_PATIENT_HISTORY = "App: Can View Patient History Report";
	
}
