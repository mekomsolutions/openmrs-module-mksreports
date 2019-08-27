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
package org.openmrs.module.mksreports;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.mksreports.patientsummary.patienthistory.PatientHistoryReportManager;

/**
 * This class contains the logic that is run every time this module is either
 * started or stopped.
 */
public class MKSReportsActivator extends BaseModuleActivator {

	protected Log log = LogFactory.getLog(getClass());

	/**
	 * @see ModuleActivator#started()
	 */
	public void started() {

		try {
			registerReports();
		} catch (Exception e) {
			e.printStackTrace();
		}
		log.info("Mekom Solutions Reports Module started");
	}

	/**
	 * 
	 * Allows to automatically register report definitions at when the module is
	 * started
	 * 
	 * @throws Exception
	 */
	public void registerReports() throws Exception {
		PatientHistoryReportManager ps = new PatientHistoryReportManager();
		ps.delete();
		ps.setup();
	}

}
