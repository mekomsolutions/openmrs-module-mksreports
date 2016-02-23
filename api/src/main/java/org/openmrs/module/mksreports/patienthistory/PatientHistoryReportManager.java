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

package org.openmrs.module.mksreports.patienthistory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.mksreports.common.Helper;
import org.openmrs.module.mksreports.data.converter.ConceptDataTypeConverter;
import org.openmrs.module.mksreports.data.converter.ConceptNameConverter;
import org.openmrs.module.mksreports.data.converter.ObsValueFromIdConverter;
import org.openmrs.module.mksreports.data.obs.definition.ObsDatetimeDataDefinition;
import org.openmrs.module.mksreports.dataset.definition.PatientHistoryEncounterAndObsDataSetDefinition;
import org.openmrs.module.mksreports.dataset.definition.PatientHistoryObsAndEncounterDataSetDefinition;
import org.openmrs.module.mksreports.library.BasePatientDataLibrary;
import org.openmrs.module.mksreports.library.DataFactory;
import org.openmrs.module.mksreports.library.EncounterDataLibrary;
import org.openmrs.module.mksreports.library.ObsDataLibrary;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.converter.DateConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.obs.definition.ObsIdDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.service.ReportService;
import org.springframework.stereotype.Component;

@Component
public class PatientHistoryReportManager extends MKSReportsReportManager {
	
	public final static String REPORT_DESIGN_NAME = "mksPatientHistory.xml_";
	protected final static String REPORT_DEFINITION_NAME = "Patient History";
	
	public final static String DATASET_KEY_DEMOGRAPHICS = "demographics";
	public final static String DATASET_KEY_ENCOUNTERS = "encounters";
	
	//@Autowired TODO Reconfigure this annotation after
	private DataFactory dataFactory = new DataFactory();
	
	//@Autowired TODO Reconfigure this annotation after
	private BuiltInPatientDataLibrary builtInPatientData = new BuiltInPatientDataLibrary();
	
	//@Autowired TODO Reconfigure this annotation after
	private EncounterDataLibrary encounterDataLibrary = new EncounterDataLibrary();
	
	//@Autowired TODO Reconfigure this annotation after
	private ObsDataLibrary obsDataLibrary = new ObsDataLibrary();
	
	//@Autowired TODO Reconfigure this annotation after
	private BasePatientDataLibrary basePatientData = new BasePatientDataLibrary();
	
	public void setup() throws Exception {
		
		ReportDefinition reportDef = constructReportDefinition();		
		ReportDesign reportDesign = Helper.createXMLReportDesign(reportDef, REPORT_DESIGN_NAME);
		Helper.saveReportDesign(reportDesign);
	}
	
	public ReportDefinition constructReportDefinition() {
		ReportDefinition reportDef = new ReportDefinition();
		reportDef.setName(REPORT_DEFINITION_NAME);
		
		// Create new dataset definition 
		PatientHistoryEncounterAndObsDataSetDefinition encountersDatasetSetDef = new PatientHistoryEncounterAndObsDataSetDefinition();
		encountersDatasetSetDef.setName("Patient History data set");
		encountersDatasetSetDef.addSortCriteria("encounterDate", SortCriteria.SortDirection.DESC);
		
		PatientDataSetDefinition patientDataSetDef = new PatientDataSetDefinition();
		Map<String, Object> mappings = new HashMap<String, Object>();
		
		MessageSourceService translator = Context.getMessageSourceService();
		
		Locale locale = Context.getLocale(); //TODO Figure out how to use a 'locale' param when getting msgs
		addColumn(patientDataSetDef, translator.getMessage("mksrports.patienthistory.demographics.identifier"),
			builtInPatientData.getPreferredIdentifierIdentifier());
		addColumn(patientDataSetDef, translator.getMessage("mksreports.patienthistory.demographics.firstname"),
			builtInPatientData.getPreferredGivenName());
		addColumn(patientDataSetDef, translator.getMessage("mksreports.patienthistory.demographics.lastname"),
			builtInPatientData.getPreferredFamilyName());
		addColumn(patientDataSetDef, translator.getMessage("mksreports.patienthistory.demographics.dob"),
			basePatientData.getBirthdate());
		addColumn(patientDataSetDef, translator.getMessage("mksreports.patienthistory.demographics.age"),
			basePatientData.getAgeAtEndInYears());
		addColumn(patientDataSetDef, translator.getMessage("mksreports.patienthistory.demographics.gender"),
			builtInPatientData.getGender());
		
		PatientHistoryObsAndEncounterDataSetDefinition obs = new PatientHistoryObsAndEncounterDataSetDefinition();
		obs.addColumn("Enounter uuid", encounterDataLibrary.getUUID(),"", new ObjectFormatter());
		obs.addColumn("Obs date-time", new ObsDatetimeDataDefinition(), "", new DateConverter());
		obs.addColumn("Concept data type", obsDataLibrary.getConceptId(), "", new ConceptDataTypeConverter());
		obs.addColumn("Concept name", obsDataLibrary.getConceptId(), "", new ConceptNameConverter());
		obs.addColumn("Value", new ObsIdDataDefinition(), "", new ObsValueFromIdConverter());
		obs.addSortCriteria("Obs date-time", SortCriteria.SortDirection.DESC);
		
		reportDef.addDataSetDefinition(DATASET_KEY_DEMOGRAPHICS,	patientDataSetDef, mappings);
		reportDef.addDataSetDefinition(DATASET_KEY_ENCOUNTERS,	encountersDatasetSetDef, new HashMap<String, Object>());
		
		Helper.saveReportDefinition(reportDef);
		
		return reportDef;
	}
	
	public void delete() {
		ReportService rs = Context.getService(ReportService.class);
		for (ReportDesign rd : rs.getAllReportDesigns(false)) {
			if (rd.getName().equals(PatientHistoryReportManager.REPORT_DESIGN_NAME)) {
				rs.purgeReportDesign(rd);
			}
		}
		Helper.purgeReportDefinition(REPORT_DEFINITION_NAME);
	}
}