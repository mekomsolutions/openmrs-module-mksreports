/*
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


package org.openmrs.module.mksreports.patientsummary;

import java.util.HashMap;
import java.util.Properties;

import org.openmrs.api.context.Context;
import org.openmrs.module.mksreports.common.Helper;
import org.openmrs.module.reporting.dataset.definition.EncounterAndObsDataSetDefinition;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;

/**
 * Base implementation of ReportManager that provides some common method implementations
 */
//@Component
public class GenericPatientSummary {

	public void setup() throws Exception {

		ReportDefinition rd = constructReportDefinition();	
		
	    ReportDesign design = Helper.createExcelDesign(rd, "mekomPatientSummary.xls_", false);

		Properties props = new Properties();
		props.put("repeatingSections", "sheet:1,row:9,dataset:patient");
		props.put("sortWeight","5000");
		design.setProperties(props);

		Helper.saveReportDesign(design);
	}

	public ReportDefinition constructReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Mekom Patient Summary");

		// Create new dataset definition 
		EncounterAndObsDataSetDefinition dataSetDefinition = new EncounterAndObsDataSetDefinition();
		dataSetDefinition.setName("Mks Data Set");

		reportDefinition.addDataSetDefinition("patient", dataSetDefinition, new HashMap<String, Object>());		
		Helper.saveReportDefinition(reportDefinition);

		return reportDefinition;
	}

	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("mekomPatientSummary.xls_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Mekome Patient Summary");
	}
}
