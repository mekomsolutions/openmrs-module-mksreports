/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.mksreports;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.mksreports.patientsummary.patienthistory.PatientHistoryReportManager;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;

/**
 * This class contains the logic that is run every time this module is either started or shutdown
 */
public class MKSReportsActivator extends BaseModuleActivator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	/**
	 * @see #started()
	 */
	public void started() {
		log.info("Started " + MKSReportsConstants.MODULE_NAME);
		
		// TODO: Descomentar essa logica
		
		// for (MKSReportManager reportManager :
		// Context.getRegisteredComponents(MKSReportManager.class)) {
		// if (reportManager.isActive()) {
		// log.info("Setting up report " + reportManager.getName() + "...");
		// ReportManagerUtil.setupReport(reportManager); // if this fails the module
		// won't start altogether
		// }
		// }
		
		this.registerPatientHistoryReport();
	}
	
	/**
	 * @see #shutdown()
	 */
	public void shutdown() {
		log.info("Shutdown " + MKSReportsConstants.MODULE_NAME);
	}
	
	/**
	 * Allows to automatically register report definitions at when the module is started
	 * 
	 * @throws Exception
	 */
	private void registerPatientHistoryReport() {
		try {
			
			PatientHistoryReportManager ps = new PatientHistoryReportManager();
			ps.delete();
			ps.setup();
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
