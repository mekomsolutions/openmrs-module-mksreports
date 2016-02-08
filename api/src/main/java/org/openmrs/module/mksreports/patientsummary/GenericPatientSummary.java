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
import java.util.Map;

import org.openmrs.api.context.Context;
import org.openmrs.module.mksreports.common.Helper;
import org.openmrs.module.mksreports.dataset.definition.MksEncounterAndObsDataSetDefinition;
import org.openmrs.module.mksreports.library.BasePatientDataLibrary;
import org.openmrs.module.mksreports.library.DataFactory;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.springframework.stereotype.Component;

@Component
public class GenericPatientSummary extends MksReportManager{
	
	//@Autowired TODO Reconfigure this annotation after
	private DataFactory df = new DataFactory();
	
	//@Autowired TODO Reconfigure this annotation after
	private BuiltInPatientDataLibrary builtInPatientData = new BuiltInPatientDataLibrary();
	
	//@Autowired TODO Reconfigure this annotation after
	private BasePatientDataLibrary basePatientData = new BasePatientDataLibrary();
	
	public void setup() throws Exception {
		
		ReportDefinition rd = constructReportDefinition();		
		ReportDesign design = Helper.createXMLReportDesign(rd, "mekomPatientSummary.xml_");
		Helper.saveReportDesign(design);
	}
	
	public ReportDefinition constructReportDefinition() {
		ReportDefinition reportDefinition = new ReportDefinition();
		reportDefinition.setName("Mekom Patient Summary");
		
		// Create new dataset definition 
		MksEncounterAndObsDataSetDefinition dataSetDefinition = new MksEncounterAndObsDataSetDefinition();
		dataSetDefinition.setName("Mks Data Set");
		dataSetDefinition.addSortCriteria("encounterDate", SortCriteria.SortDirection.ASC);
		
		
		PatientDataSetDefinition dsd = new PatientDataSetDefinition();
		Map<String, Object> mappings = new HashMap<String, Object>();


		addColumn(dsd, "patient_id", builtInPatientData.getPatientId());
		addColumn(dsd, "given_name", builtInPatientData.getPreferredGivenName());
		addColumn(dsd, "last_name", builtInPatientData.getPreferredFamilyName());
		addColumn(dsd, "birthdate", basePatientData.getBirthdate());
		addColumn(dsd, "current_age_yr", basePatientData.getAgeAtEndInYears());
		addColumn(dsd, "gender", builtInPatientData.getGender());
		
		
		reportDefinition.addDataSetDefinition("demographics", dsd, mappings);
		reportDefinition.addDataSetDefinition("encounters", dataSetDefinition, new HashMap<String, Object>());
		
		Helper.saveReportDefinition(reportDefinition);
		
		return reportDefinition;
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if ("mekomPatientSummary.xml_".equals(rd.getName())) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition("Mekome Patient Summary");
	}
}
