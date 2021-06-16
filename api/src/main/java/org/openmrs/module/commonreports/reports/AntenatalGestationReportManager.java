package org.openmrs.module.commonreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.commonreports.CommonReportsConstants;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(CommonReportsConstants.COMPONENT_REPORTMANAGER_ANTENATALGESTATION)
public class AntenatalGestationReportManager extends ActivatedReportManager {
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.antenatalGestation.active", false);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "b6b2a422-38d1-4fb1-acfc-46eb842bcf7f";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.antenatalGestation.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.antenatalGestation.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", "Start Date", Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", "End Date", Date.class);
	}
	
	public static String col1 = "";
	
	public static String col2 = "";
	
	public static String col3 = "";
	
	public static String col4 = "";
	
	public static String col5 = "";
	
	public static String col6 = "";
	
	@Override
	public List<Parameter> getParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		params.add(getStartDateParameter());
		params.add(getEndDateParameter());
		return params;
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		
		ReportDefinition rd = new ReportDefinition();
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		
		rd.setParameters(getParameters());
		
		CohortCrossTabDataSetDefinition antenatalGestation = new CohortCrossTabDataSetDefinition();
		antenatalGestation.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(antenatalGestation));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		
		ConceptService cs = Context.getConceptService();
		String[] myArray2 = { "0-13", "14-27", "28-40", "Total" };
		
		for (String member : myArray2) {
			
			if (member.equals("Total")) {
				
				SqlCohortDefinition sqd = new SqlCohortDefinition();
				String st = "select person_id from obs where concept_id = (select DISTINCT concept_id from concept_name where name = 'Number of Weeks' and locale='en' and concept_name_type='FULLY_SPECIFIED' and voided = 0)  AND obs_group_id IN (select obs_id from obs where concept_id = (select DISTINCT concept_id from concept_name where name = 'Estimated Gestational Age' and locale='en' and concept_name_type='FULLY_SPECIFIED' and voided = 0))";
				sqd.setQuery(st);
				sqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
				sqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
				antenatalGestation.addRow(member, sqd, parameterMappings);
			} else {
				
				String[] bit = member.split("-");
				String firstNumber = bit[0];
				String lastNumber = bit[1];
				
				SqlCohortDefinition sqd = new SqlCohortDefinition();
				String st = "select person_id from obs where concept_id = (select DISTINCT concept_id from concept_name where name = 'Number of Weeks' and locale='en' and concept_name_type='FULLY_SPECIFIED' and voided = 0) and (value_numeric BETWEEN "
				        + firstNumber + " AND " + lastNumber
				        + ") AND obs_group_id IN (select obs_id from obs where concept_id = (select DISTINCT concept_id from concept_name where name = 'Estimated Gestational Age' and locale='en' and concept_name_type='FULLY_SPECIFIED' and voided = 0))";
				sqd.setQuery(st);
				sqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
				sqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
				antenatalGestation.addRow(
				    member + " " + MessageUtil.translate("commonreports.report.antenatalGestation.gestationWeeks"), sqd,
				    parameterMappings);
				
			}
			
		}
		
		setColumnNames();
		
		// First Visit To column
		CodedObsCohortDefinition firstVisit = new CodedObsCohortDefinition();
		firstVisit.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		firstVisit.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		firstVisit.setOperator(SetComparator.IN);
		firstVisit.setQuestion(cs.getConcept("Numéro de la visite"));
		firstVisit.addValue(cs.getConcept("Une"));
		antenatalGestation.addColumn(col1, createCohortComposition(firstVisit), null);
		
		// Second Visit column
		CodedObsCohortDefinition secondVisit = new CodedObsCohortDefinition();
		secondVisit.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		secondVisit.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		secondVisit.setOperator(SetComparator.IN);
		secondVisit.setQuestion(cs.getConcept("Numéro de la visite"));
		secondVisit.addValue(cs.getConcept("Deux"));
		antenatalGestation.addColumn(col2, createCohortComposition(secondVisit), null);
		
		// Third Visit column
		CodedObsCohortDefinition thirdVisit = new CodedObsCohortDefinition();
		thirdVisit.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		thirdVisit.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		thirdVisit.setOperator(SetComparator.IN);
		thirdVisit.setQuestion(cs.getConcept("Numéro de la visite"));
		thirdVisit.addValue(cs.getConcept("Trois"));
		antenatalGestation.addColumn(col3, createCohortComposition(thirdVisit), null);
		
		// Fourth Visit column
		CodedObsCohortDefinition fourthVisit = new CodedObsCohortDefinition();
		fourthVisit.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		fourthVisit.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		fourthVisit.setOperator(SetComparator.IN);
		fourthVisit.setQuestion(cs.getConcept("Numéro de la visite"));
		fourthVisit.addValue(cs.getConcept("Quatre"));
		antenatalGestation.addColumn(col4, createCohortComposition(fourthVisit), null);
		
		// Fifth Visit column
		CodedObsCohortDefinition fifthVisit = new CodedObsCohortDefinition();
		fifthVisit.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		fifthVisit.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		fifthVisit.setOperator(SetComparator.IN);
		fifthVisit.setQuestion(cs.getConcept("Numéro de la visite"));
		fifthVisit.addValue(cs.getConcept("Cinq plus"));
		antenatalGestation.addColumn(col5, createCohortComposition(fifthVisit), null);
		
		// Total Visit column
		CodedObsCohortDefinition totalVisit = new CodedObsCohortDefinition();
		antenatalGestation.addColumn(col6, createCohortComposition(totalVisit), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		
		col1 = MessageUtil.translate("commonreports.report.antenatalGestation.firstVisit.label");
		col2 = MessageUtil.translate("commonreports.report.antenatalGestation.secondVisit.label");
		col3 = MessageUtil.translate("commonreports.report.antenatalGestation.thirdVisit.label");
		col4 = MessageUtil.translate("commonreports.report.antenatalGestation.fourthVisit.label");
		col5 = MessageUtil.translate("commonreports.report.antenatalGestation.fifthVisit.label");
		col6 = MessageUtil.translate("commonreports.report.antenatalGestation.total.label");
		
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("a0ebe3c1-d6a3-4761-bff0-6a03626d2c75", reportDefinition));
	}
}
