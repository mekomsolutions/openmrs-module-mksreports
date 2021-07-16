package org.openmrs.module.commonreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.commonreports.CommonReportsConstants;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(CommonReportsConstants.COMPONENT_REPORTMANAGER_VACCINATION)
public class VaccinationReportManager extends ActivatedReportManager {
	
	@Autowired
	private InitializerService inizService;
	
	@Autowired
	private ConceptService conceptService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.vaccination.active", false);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "a7fb98da-0386-47c0-8b7f-f5377b1df8d3";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.vaccination.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.vaccination.reportDescription");
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
		
		CohortCrossTabDataSetDefinition vaccination = new CohortCrossTabDataSetDefinition();
		vaccination.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(vaccination));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		
		String[] vaccinationList = inizService.getValueFromKey("report.vaccinationList").split(",");
		
		for (String member : vaccinationList) {
			
			String[] bits = member.split(" ");
			String lastOne = bits[bits.length - 1];
			if (!NumberUtils.isNumber(lastOne)) {
				String sqlQuery = "SELECT person_id FROM obs where obs_group_id IN (SELECT obs_group_id FROM obs where concept_id= (select DISTINCT CONCEPT_ID from concept where uuid='"
				        + inizService.getValueFromKey("report.vaccinations")
				        + "') and value_coded=(select DISTINCT CONCEPT_ID from concept where uuid='" + member + "'));";
				SqlCohortDefinition sql = new SqlCohortDefinition(sqlQuery);
				sql.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
				sql.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
				
				vaccination.addRow(conceptService.getConceptByUuid(member).getDisplayString(), sql, parameterMappings);
				
			} else {
				int lastIndex = Integer.parseInt(lastOne);
				String vacName = member.substring(0, member.lastIndexOf(" "));
				
				String sqlQuery = "SELECT person_id FROM obs where obs_group_id IN (SELECT obs_group_id FROM obs where concept_id= (select DISTINCT CONCEPT_ID from concept where uuid='"
				        + inizService.getValueFromKey("report.vaccinations")
				        + "') and value_coded=(select DISTINCT CONCEPT_ID from concept where uuid='" + vacName
				        + "')) AND ((concept_id=(select DISTINCT CONCEPT_ID from concept where uuid='"
				        + inizService.getValueFromKey("report.vaccinationSequenceNumber") + "') AND value_numeric="
				        + lastIndex + ") OR (concept_id=(select DISTINCT CONCEPT_ID from concept where uuid='"
				        + inizService.getValueFromKey("report.boosterSequenceNumber") + "') AND value_numeric=" + lastIndex
				        + "))";
				SqlCohortDefinition sql = new SqlCohortDefinition(sqlQuery);
				sql.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
				sql.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
				
				vaccination.addRow(conceptService.getConceptByUuid(vacName).getDisplayString() + " " + lastIndex, sql,
				    parameterMappings);
				
			}
			
		}
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		
		AgeCohortDefinition _0mTo1y = new AgeCohortDefinition();
		_0mTo1y.setMinAge(0);
		_0mTo1y.setMinAgeUnit(DurationUnit.MONTHS);
		_0mTo1y.setMaxAge(11);
		_0mTo1y.setMaxAgeUnit(DurationUnit.MONTHS);
		
		AgeCohortDefinition _1To2y = new AgeCohortDefinition();
		_1To2y.setMinAge(11);
		_1To2y.setMinAgeUnit(DurationUnit.MONTHS);
		_1To2y.setMaxAge(23);
		_1To2y.setMaxAgeUnit(DurationUnit.MONTHS);
		
		VisitCohortDefinition _prenatal = new VisitCohortDefinition();
		_prenatal.setVisitTypeList(Arrays.asList(
		    Context.getVisitService().getVisitTypeByUuid(inizService.getValueFromKey("report.prenatalVisitType"))));
		
		vaccination.addColumn(col1, createCohortComposition(_0mTo1y, females), null);
		vaccination.addColumn(col2, createCohortComposition(_1To2y, females), null);
		vaccination.addColumn(col3, createCohortComposition(_0mTo1y, males), null);
		vaccination.addColumn(col4, createCohortComposition(_1To2y, males), null);
		vaccination.addColumn(col5, createCohortComposition(_prenatal, females), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		col1 = MessageUtil.translate("commonreports.report.vaccination.ageCategory1.label") + " - "
		        + MessageUtil.translate("commonreports.report.vaccination.females.label");
		col2 = MessageUtil.translate("commonreports.report.vaccination.ageCategory2.label") + " - "
		        + MessageUtil.translate("commonreports.report.vaccination.females.label");
		col3 = MessageUtil.translate("commonreports.report.vaccination.ageCategory1.label") + " - "
		        + MessageUtil.translate("commonreports.report.vaccination.males.label");
		col4 = MessageUtil.translate("commonreports.report.vaccination.ageCategory2.label") + " - "
		        + MessageUtil.translate("commonreports.report.vaccination.males.label");
		col5 = MessageUtil.translate("commonreports.report.vaccination.females.label") + " - "
		        + MessageUtil.translate("commonreports.report.vaccination.prenatal.label");
		
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("ab8b5d26-8d13-4397-9690-0f107ad50adf", reportDefinition));
	}
}
