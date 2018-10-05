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
		
		CohortCrossTabDataSetDefinition OPDConsult = new CohortCrossTabDataSetDefinition();
		OPDConsult.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(OPDConsult));
		
		Concept allDiags = inizService.getConceptFromKey("report.opdconsult.diagnosesList.concept");
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		parameterMappings.put("locationList", "${locationList}");
		
		// Add a row for each member of allDiags concept
		for (Concept member : allDiags.getSetMembers()) {
			CodedObsCohortDefinition diag = new CodedObsCohortDefinition();
			diag.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
			diag.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
			diag.addParameter(new Parameter("locationList", "Visit Location", Location.class, List.class, null));
			diag.setOperator(SetComparator.IN);
			diag.setQuestion(inizService.getConceptFromKey("report.opdconsult.diagnosisQuestion.concept"));
			
			diag.setValueList(Arrays.asList(member));
			OPDConsult.addRow(member.getDisplayString(), diag, parameterMappings);
		}
		
		setColumnNames();
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		
		AgeCohortDefinition zeroTo1Month = new AgeCohortDefinition();
		zeroTo1Month.setMinAge(0);
		zeroTo1Month.setMinAgeUnit(DurationUnit.DAYS);
		zeroTo1Month.setMaxAge(1);
		zeroTo1Month.setMaxAgeUnit(DurationUnit.MONTHS);
		OPDConsult.addColumn(col1, createCohortComposition(zeroTo1Month, males), null);
		OPDConsult.addColumn(col2, createCohortComposition(zeroTo1Month, females), null);
		
		AgeCohortDefinition oneMonthTo1Year = new AgeCohortDefinition();
		oneMonthTo1Year.setMinAge(1);
		oneMonthTo1Year.setMinAgeUnit(DurationUnit.MONTHS);
		oneMonthTo1Year.setMaxAge(11);
		oneMonthTo1Year.setMaxAgeUnit(DurationUnit.MONTHS);
		OPDConsult.addColumn(col3, createCohortComposition(oneMonthTo1Year, males), null);
		OPDConsult.addColumn(col4, createCohortComposition(oneMonthTo1Year, females), null);
		
		AgeCohortDefinition oneYearTo5Years = new AgeCohortDefinition();
		oneYearTo5Years.setMinAge(1);
		oneYearTo5Years.setMinAgeUnit(DurationUnit.YEARS);
		oneYearTo5Years.setMaxAge(4);
		oneYearTo5Years.setMaxAgeUnit(DurationUnit.YEARS);
		OPDConsult.addColumn(col5, createCohortComposition(oneYearTo5Years, males), null);
		OPDConsult.addColumn(col6, createCohortComposition(oneYearTo5Years, females), null);
		
		AgeCohortDefinition fiveYearsTo15Years = new AgeCohortDefinition();
		fiveYearsTo15Years.setMinAge(5);
		fiveYearsTo15Years.setMinAgeUnit(DurationUnit.YEARS);
		fiveYearsTo15Years.setMaxAge(14);
		fiveYearsTo15Years.setMaxAgeUnit(DurationUnit.YEARS);
		OPDConsult.addColumn(col7, createCohortComposition(fiveYearsTo15Years, males), null);
		OPDConsult.addColumn(col8, createCohortComposition(fiveYearsTo15Years, females), null);
		
		AgeCohortDefinition fifteenYearsTo25Years = new AgeCohortDefinition();
		fifteenYearsTo25Years.setMinAge(15);
		fifteenYearsTo25Years.setMinAgeUnit(DurationUnit.YEARS);
		fifteenYearsTo25Years.setMaxAge(24);
		fifteenYearsTo25Years.setMaxAgeUnit(DurationUnit.YEARS);
		OPDConsult.addColumn(col9, createCohortComposition(fifteenYearsTo25Years, males), null);
		OPDConsult.addColumn(col10, createCohortComposition(fifteenYearsTo25Years, females), null);
		
		AgeCohortDefinition twentyFiveYearsTo50Years = new AgeCohortDefinition();
		twentyFiveYearsTo50Years.setMinAge(25);
		twentyFiveYearsTo50Years.setMinAgeUnit(DurationUnit.YEARS);
		twentyFiveYearsTo50Years.setMaxAge(49);
		twentyFiveYearsTo50Years.setMaxAgeUnit(DurationUnit.YEARS);
		OPDConsult.addColumn(col11, createCohortComposition(twentyFiveYearsTo50Years, males), null);
		OPDConsult.addColumn(col12, createCohortComposition(twentyFiveYearsTo50Years, females), null);
		
		AgeCohortDefinition fiftyYearsTo65Years = new AgeCohortDefinition();
		fiftyYearsTo65Years.setMinAge(50);
		fiftyYearsTo65Years.setMinAgeUnit(DurationUnit.YEARS);
		fiftyYearsTo65Years.setMaxAge(64);
		fiftyYearsTo65Years.setMaxAgeUnit(DurationUnit.YEARS);
		OPDConsult.addColumn(col13, createCohortComposition(fiftyYearsTo65Years, males), null);
		OPDConsult.addColumn(col14, createCohortComposition(fiftyYearsTo65Years, females), null);
		
		AgeCohortDefinition moreThanSixtyFiveYears = new AgeCohortDefinition();
		moreThanSixtyFiveYears.setMinAge(65);
		moreThanSixtyFiveYears.setMinAgeUnit(DurationUnit.YEARS);
		moreThanSixtyFiveYears.setMaxAge(200);
		moreThanSixtyFiveYears.setMaxAgeUnit(DurationUnit.YEARS);
		OPDConsult.addColumn(col15, createCohortComposition(moreThanSixtyFiveYears, males), null);
		OPDConsult.addColumn(col16, createCohortComposition(moreThanSixtyFiveYears, females), null);
		
		// Total column
		GenderCohortDefinition total = new GenderCohortDefinition();
		total.setFemaleIncluded(true);
		total.setMaleIncluded(true);
		total.setUnknownGenderIncluded(true);
		OPDConsult.addColumn(col17, createCohortComposition(total, males), null);
		OPDConsult.addColumn(col18, createCohortComposition(total, females), null);
		OPDConsult.addColumn(col23, createCohortComposition(total), null);
		
		// Referred To column
		CodedObsCohortDefinition referredTo = new CodedObsCohortDefinition();
		referredTo.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		referredTo.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		referredTo.setOperator(SetComparator.IN);
		referredTo.setQuestion(inizService.getConceptFromKey("report.opdconsult.referredTo.concept"));
		OPDConsult.addColumn(col19, createCohortComposition(referredTo, males), null);
		OPDConsult.addColumn(col20, createCohortComposition(referredTo, females), null);
		
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
