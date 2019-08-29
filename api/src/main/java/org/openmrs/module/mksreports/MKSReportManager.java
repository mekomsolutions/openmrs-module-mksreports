package org.openmrs.module.mksreports;

import org.openmrs.module.reporting.report.manager.BaseReportManager;

public abstract class MKSReportManager extends BaseReportManager {
	
	/**
	 * Tells whether the reports should be processed or not by the activator.
	 */
	public boolean isActive() {
		return true;
	}
}
