package org.openmrs.module.mksreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.mksreports.MKSReportManager;
import org.openmrs.module.mksreports.MKSReportsConstants;
import org.openmrs.module.mksreports.dataset.definition.ObsSummaryRowDataSetDefinition;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
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

@Component(MKSReportsConstants.COMPONENT_REPORTMANAGER_OPDCONSULT)
public class OutpatientConsultationReportManager extends MKSReportManager {
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public boolean isActive() {
		return inizService.getBooleanFromKey("report.opdconsult.active", true);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "58d7a2ba-5b62-4e21-ac21-090e3758cce7";
	}
	
	@Override
	public String getName() {
		return "HC1 Outpatient Consultation";
	}
	
	@Override
	public String getDescription() {
		return "";
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", "Start Date", Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", "End Date", Date.class);
	}
	
	private Parameter getLocationParameter() {
		return new Parameter("locationList", "Visit Location", Location.class, List.class, null);
	}
	
	public static String col1 = "";
	
	public static String col2 = "";
	
	public static String col3 = "";
	
	public static String col4 = "";
	
	public static String col5 = "";
	
	public static String col6 = "";
	
	public static String col7 = "";
	
	public static String col8 = "";
	
	public static String col9 = "";
	
	public static String col10 = "";
	
	public static String col11 = "";
	
	public static String col12 = "";
	
	public static String col13 = "";
	
	public static String col14 = "";
	
	public static String col15 = "";
	
	public static String col16 = "";
	
	public static String col17 = "";
	
	public static String col18 = "";
	
	public static String col19 = "";
	
	public static String col20 = "";
	
	public static String col21 = "";
	
	public static String col22 = "";
	
	public static String col23 = "";
	
	@Override
	public List<Parameter> getParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		params.add(getStartDateParameter());
		params.add(getEndDateParameter());
		params.add(getLocationParameter());
		return params;
	}
	
	@Override
	public ReportDefinition constructReportDefinition() {
		
		ReportDefinition rd = new ReportDefinition();
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		
		rd.setParameters(getParameters());
		
		CohortCrossTabDataSetDefinition opdConsult = new CohortCrossTabDataSetDefinition();
		opdConsult.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(opdConsult));
		
		Concept allDiags = inizService.getConceptFromKey("report.opdconsult.diagnoses.conceptSet");
		
		ObsSummaryRowDataSetDefinition obsSummaryDS = new ObsSummaryRowDataSetDefinition();
		obsSummaryDS.addParameter(new Parameter("diagnosisList", "List of Diagnosis", Concept.class, List.class, null));
		obsSummaryDS.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		obsSummaryDS.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		obsSummaryDS.addParameter(new Parameter("questionConceptIds", "Question Concept", Integer.class, List.class, null));
		rd.addDataSetDefinition("Obs Summary", Mapped.mapStraightThrough(obsSummaryDS));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		parameterMappings.put("locationList", "${locationList}");
		
		List<Concept> questionConcepts = inizService.getConceptsFromKey("report.opdconsult.diagnosisQuestion.concepts");
		for (Concept question : questionConcepts) {
			for (Concept member : allDiags.getSetMembers()) {
				CodedObsCohortDefinition diag = new CodedObsCohortDefinition();
				diag.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
				diag.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
				diag.addParameter(new Parameter("locationList", "Visit Location", Location.class, List.class, null));
				diag.setOperator(SetComparator.IN);
				diag.setQuestion(question);
				
				diag.setValueList(Arrays.asList(member));
				opdConsult.addRow(member.getDisplayString(), diag, parameterMappings);
			}
		}
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		
		AgeCohortDefinition _0To1m = new AgeCohortDefinition();
		_0To1m.setMinAge(0);
		_0To1m.setMinAgeUnit(DurationUnit.DAYS);
		_0To1m.setMaxAge(1);
		_0To1m.setMaxAgeUnit(DurationUnit.MONTHS);
		opdConsult.addColumn(col1, createCohortComposition(_0To1m, males), null);
		opdConsult.addColumn(col2, createCohortComposition(_0To1m, females), null);
		obsSummaryDS.addColumn(col1, createCohortComposition(_0To1m, males));
		obsSummaryDS.addColumn(col2, createCohortComposition(_0To1m, females));
		
		AgeCohortDefinition _1mTo1y = new AgeCohortDefinition();
		_1mTo1y.setMinAge(1);
		_1mTo1y.setMinAgeUnit(DurationUnit.MONTHS);
		_1mTo1y.setMaxAge(11);
		_1mTo1y.setMaxAgeUnit(DurationUnit.MONTHS);
		opdConsult.addColumn(col3, createCohortComposition(_1mTo1y, males), null);
		opdConsult.addColumn(col4, createCohortComposition(_1mTo1y, females), null);
		obsSummaryDS.addColumn(col3, createCohortComposition(_1mTo1y, males));
		obsSummaryDS.addColumn(col4, createCohortComposition(_1mTo1y, females));
		
		AgeCohortDefinition _1To5y = new AgeCohortDefinition();
		_1To5y.setMinAge(1);
		_1To5y.setMinAgeUnit(DurationUnit.YEARS);
		_1To5y.setMaxAge(4);
		_1To5y.setMaxAgeUnit(DurationUnit.YEARS);
		opdConsult.addColumn(col5, createCohortComposition(_1To5y, males), null);
		opdConsult.addColumn(col6, createCohortComposition(_1To5y, females), null);
		obsSummaryDS.addColumn(col5, createCohortComposition(_1To5y, males));
		obsSummaryDS.addColumn(col6, createCohortComposition(_1To5y, females));
		
		AgeCohortDefinition _5To15y = new AgeCohortDefinition();
		_5To15y.setMinAge(5);
		_5To15y.setMinAgeUnit(DurationUnit.YEARS);
		_5To15y.setMaxAge(14);
		_5To15y.setMaxAgeUnit(DurationUnit.YEARS);
		opdConsult.addColumn(col7, createCohortComposition(_5To15y, males), null);
		opdConsult.addColumn(col8, createCohortComposition(_5To15y, females), null);
		obsSummaryDS.addColumn(col7, createCohortComposition(_5To15y, males));
		obsSummaryDS.addColumn(col8, createCohortComposition(_5To15y, females));
		
		AgeCohortDefinition _15To25y = new AgeCohortDefinition();
		_15To25y.setMinAge(15);
		_15To25y.setMinAgeUnit(DurationUnit.YEARS);
		_15To25y.setMaxAge(24);
		_15To25y.setMaxAgeUnit(DurationUnit.YEARS);
		opdConsult.addColumn(col9, createCohortComposition(_15To25y, males), null);
		opdConsult.addColumn(col10, createCohortComposition(_15To25y, females), null);
		obsSummaryDS.addColumn(col9, createCohortComposition(_15To25y, males));
		obsSummaryDS.addColumn(col10, createCohortComposition(_15To25y, females));
		
		AgeCohortDefinition _25To50y = new AgeCohortDefinition();
		_25To50y.setMinAge(25);
		_25To50y.setMinAgeUnit(DurationUnit.YEARS);
		_25To50y.setMaxAge(49);
		_25To50y.setMaxAgeUnit(DurationUnit.YEARS);
		opdConsult.addColumn(col11, createCohortComposition(_25To50y, males), null);
		opdConsult.addColumn(col12, createCohortComposition(_25To50y, females), null);
		obsSummaryDS.addColumn(col11, createCohortComposition(_25To50y, males));
		obsSummaryDS.addColumn(col12, createCohortComposition(_25To50y, females));
		
		AgeCohortDefinition _50To65y = new AgeCohortDefinition();
		_50To65y.setMinAge(50);
		_50To65y.setMinAgeUnit(DurationUnit.YEARS);
		_50To65y.setMaxAge(64);
		_50To65y.setMaxAgeUnit(DurationUnit.YEARS);
		opdConsult.addColumn(col13, createCohortComposition(_50To65y, males), null);
		opdConsult.addColumn(col14, createCohortComposition(_50To65y, females), null);
		obsSummaryDS.addColumn(col13, createCohortComposition(_50To65y, males));
		obsSummaryDS.addColumn(col14, createCohortComposition(_50To65y, females));
		
		AgeCohortDefinition moreThan65y = new AgeCohortDefinition();
		moreThan65y.setMinAge(65);
		moreThan65y.setMinAgeUnit(DurationUnit.YEARS);
		moreThan65y.setMaxAge(200);
		moreThan65y.setMaxAgeUnit(DurationUnit.YEARS);
		opdConsult.addColumn(col15, createCohortComposition(moreThan65y, males), null);
		opdConsult.addColumn(col16, createCohortComposition(moreThan65y, females), null);
		obsSummaryDS.addColumn(col15, createCohortComposition(moreThan65y, males));
		obsSummaryDS.addColumn(col16, createCohortComposition(moreThan65y, females));
		
		// Total column
		GenderCohortDefinition total = new GenderCohortDefinition();
		total.setFemaleIncluded(true);
		total.setMaleIncluded(true);
		total.setUnknownGenderIncluded(true);
		opdConsult.addColumn(col17, createCohortComposition(total, males), null);
		opdConsult.addColumn(col18, createCohortComposition(total, females), null);
		opdConsult.addColumn(col23, createCohortComposition(total), null);
		obsSummaryDS.addColumn(col23, createCohortComposition(total));
		
		// Referred To column
		CodedObsCohortDefinition referredTo = new CodedObsCohortDefinition();
		referredTo.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		referredTo.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		referredTo.setOperator(SetComparator.IN);
		referredTo.setQuestion(inizService.getConceptFromKey("report.opdconsult.referredTo.concept"));
		opdConsult.addColumn(col19, createCohortComposition(referredTo, males), null);
		opdConsult.addColumn(col20, createCohortComposition(referredTo, females), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		col1 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory1.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.males.label");
		col2 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory1.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.females.label");
		col3 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory2.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.males.label");
		col4 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory2.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.females.label");
		col5 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory3.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.males.label");
		col6 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory3.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.females.label");
		col7 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory4.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.males.label");
		col8 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory4.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.females.label");
		col9 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory5.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.males.label");
		col10 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory5.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.females.label");
		col11 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory6.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.males.label");
		col12 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory6.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.females.label");
		col13 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory7.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.males.label");
		col14 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory7.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.females.label");
		col15 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory8.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.males.label");
		col16 = MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory8.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.females.label");
		col17 = MessageUtil.translate("mksreports.report.opdconsult.total.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.males.label");
		col18 = MessageUtil.translate("mksreports.report.opdconsult.total.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.females.label");
		col19 = MessageUtil.translate("mksreports.report.opdconsult.referredTo.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.males.label");
		col20 = MessageUtil.translate("mksreports.report.opdconsult.referredTo.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.females.label");
		col21 = MessageUtil.translate("mksreports.report.opdconsult.hefId.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.males.label");
		col22 = MessageUtil.translate("mksreports.report.opdconsult.hefId.label") + " - "
		        + MessageUtil.translate("mksreports.report.opdconsult.females.label");
		col23 = MessageUtil.translate("mksreports.report.opdconsult.total.label");
		
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("42b32ac1-fcd0-473d-8fdb-71fd6fc2e26d", reportDefinition));
	}
}
