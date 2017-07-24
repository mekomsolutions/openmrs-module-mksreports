package org.openmrs.module.mksreports.definition.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.mksreports.MKSReportsConstants;
import org.openmrs.module.mksreports.query.VisitWithinDateRangeQuery;
import org.openmrs.module.reporting.common.Age;
import org.openmrs.module.reporting.common.AgeRange;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.converter.AgeRangeConverter;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.AgeDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.data.visit.library.BuiltInVisitDataLibrary;
import org.openmrs.module.reporting.dataset.definition.VisitDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.BaseReportManager;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OutpatientRecordBook extends BaseReportManager {
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private BuiltInPatientDataLibrary builtInPatientData;
	
	@Autowired
	private BuiltInVisitDataLibrary builtInVisitData;
	
	@Override
	public String getUuid() {
		return "6c74e2ab-0e9b-4469-8901-8221f7d4b498";
	}
	
	@Override
	public String getName() {
		return "HIS Outpatient Record Book";
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
	
	@Override
	public List<Parameter> getParameters() {
		List<Parameter> params = new ArrayList<Parameter>();
		params.add(getStartDateParameter());
		params.add(getEndDateParameter());
		return params;
	}
	
	public OutpatientRecordBook() {
	};
	
	@Override
	public ReportDefinition constructReportDefinition() {
		
		ReportDefinition rd = new ReportDefinition();
		rd.setUuid(getUuid());
		rd.setName(getName());
		rd.setDescription(getDescription());
		
		rd.setParameters(getParameters());
		
		VisitDataSetDefinition vdsd = new VisitDataSetDefinition();
		vdsd.addParameters(getParameters());
		rd.addDataSetDefinition("visits", Mapped.mapStraightThrough(vdsd));
		
		VisitWithinDateRangeQuery query = new VisitWithinDateRangeQuery();
		vdsd.addRowFilter(query, ObjectUtil.toString(Mapped.straightThroughMappings(query), "=", ","));
		
		// Visit ID
		VisitDataDefinition vdd = builtInVisitData.getVisitId();
		vdsd.addColumn("Visit ID", vdd, ObjectUtil.toString(Mapped.straightThroughMappings(vdd), "=", ","));
		
		// Patient ID
		vdsd.addColumn("Patient ID", builtInPatientData.getPatientId(),
		    ObjectUtil.toString(Mapped.straightThroughMappings(builtInPatientData.getPatientId()), "=", ","));
		
		// Patient Identifier
		PatientIdentifierType type = patientService.getPatientIdentifierTypeByUuid(Context.getAdministrationService()
		        .getGlobalProperty(MKSReportsConstants.GP_PATIENT_IDENTIFIER_TYPE_UUID));
		PatientIdentifierDataDefinition pidd = new PatientIdentifierDataDefinition();
		pidd.setTypes(Arrays.asList(type));
		
		vdsd.addColumn(MessageUtil.translate("${project.parent.artifactId}.report.registerLogbook.identifier.label"), pidd,
		    ObjectUtil.toString(Mapped.straightThroughMappings(pidd), "=", ","));
		
		// Age Categories
		AgeDataDefinition ageDD = new AgeDataDefinition();
		
		String isOfCategoryLabel = MessageUtil.translate("mksreports.report.registerLogbook.isOfCategory.label");
		
		AgeRangeConverter ageConverter1 = new AgeRangeConverter();
		ageConverter1.addAgeRange(new AgeRange(0, Age.Unit.MONTHS, 1, Age.Unit.MONTHS, isOfCategoryLabel));
		AgeRangeConverter ageConverter2 = new AgeRangeConverter();
		ageConverter2.addAgeRange(new AgeRange(1, Age.Unit.MONTHS, 12, Age.Unit.MONTHS, isOfCategoryLabel));
		AgeRangeConverter ageConverter3 = new AgeRangeConverter();
		ageConverter3.addAgeRange(new AgeRange(1, Age.Unit.YEARS, 4, Age.Unit.YEARS, isOfCategoryLabel));
		AgeRangeConverter ageConverter4 = new AgeRangeConverter();
		ageConverter4.addAgeRange(new AgeRange(5, Age.Unit.YEARS, 14, Age.Unit.YEARS, isOfCategoryLabel));
		AgeRangeConverter ageConverter5 = new AgeRangeConverter();
		ageConverter5.addAgeRange(new AgeRange(15, Age.Unit.YEARS, 25, Age.Unit.YEARS, isOfCategoryLabel));
		AgeRangeConverter ageConverter6 = new AgeRangeConverter();
		ageConverter6.addAgeRange(new AgeRange(25, Age.Unit.YEARS, 50, Age.Unit.YEARS, isOfCategoryLabel));
		AgeRangeConverter ageConverter7 = new AgeRangeConverter();
		ageConverter7.addAgeRange(new AgeRange(50, Age.Unit.YEARS, 65, Age.Unit.YEARS, isOfCategoryLabel));
		AgeRangeConverter ageConverter8 = new AgeRangeConverter();
		ageConverter8.addAgeRange(new AgeRange(65, Age.Unit.YEARS, 999, Age.Unit.YEARS, isOfCategoryLabel));
		
		vdsd.addColumn(MessageUtil.translate("mksreports.report.registerLogbook.ageCategory1.label"), ageDD, (String) null,
		    ageConverter1);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.registerLogbook.ageCategory2.label"), ageDD, (String) null,
		    ageConverter2);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.registerLogbook.ageCategory3.label"), ageDD, (String) null,
		    ageConverter3);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.registerLogbook.ageCategory4.label"), ageDD, (String) null,
		    ageConverter4);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.registerLogbook.ageCategory5.label"), ageDD, (String) null,
		    ageConverter5);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.registerLogbook.ageCategory6.label"), ageDD, (String) null,
		    ageConverter6);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.registerLogbook.ageCategory7.label"), ageDD, (String) null,
		    ageConverter7);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.registerLogbook.ageCategory8.label"), ageDD, (String) null,
		    ageConverter8);
		
		return rd;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		ReportDesign design = ReportManagerUtil.createCsvReportDesign("9873e45d-f8a0-4682-be78-243b8c9b848c",
		    reportDefinition);
		List<ReportDesign> list = new ArrayList<ReportDesign>();
		list.add(design);
		return list;
	}
	
	@Override
	public String getVersion() {
		return "0.1.0-SNAPSHOT";
	}
	
}
