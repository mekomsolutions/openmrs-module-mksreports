package org.openmrs.module.commonreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.openmrs.Concept;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PresenceOrAbsenceCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.reporting.common.DurationUnit;
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

@Component
public class ChronicIllnessesReportManager extends ActivatedReportManager {
	
	private String col1 = "";
	
	private String col2 = "";
	
	private String col3 = "";
	
	private String col4 = "";
	
	private String col5 = "";
	
	private String col6 = "";
	
	private String col7 = "";
	
	private String col8 = "";
	
	private String col9 = "";
	
	private String col10 = "";
	
	private String col11 = "";
	
	private String col12 = "";
	
	private String col13 = "";
	
	private String col14 = "";
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.chronicIllnesses.active", false);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "95d1eb9a-a84d-425f-a320-c5b694c0f765";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.chronicIllnesses.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.chronicIllnesses.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", "Start Date", Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", "End Date", Date.class);
	}
	
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
		
		CohortCrossTabDataSetDefinition chronicIllnessesDsd = new CohortCrossTabDataSetDefinition();
		chronicIllnessesDsd.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(chronicIllnessesDsd));
		
		Concept illnesses = inizService.getConceptFromKey("report.chronicIllnesses.conceptSet");
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		parameterMappings.put("startedOnOrAfter", "${startDate}");
		
		// Add new diagnosis & condition rows for each illness concept member
		for (Concept member : illnesses.getSetMembers()) {
			CodedObsCohortDefinition diag = new CodedObsCohortDefinition();
			diag.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
			diag.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
			diag.setOperator(SetComparator.IN);
			diag.setQuestion(inizService.getConceptFromKey("report.chronicIllnesses.diagnosisQuestion.concept"));
			
			String conditionList = "";
			if (!member.getSet()) {
				diag.setValueList(Arrays.asList(member));
				conditionList = member.getId().toString();
			} else {
				diag.setValueList(new ArrayList<Concept>(member.getSetMembers()));
				conditionList = member.getSetMembers().stream().map(Concept::getId).map(Object::toString)
				        .collect(Collectors.joining(","));
			}
			
			SqlCohortDefinition conditions = new SqlCohortDefinition();
			String sql = "SELECT c.patient_id FROM conditions c WHERE c.concept_id IN (" + conditionList + ") "
			        + "AND c.`status`='ACTIVE' AND NOT EXISTS (SELECT 1 FROM conditions new_c "
			        + "WHERE new_c.previous_condition_id = c.condition_id);";
			conditions.setQuery(sql);
			
			PresenceOrAbsenceCohortDefinition absentInConditions = new PresenceOrAbsenceCohortDefinition();
			absentInConditions.addCohortToCheck(Mapped.mapStraightThrough(conditions));
			absentInConditions.setPresentInAtMost(0);
			CompositionCohortDefinition newDiagnosis = createCohortComposition(diag, absentInConditions);
			newDiagnosis.addParameter(new Parameter("startedOnOrAfter", "On Or After", Date.class));
			newDiagnosis.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
			newDiagnosis.addParameter(new Parameter("onOrBefore", "On Or after", Date.class));
			
			VisitCohortDefinition visits = new VisitCohortDefinition();
			visits.addParameter(new Parameter("startedOnOrAfter", "On Or After", Date.class));
			
			CompositionCohortDefinition visitsWithConditions = createCohortComposition(visits, conditions);
			visitsWithConditions.addParameter(new Parameter("startedOnOrAfter", "On Or After", Date.class));
			visitsWithConditions.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
			visitsWithConditions.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
			
			chronicIllnessesDsd.addRow(
			    member.getDisplayString() + " - "
			            + MessageUtil.translate("commonreports.report.chronicIllnesses.diagnoses.label"),
			    newDiagnosis, parameterMappings);
			chronicIllnessesDsd.addRow(
			    member.getDisplayString() + " - "
			            + MessageUtil.translate("commonreports.report.chronicIllnesses.conditions.label"),
			    visitsWithConditions, parameterMappings);
		}
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		
		AgeCohortDefinition _0To9y = new AgeCohortDefinition();
		_0To9y.setMinAge(0);
		_0To9y.setMinAgeUnit(DurationUnit.YEARS);
		_0To9y.setMaxAge(9);
		_0To9y.setMaxAgeUnit(DurationUnit.YEARS);
		chronicIllnessesDsd.addColumn(col1, createCohortComposition(_0To9y, females), null);
		chronicIllnessesDsd.addColumn(col2, createCohortComposition(_0To9y, males), null);
		
		AgeCohortDefinition _10To14y = new AgeCohortDefinition();
		_10To14y.setMinAge(10);
		_10To14y.setMinAgeUnit(DurationUnit.YEARS);
		_10To14y.setMaxAge(14);
		_10To14y.setMaxAgeUnit(DurationUnit.YEARS);
		chronicIllnessesDsd.addColumn(col3, createCohortComposition(_10To14y, females), null);
		chronicIllnessesDsd.addColumn(col4, createCohortComposition(_10To14y, males), null);
		
		AgeCohortDefinition _15To19y = new AgeCohortDefinition();
		_15To19y.setMinAge(15);
		_15To19y.setMinAgeUnit(DurationUnit.YEARS);
		_15To19y.setMaxAge(19);
		_15To19y.setMaxAgeUnit(DurationUnit.YEARS);
		chronicIllnessesDsd.addColumn(col5, createCohortComposition(_15To19y, females), null);
		chronicIllnessesDsd.addColumn(col6, createCohortComposition(_15To19y, males), null);
		
		AgeCohortDefinition _20To24y = new AgeCohortDefinition();
		_20To24y.setMinAge(20);
		_20To24y.setMinAgeUnit(DurationUnit.YEARS);
		_20To24y.setMaxAge(24);
		_20To24y.setMaxAgeUnit(DurationUnit.YEARS);
		chronicIllnessesDsd.addColumn(col7, createCohortComposition(_20To24y, females), null);
		chronicIllnessesDsd.addColumn(col8, createCohortComposition(_20To24y, males), null);
		
		AgeCohortDefinition _25To49y = new AgeCohortDefinition();
		_25To49y.setMinAge(25);
		_25To49y.setMinAgeUnit(DurationUnit.YEARS);
		_25To49y.setMaxAge(49);
		_25To49y.setMaxAgeUnit(DurationUnit.YEARS);
		chronicIllnessesDsd.addColumn(col9, createCohortComposition(_25To49y, females), null);
		chronicIllnessesDsd.addColumn(col10, createCohortComposition(_25To49y, males), null);
		
		AgeCohortDefinition _50yAndAbove = new AgeCohortDefinition();
		_50yAndAbove.setMinAge(50);
		_50yAndAbove.setMinAgeUnit(DurationUnit.YEARS);
		_50yAndAbove.setMaxAge(200);
		_50yAndAbove.setMaxAgeUnit(DurationUnit.YEARS);
		chronicIllnessesDsd.addColumn(col11, createCohortComposition(_50yAndAbove, females), null);
		chronicIllnessesDsd.addColumn(col12, createCohortComposition(_50yAndAbove, males), null);
		
		// Referred To column
		CodedObsCohortDefinition referredTo = new CodedObsCohortDefinition();
		referredTo.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		referredTo.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		referredTo.setOperator(SetComparator.IN);
		referredTo.setQuestion(inizService.getConceptFromKey("report.chronicIllnesses.referredTo.concept"));
		chronicIllnessesDsd.addColumn(col13, createCohortComposition(referredTo, females), null);
		chronicIllnessesDsd.addColumn(col14, createCohortComposition(referredTo, males), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		col1 = MessageUtil.translate("commonreports.report.chronicIllnesses.ageCategory1.label") + " - "
		        + MessageUtil.translate("commonreports.report.chronicIllnesses.females.label");
		col2 = MessageUtil.translate("commonreports.report.chronicIllnesses.ageCategory1.label") + " - "
		        + MessageUtil.translate("commonreports.report.chronicIllnesses.males.label");
		col3 = MessageUtil.translate("commonreports.report.chronicIllnesses.ageCategory2.label") + " - "
		        + MessageUtil.translate("commonreports.report.chronicIllnesses.females.label");
		col4 = MessageUtil.translate("commonreports.report.chronicIllnesses.ageCategory2.label") + " - "
		        + MessageUtil.translate("commonreports.report.chronicIllnesses.males.label");
		col5 = MessageUtil.translate("commonreports.report.chronicIllnesses.ageCategory3.label") + " - "
		        + MessageUtil.translate("commonreports.report.chronicIllnesses.females.label");
		col6 = MessageUtil.translate("commonreports.report.chronicIllnesses.ageCategory3.label") + " - "
		        + MessageUtil.translate("commonreports.report.chronicIllnesses.males.label");
		col7 = MessageUtil.translate("commonreports.report.chronicIllnesses.ageCategory4.label") + " - "
		        + MessageUtil.translate("commonreports.report.chronicIllnesses.females.label");
		col8 = MessageUtil.translate("commonreports.report.chronicIllnesses.ageCategory4.label") + " - "
		        + MessageUtil.translate("commonreports.report.chronicIllnesses.males.label");
		col9 = MessageUtil.translate("commonreports.report.chronicIllnesses.ageCategory5.label") + " - "
		        + MessageUtil.translate("commonreports.report.chronicIllnesses.females.label");
		col10 = MessageUtil.translate("commonreports.report.chronicIllnesses.ageCategory5.label") + " - "
		        + MessageUtil.translate("commonreports.report.chronicIllnesses.males.label");
		col11 = MessageUtil.translate("commonreports.report.chronicIllnesses.ageCategory6.label") + " - "
		        + MessageUtil.translate("commonreports.report.chronicIllnesses.females.label");
		col12 = MessageUtil.translate("commonreports.report.chronicIllnesses.ageCategory6.label") + " - "
		        + MessageUtil.translate("commonreports.report.chronicIllnesses.males.label");
		col13 = MessageUtil.translate("commonreports.report.chronicIllnesses.totalReferredCases.label") + " - "
		        + MessageUtil.translate("commonreports.report.chronicIllnesses.females.label");
		col14 = MessageUtil.translate("commonreports.report.chronicIllnesses.totalReferredCases.label") + " - "
		        + MessageUtil.translate("commonreports.report.chronicIllnesses.males.label");
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("aaaf86d1-4db5-45da-9861-bcfcc6e58994", reportDefinition));
	}
}
