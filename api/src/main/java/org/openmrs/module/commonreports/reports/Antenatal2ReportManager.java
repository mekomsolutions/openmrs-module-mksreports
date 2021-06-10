package org.openmrs.module.commonreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.VisitType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.commonreports.CommonReportsConstants;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.NumericObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.SqlCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.VisitCohortDefinition;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.dataset.definition.CohortCrossTabDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(CommonReportsConstants.COMPONENT_REPORTMANAGER_ANTENATAL2)
public class Antenatal2ReportManager extends ActivatedReportManager {
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.antenatal2.active", false);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "97891fbe-8027-46f2-aea7-20953e54d775";
	}
	
	@Override
	public String getName() {
		return MessageUtil.translate("commonreports.report.antenatal2.reportName");
	}
	
	@Override
	public String getDescription() {
		return MessageUtil.translate("commonreports.report.antenatal2.reportDescription");
	}
	
	private Parameter getStartDateParameter() {
		return new Parameter("startDate", "Start Date", Date.class);
	}
	
	private Parameter getEndDateParameter() {
		return new Parameter("endDate", "End Date", Date.class);
	}
	
	public static String col1 = "";
	
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
		
		CohortCrossTabDataSetDefinition antenatal2 = new CohortCrossTabDataSetDefinition();
		antenatal2.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(antenatal2));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		
		ConceptService cs = Context.getConceptService();
		
		GenderCohortDefinition female = new GenderCohortDefinition();
		female.setFemaleIncluded(true);
		
		// Risky Pregnancies
		CodedObsCohortDefinition riskyPregnancy = new CodedObsCohortDefinition();
		riskyPregnancy.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		riskyPregnancy.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		riskyPregnancy.setOperator(SetComparator.IN);
		riskyPregnancy.setQuestion(cs.getConcept("Grossesse à risque"));
		riskyPregnancy.setValueList(Arrays.asList(cs.getConcept("Oui")));
		antenatal2.addRow(MessageUtil.translate("commonreports.report.antenatal2.riskyPregnancy"), riskyPregnancy,
		    parameterMappings);
		
		// Iron def ANC visit Pregnancies
		CodedObsCohortDefinition anemia = new CodedObsCohortDefinition();
		anemia.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		anemia.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		anemia.setOperator(SetComparator.IN);
		anemia.setQuestion(cs.getConcept("Diagnostic codé"));
		anemia.setValueList(Arrays.asList(cs.getConcept("Anemie, carence en fer (D50.9)")));
		
		VisitCohortDefinition _prenatal = new VisitCohortDefinition();
		_prenatal.setVisitTypeList(
		    Arrays.asList(Context.getVisitService().getVisitTypeByUuid("35ba9aff-901c-49dc-8630-a59385480d18")));
		CompositionCohortDefinition ccd = new CompositionCohortDefinition();
		ccd.initializeFromElements(_prenatal, anemia, female);
		antenatal2.addRow(MessageUtil.translate("commonreports.report.antenatal2.ironDefANC"), ccd, parameterMappings);
		
		// Prenatal visit + Fer Folate Co prescribed 
		SqlCohortDefinition sqd = new SqlCohortDefinition(
		        "select patient_id from orders where concept_id=(select DISTINCT concept_id from concept_name where name = 'Ferrous folate' and locale='en' and concept_name_type='FULLY_SPECIFIED' and voided = 0) and order_type_id =(select order_type_id from order_type where name = 'Drug Order')");
		sqd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		CompositionCohortDefinition ccd1 = new CompositionCohortDefinition();
		ccd1.initializeFromElements(_prenatal, sqd, female);
		antenatal2.addRow(MessageUtil.translate("commonreports.report.antenatal2.prenatalIron"), ccd1, parameterMappings);
		
		//Prenatal visit treated for Fe def (Same as above--> Prenatal visit + Fer Folate Co prescribed)
		antenatal2.addRow(MessageUtil.translate("commonreports.report.antenatal2.prenatalIronDef"), ccd1, parameterMappings);
		
		//Mothers with a birth plan
		SqlCohortDefinition scd = new SqlCohortDefinition();
		String st = "select person_id from obs where concept_id = (select DISTINCT concept_id from concept_name where name = 'Number of Weeks' and locale='en' and concept_name_type='FULLY_SPECIFIED' and voided = 0)  AND obs_group_id IN (select obs_id from obs where concept_id = (select DISTINCT concept_id from concept_name where name = 'Estimated Gestational Age' and locale='en' and concept_name_type='FULLY_SPECIFIED' and voided = 0))";
		scd.setQuery(st);
		scd.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		scd.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		antenatal2.addRow(MessageUtil.translate("commonreports.report.antenatal2.motherBirthPlan"), scd, parameterMappings);
		
		//Prenatal visit + malaria test positive + chloroqine co prescribed 
		CodedObsCohortDefinition malaria = new CodedObsCohortDefinition();
		malaria.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		malaria.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		malaria.setOperator(SetComparator.IN);
		malaria.setQuestion(cs.getConcept("Malaria"));
		
		List<Concept> malariaPositiveConceptSet = new ArrayList<Concept>();
		malariaPositiveConceptSet.add(cs.getConcept("Positif"));
		malariaPositiveConceptSet.add(cs.getConcept("Un plus"));
		malariaPositiveConceptSet.add(cs.getConcept("Deux plus"));
		malariaPositiveConceptSet.add(cs.getConcept("Trois plus"));
		malariaPositiveConceptSet.add(cs.getConcept("Quatre plus"));
		
		malaria.setValueList(malariaPositiveConceptSet);
		
		SqlCohortDefinition sql = new SqlCohortDefinition(
		        "select patient_id from orders where concept_id=(select DISTINCT concept_id from concept_name where name = 'Chloroquine' and locale='en' and concept_name_type='FULLY_SPECIFIED' and voided = 0) and order_type_id =(select order_type_id from order_type where name = 'Drug Order')");
		sql.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sql.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		CompositionCohortDefinition ccd2 = new CompositionCohortDefinition();
		ccd2.initializeFromElements(_prenatal, sql, malaria, female);
		antenatal2.addRow(MessageUtil.translate("commonreports.report.antenatal2.prenatalMalariaPositiveChloroquine"), ccd2,
		    parameterMappings);
		
		//Prental + MUAC =<21cm
		NumericObsCohortDefinition muac = new NumericObsCohortDefinition();
		muac.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		muac.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		muac.setQuestion(cs.getConcept("Périmètre bracchial"));
		muac.setOperator1(RangeComparator.GREATER_EQUAL);
		muac.setValue1(0.0);
		muac.setOperator2(RangeComparator.LESS_EQUAL);
		muac.setValue2(210.0);
		CompositionCohortDefinition ccd3 = new CompositionCohortDefinition();
		ccd3.initializeFromElements(_prenatal, muac, female);
		antenatal2.addRow(MessageUtil.translate("commonreports.report.antenatal2.prenatalMUAC=<21cm"), ccd3,
		    parameterMappings);
		
		//Women + fer folate co prescribed 
		VisitService vs = Context.getVisitService();
		
		VisitType fP = vs.getVisitTypeByUuid("c4643116-8a61-499f-b62b-ff9375db0b7d");
		VisitType vac = vs.getVisitTypeByUuid("91c6bdce-3b92-42a2-9fa8-6ef6b169c4b2");
		VisitType maln = vs.getVisitTypeByUuid("a631dbbc-daa4-48aa-8fe4-3b5784360ab1");
		VisitType gen = vs.getVisitTypeByUuid("b9ba7b27-e64b-44d1-b4c5-2ced77f477d0");
		VisitType tb = vs.getVisitTypeByUuid("ad29e858-68a1-44eb-ae62-94157a8a52b0");
		VisitType labv = vs.getVisitTypeByUuid("929fb1a1-d801-4b56-9d15-503685a6c49d");
		List<VisitType> lVT = new ArrayList<VisitType>();
		lVT.add(fP);
		lVT.add(vac);
		lVT.add(maln);
		lVT.add(gen);
		lVT.add(tb);
		lVT.add(labv);
		
		VisitCohortDefinition _other = new VisitCohortDefinition();
		_other.setVisitTypeList(lVT);
		
		SqlCohortDefinition sqdc = new SqlCohortDefinition(
		        "select patient_id from orders where concept_id=(select DISTINCT concept_id from concept_name where name = 'Ferrous folate' and locale='en' and concept_name_type='FULLY_SPECIFIED' and voided = 0) and order_type_id =(select order_type_id from order_type where name = 'Drug Order')");
		sqdc.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
		sqdc.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
		CompositionCohortDefinition ccd5 = new CompositionCohortDefinition();
		ccd5.initializeFromElements(_other, sqdc, female);
		antenatal2.addRow(MessageUtil.translate("commonreports.report.antenatal2.womenIron"), ccd5, parameterMappings);
		
		setColumnNames();
		
		// All column
		GenderCohortDefinition allGender = new GenderCohortDefinition();
		allGender.setMaleIncluded(true);
		allGender.setFemaleIncluded(true);
		antenatal2.addColumn(col1, createCohortComposition(allGender), null);
		
		return rd;
	}
	
	private void setColumnNames() {
		
		col1 = MessageUtil.translate("commonreports.report.antenatal2.all.label");
		
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays
		        .asList(ReportManagerUtil.createCsvReportDesign("c3db03a7-8d69-49b5-94e5-15f9a59f027d", reportDefinition));
	}
}
