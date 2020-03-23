package org.openmrs.module.commonreports;

import org.openmrs.module.reporting.report.manager.BaseReportManager;

public abstract class ActivatedReportManager extends BaseReportManager {
	
	/**
	 * Tells whether the report manager should be setup by the Reporting module.
	 */
	public boolean isActivated() {
		return true;
	}
}
