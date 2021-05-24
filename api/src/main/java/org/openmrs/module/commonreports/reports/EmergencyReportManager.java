package org.openmrs.module.commonreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.openmrs.Concept;
import org.openmrs.module.commonreports.ActivatedReportManager;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.AllPatientsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.BirthAndDeathCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.PresenceOrAbsenceCohortDefinition;
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
public class EmergencyReportManager extends ActivatedReportManager {
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public boolean isActivated() {
		return inizService.getBooleanFromKey("report.emergency.active", false);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
	@Override
	public String getUuid() {
		return "df9c8bf3-1fb2-47c7-a439-eb73f2e5d293";
	}
	
	@Override
	public String getName() {
		return "Emergencies";
	}
	
	@Override
	public String getDescription() {
		return "MSPP Report on Emergencies";
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
		
		rd.setName(getName());
		rd.setDescription(getDescription());
		rd.setParameters(getParameters());
		rd.setUuid(getUuid());
		
		CohortCrossTabDataSetDefinition emergencies = new CohortCrossTabDataSetDefinition();
		emergencies.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(emergencies));
		
		Map<String, Object> parameterMappings = new HashMap<String, Object>();
		parameterMappings.put("onOrAfter", "${startDate}");
		parameterMappings.put("onOrBefore", "${endDate}");
		
		Concept questionConcept = inizService.getConceptFromKey("report.emergency.question.concept");
		
		// adding rows
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		females.setFemaleIncluded(true);
		
		GenderCohortDefinition allGender = new GenderCohortDefinition();
		allGender.setFemaleIncluded(true);
		allGender.setMaleIncluded(true);
		allGender.setUnknownGenderIncluded(true);
		
		AgeCohortDefinition _0To14y = new AgeCohortDefinition();
		_0To14y.setMinAge(0);
		_0To14y.setMinAgeUnit(DurationUnit.YEARS);
		_0To14y.setMaxAge(14);
		_0To14y.setMaxAgeUnit(DurationUnit.YEARS);
		
		AgeCohortDefinition above14y = new AgeCohortDefinition();
		above14y.setMinAge(15);
		above14y.setMinAgeUnit(DurationUnit.YEARS);
		above14y.setMaxAge(200);
		above14y.setMaxAgeUnit(DurationUnit.YEARS);
		
		{
			// Public road accident
			Concept roadAccidents = inizService.getConceptFromKey("report.emergency.roadAccidents.concept");
			
			for (Concept accident : roadAccidents.getSetMembers()) {
				
				CodedObsCohortDefinition roadAccident = new CodedObsCohortDefinition();
				roadAccident.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
				roadAccident.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
				roadAccident.setOperator(SetComparator.IN);
				roadAccident.setQuestion(questionConcept);
				roadAccident.setValueList(Arrays.asList(accident));
				
				emergencies.addRow(accident.getDisplayString(), roadAccident, parameterMappings);
			}
		}
		
		{
			// Work accident
			CodedObsCohortDefinition workAccident = new CodedObsCohortDefinition();
			Concept wac = inizService.getConceptFromKey("report.emergency.workAccident.concept");
			
			workAccident.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
			workAccident.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
			workAccident.setOperator(SetComparator.IN);
			workAccident.setQuestion(questionConcept);
			workAccident.setValueList(Arrays.asList(wac));
			emergencies.addRow(wac.getDisplayString(), workAccident, parameterMappings);
		}
		
		{
			// Sexual violence
			CodedObsCohortDefinition sexualViolence = new CodedObsCohortDefinition();
			Concept svc = inizService.getConceptFromKey("report.emergency.sexualViolence.concept");
			
			sexualViolence.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
			sexualViolence.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
			sexualViolence.setOperator(SetComparator.IN);
			sexualViolence.setQuestion(questionConcept);
			sexualViolence.setValueList(Arrays.asList(svc));
			
			emergencies.addRow(
			    svc.getDisplayString() + " - " + MessageUtil.translate("commonreports.report.emergency.boys") + " "
			            + MessageUtil.translate("commonreports.report.emergency.0_14years"),
			    createCohortComposition(sexualViolence, males, _0To14y), parameterMappings);
			emergencies.addRow(
			    svc.getDisplayString() + " - " + MessageUtil.translate("commonreports.report.emergency.girls") + " "
			            + MessageUtil.translate("commonreports.report.emergency.0_14years"),
			    createCohortComposition(sexualViolence, females, _0To14y), parameterMappings);
			emergencies.addRow(
			    svc.getDisplayString() + " - " + MessageUtil.translate("commonreports.report.emergency.women") + " "
			            + MessageUtil.translate("commonreports.report.emergency.above14years"),
			    createCohortComposition(sexualViolence, males, above14y), parameterMappings);
			emergencies.addRow(
			    svc.getDisplayString() + " - " + MessageUtil.translate("commonreports.report.emergency.women") + " "
			            + MessageUtil.translate("commonreports.report.emergency.above14years"),
			    createCohortComposition(sexualViolence, females, above14y), parameterMappings);
		}
		
		{
			// Physical violence
			CodedObsCohortDefinition physicalViolence = new CodedObsCohortDefinition();
			Concept pvc = inizService.getConceptFromKey("report.emergency.physicalViolence.concept");
			physicalViolence.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
			physicalViolence.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
			physicalViolence.setOperator(SetComparator.IN);
			physicalViolence.setQuestion(questionConcept);
			physicalViolence.setValueList(Arrays.asList(pvc));
			
			emergencies.addRow(
			    pvc.getDisplayString() + " - " + MessageUtil.translate("commonreports.report.emergency.children") + " "
			            + MessageUtil.translate("commonreports.report.emergency.0_14years"),
			    createCohortComposition(physicalViolence, allGender, _0To14y), parameterMappings);
			emergencies.addRow(
			    pvc.getDisplayString() + " - " + MessageUtil.translate("commonreports.report.emergency.men") + " "
			            + MessageUtil.translate("commonreports.report.emergency.above14years"),
			    createCohortComposition(physicalViolence, females), parameterMappings);
			emergencies.addRow(
			    pvc.getDisplayString() + " - " + MessageUtil.translate("commonreports.report.emergency.women") + " "
			            + MessageUtil.translate("commonreports.report.emergency.above14years"),
			    createCohortComposition(physicalViolence, males), parameterMappings);
			
		}
		
		{
			// Other types of violence
			CodedObsCohortDefinition otherViolenceType = new CodedObsCohortDefinition();
			Concept ovtc = inizService.getConceptFromKey("report.emergency.otherViolenceType.concept");
			
			otherViolenceType.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
			otherViolenceType.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
			otherViolenceType.setOperator(SetComparator.IN);
			otherViolenceType.setQuestion(questionConcept);
			otherViolenceType.setValueList(Arrays.asList(ovtc));
			
			emergencies.addRow(
			    ovtc.getDisplayString() + " - " + MessageUtil.translate("commonreports.report.emergency.children") + " "
			            + MessageUtil.translate("commonreports.report.emergency.0_14years"),
			    createCohortComposition(otherViolenceType, allGender, _0To14y), parameterMappings);
			emergencies.addRow(
			    ovtc.getDisplayString() + " - " + MessageUtil.translate("commonreports.report.emergency.men") + " "
			            + MessageUtil.translate("commonreports.report.emergency.above14years"),
			    createCohortComposition(otherViolenceType, females), parameterMappings);
			emergencies.addRow(
			    ovtc.getDisplayString() + " - " + MessageUtil.translate("commonreports.report.emergency.women") + " "
			            + MessageUtil.translate("commonreports.report.emergency.above14years"),
			    createCohortComposition(otherViolenceType, males), parameterMappings);
			
		}
		
		{
			// Medical and surgical emergencies
			Concept mseq = inizService.getConceptFromKey("report.emergency.medicalAndSurgicalEmergenciesQuesion.concept");
			Concept msecs = inizService.getConceptFromKey("report.emergency.medicalAndSurgicalEmergenciesSetOfSets.concept");
			
			for (Concept emergency : msecs.getSetMembers()) {
				
				CodedObsCohortDefinition medicalAndSurgicalEmergencies = new CodedObsCohortDefinition();
				medicalAndSurgicalEmergencies.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
				medicalAndSurgicalEmergencies.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
				medicalAndSurgicalEmergencies.setOperator(SetComparator.IN);
				medicalAndSurgicalEmergencies.setQuestion(mseq);
				medicalAndSurgicalEmergencies.setValueList(new ArrayList<Concept>(emergency.getSetMembers()));
				
				emergencies.addRow(MessageUtil.translate("commonreports.report.emergency.medicalAndSurgicalEmergency")
				        + " - " + emergency.getDisplayString(),
				    medicalAndSurgicalEmergencies, parameterMappings);
			}
		}
		
		{
			//Other causes of emergencies
			CodedObsCohortDefinition otherEmergencies = new CodedObsCohortDefinition();
			Concept oec = inizService.getConceptFromKey("report.emergency.otherEmergencies.concept");
			Concept oeq = inizService.getConceptFromKey("report.emergency.otherEmergenciesQuestion.concept");
			
			otherEmergencies.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
			otherEmergencies.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
			otherEmergencies.setOperator(SetComparator.IN);
			otherEmergencies.setQuestion(oeq);
			otherEmergencies.setValueList(new ArrayList<Concept>(oec.getSetMembers()));
			
			emergencies.addRow(oec.getDisplayString(), otherEmergencies, parameterMappings);
			
		}
		
		// adding columns
		{
			// Total
			AllPatientsCohortDefinition allPatients = new AllPatientsCohortDefinition();
			emergencies.addColumn(MessageUtil.translate("commonreports.report.emergency.totalNumber.label"), allPatients,
			    null);
		}
		
		{	
			// Deceased
			BirthAndDeathCohortDefinition deceased = new BirthAndDeathCohortDefinition();
			deceased.setDied(true);
			
			// Left without permission
			CodedObsCohortDefinition leftWithoutPermissionCategory = new CodedObsCohortDefinition();
			Concept lwpc = inizService.getConceptFromKey("report.emergency.leftWithoutPermission.concept");
			Concept yesAns = inizService.getConceptFromKey("report.emergency.yes.concept");
			
			leftWithoutPermissionCategory.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
			leftWithoutPermissionCategory.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
			leftWithoutPermissionCategory.setOperator(SetComparator.IN);
			leftWithoutPermissionCategory.setQuestion(lwpc);
			leftWithoutPermissionCategory.setValueList(Arrays.asList(yesAns));
			
			PresenceOrAbsenceCohortDefinition absentInDceasedCategory = new PresenceOrAbsenceCohortDefinition();
			absentInDceasedCategory.addCohortToCheck(Mapped.mapStraightThrough(deceased));
			absentInDceasedCategory.setPresentInAtMost(0);
			CompositionCohortDefinition leftWithoutPermission = createCohortComposition(leftWithoutPermissionCategory, absentInDceasedCategory);
			
			// Referrals
			CodedObsCohortDefinition referredToCategory = new CodedObsCohortDefinition();
			Concept referredToConcept = inizService.getConceptFromKey("report.emergency.referredTo.concept");
			
			referredToCategory.addParameter(new Parameter("onOrAfter", "On Or After", Date.class));
			referredToCategory.addParameter(new Parameter("onOrBefore", "On Or Before", Date.class));
			referredToCategory.setOperator(SetComparator.IN);
			referredToCategory.setQuestion(referredToConcept);
			
			PresenceOrAbsenceCohortDefinition absentInDceasedLeftAndWithoutPermissionCategories = new PresenceOrAbsenceCohortDefinition();
			absentInDceasedLeftAndWithoutPermissionCategories.addCohortToCheck(Mapped.mapStraightThrough(deceased));
			absentInDceasedLeftAndWithoutPermissionCategories.addCohortToCheck(Mapped.mapStraightThrough(leftWithoutPermission));
			absentInDceasedLeftAndWithoutPermissionCategories.setPresentInAtMost(0);
			CompositionCohortDefinition referrals = createCohortComposition(referredToCategory, absentInDceasedLeftAndWithoutPermissionCategories);
			
			// Cared for (not in above categories)
			PresenceOrAbsenceCohortDefinition notInCategory = new PresenceOrAbsenceCohortDefinition();
			
			notInCategory.addCohortToCheck(Mapped.mapStraightThrough(referrals));
			notInCategory.addCohortToCheck(Mapped.mapStraightThrough(deceased));
			notInCategory.addCohortToCheck(Mapped.mapStraightThrough(leftWithoutPermission));
			notInCategory.setPresentInAtMost(0);
			CompositionCohortDefinition caredFor = createCohortComposition(notInCategory);
			
			emergencies.addColumn(
				    MessageUtil.translate("commonreports.report.emergency.leftWithoutPermissionCategory.label"),
				    leftWithoutPermission, null);
			emergencies.addColumn(MessageUtil.translate("commonreports.report.emergency.deceasedCategory.label"),
				    deceased, null);
			emergencies.addColumn(MessageUtil.translate("commonreports.report.emergency.referredCategory.label"),
					referrals, null);
			emergencies.addColumn(MessageUtil.translate("commonreports.report.emergency.notInCategory.label"), caredFor,
			    null);
			
		}
		
		return rd;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		ReportDesign reportDesign = ReportManagerUtil.createCsvReportDesign("0c67c7f3-b4b3-4920-82b6-830d0a8b83a1",
		    reportDefinition);
		return Arrays.asList(reportDesign);
	}
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
}
