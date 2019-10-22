package org.openmrs.module.mksreports.common;

import org.openmrs.annotation.AddOnStartup;
import org.openmrs.annotation.HasAddOnStartupPrivileges;

@HasAddOnStartupPrivileges
public class MksReportPrivilegeConstants {
	
	@AddOnStartup(description = "Able to view patient history report")
	public static final String VIEW_PATIENT_HISTORY = "Can View Patient History";
	
}
