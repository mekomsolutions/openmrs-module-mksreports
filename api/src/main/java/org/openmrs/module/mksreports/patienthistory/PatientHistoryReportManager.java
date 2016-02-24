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
import org.openmrs.module.mksreports.data.converter.VisitLocationFromIdConverter;
import org.openmrs.module.mksreports.data.converter.VisitUUIDFromIdConverter;
import org.openmrs.module.mksreports.data.obs.definition.ObsDatetimeDataDefinition;
import org.openmrs.module.mksreports.dataset.definition.PatientHistoryEncounterAndVisitDataSetDefinition;
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
	public final static String DATASET_KEY_OBS = "obs";
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
		
		Map<String, Object> mappings = new HashMap<String, Object>();		
		MessageSourceService translator = Context.getMessageSourceService();		
		Locale locale = Context.getLocale(); //TODO Figure out how to use a 'locale' param when getting msgs
		
		// Create dataset definitions 
		PatientHistoryEncounterAndVisitDataSetDefinition encountersDatasetSetDef = createEncounterAndVisitDataSetDefinition();
		PatientDataSetDefinition patientDataSetDef = createPatientDataSetDefinition(translator);
		PatientHistoryObsAndEncounterDataSetDefinition obsDataSetDef = createObsDataSetDefinition();
		
		//Add datasets to the report
		reportDef.addDataSetDefinition(DATASET_KEY_DEMOGRAPHICS,	patientDataSetDef, mappings);
		reportDef.addDataSetDefinition(DATASET_KEY_OBS,	obsDataSetDef, mappings);
		reportDef.addDataSetDefinition(DATASET_KEY_ENCOUNTERS,	encountersDatasetSetDef, new HashMap<String, Object>());
		
		//Save the report definition
		Helper.saveReportDefinition(reportDef);
		
		return reportDef;
	}
	
	/**
	 * @param translator
	 * @return
	 */
	public PatientDataSetDefinition createPatientDataSetDefinition(MessageSourceService translator) {
		PatientDataSetDefinition patientDataSetDef = new PatientDataSetDefinition();
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
		return patientDataSetDef;
	}
	
	/**
	 * @return
	 */
	public PatientHistoryEncounterAndVisitDataSetDefinition createEncounterAndVisitDataSetDefinition() {
		PatientHistoryEncounterAndVisitDataSetDefinition encounterAndVistDatasetSetDef = new PatientHistoryEncounterAndVisitDataSetDefinition();
		encounterAndVistDatasetSetDef.addColumn("Visit uuid", encounterDataLibrary.getVisitId(),"", new VisitUUIDFromIdConverter());
		encounterAndVistDatasetSetDef.addColumn("Visit Location", encounterDataLibrary.getVisitId(),"", new VisitLocationFromIdConverter());
		return encounterAndVistDatasetSetDef;
	}
	
	public final static String		OBS_VALUE_LABEL		= "obs_value";
	protected final static String	OBS_DATETIME_LABEL	= "obs_datetime";
	
	/**
	 * @return
	 */
	public PatientHistoryObsAndEncounterDataSetDefinition createObsDataSetDefinition() {
		PatientHistoryObsAndEncounterDataSetDefinition obsDataSetDef = new PatientHistoryObsAndEncounterDataSetDefinition();
		obsDataSetDef.addColumn("encounter_uuid", encounterDataLibrary.getUUID(),"", new ObjectFormatter());
		obsDataSetDef.addColumn(OBS_DATETIME_LABEL, new ObsDatetimeDataDefinition(), "", new DateConverter());
		obsDataSetDef.addColumn("concept_datatype", obsDataLibrary.getConceptId(), "", new ConceptDataTypeConverter());
		obsDataSetDef.addColumn("concept_name", obsDataLibrary.getConceptId(), "", new ConceptNameConverter());
		obsDataSetDef.addColumn(OBS_VALUE_LABEL, new ObsIdDataDefinition(), "", new ObsValueFromIdConverter());
		obsDataSetDef.addSortCriteria(OBS_DATETIME_LABEL, SortCriteria.SortDirection.DESC);
		return obsDataSetDef;
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