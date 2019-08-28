package org.openmrs.module.mksreports.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openmrs.Concept;
import org.openmrs.Location;
import org.openmrs.ConceptAnswer;
import org.openmrs.PersonAttributeType;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.mksreports.MKSReportManager;
import org.openmrs.module.mksreports.MKSReportsConstants;
import org.openmrs.module.mksreports.data.converter.AddressAndPhoneConverter;
import org.openmrs.module.mksreports.data.converter.CodedToShortNameConverter;
import org.openmrs.module.mksreports.data.converter.DistanceFromHealthCenterConverter;
import org.openmrs.module.mksreports.data.converter.GenderConverter;
import org.openmrs.module.mksreports.data.converter.NullToNAConverter;
import org.openmrs.module.mksreports.data.converter.ObsBooleanConverter;
import org.openmrs.module.mksreports.data.converter.OrderConverter;
import org.openmrs.module.mksreports.data.converter.RoundNumber;
import org.openmrs.module.mksreports.definition.data.CalculatedObsDataDefinition;
import org.openmrs.module.mksreports.definition.data.CalculatedObsDataDefinition.Operator;
import org.openmrs.module.mksreports.definition.data.ContactInfoDataDefinition;
import org.openmrs.module.mksreports.definition.data.IMCIProgramDataDefinition;
import org.openmrs.module.mksreports.definition.data.ObsOnAgeDataDefinition;
import org.openmrs.module.mksreports.definition.data.PersonNameAndAttributesDataDefinition;
import org.openmrs.module.reporting.common.Age;
import org.openmrs.module.reporting.common.AgeRange;
import org.openmrs.module.reporting.common.MessageUtil;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.converter.AgeRangeConverter;
import org.openmrs.module.reporting.data.converter.CollectionConverter;
import org.openmrs.module.reporting.data.converter.ObsValueConverter;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.patient.library.BuiltInPatientDataLibrary;
import org.openmrs.module.reporting.data.person.definition.AgeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.ObsForVisitDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.OrderForVisitDataDefinition;
import org.openmrs.module.reporting.dataset.definition.VisitDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.query.visit.definition.BasicVisitQuery;
import org.openmrs.module.reporting.report.ReportDesign;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.openmrs.module.reporting.report.manager.ReportManagerUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component(MKSReportsConstants.COMPONENT_REPORTMANAGER_OPDRECBOOK)
public class OutpatientRecordBookManager extends MKSReportManager {
	
	@Autowired
	private BuiltInPatientDataLibrary builtInPatientData;
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private ConceptService conceptService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public boolean isActive() {
		return inizService.getBooleanFromKey("report.opdrecbook.active", true);
	}
	
	@Override
	public String getVersion() {
		return "1.0.0-SNAPSHOT";
	}
	
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
	
	private Parameter getLocationParameter() {
		return new Parameter("locationList", "Visit Location", Location.class, List.class, null);
	}
	
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
		
		VisitDataSetDefinition vdsd = new VisitDataSetDefinition();
		vdsd.addParameters(getParameters());
		rd.addDataSetDefinition(getName(), Mapped.mapStraightThrough(vdsd));
		
		BasicVisitQuery query = new BasicVisitQuery();
		
		Parameter endedOnOrAfter = new Parameter("endedOnOrAfter", "Ended On Or After", Date.class);
		Parameter endedBefore = new Parameter("endedOnOrBefore", "Ended On Or Before", Date.class);
		Parameter locationList = new Parameter("locationList", "Visit Location", Location.class, List.class, null);
		query.setParameters(Arrays.asList(endedOnOrAfter, endedBefore, locationList));
		
		{
			Map<String, Object> parameterMappings = new HashMap<String, Object>();
			parameterMappings.put("endedOnOrAfter", "${startDate}");
			parameterMappings.put("endedOnOrBefore", "${endDate}");
			parameterMappings.put("locationList", "${locationList}");
			vdsd.addRowFilter(query, ObjectUtil.toString(parameterMappings, "=", ","));
		}
		
		// Patient Identifiers (all)
		PatientIdentifierDataDefinition piDD = new PatientIdentifierDataDefinition();
		piDD.setTypes(patientService.getAllPatientIdentifierTypes());
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.identifier.label"), piDD, null);
		
		// Patient Name
		PersonNameAndAttributesDataDefinition nameDD = new PersonNameAndAttributesDataDefinition();
		
		PreferredNameDataDefinition preferredNameDD = new PreferredNameDataDefinition();
		Mapped<PreferredNameDataDefinition> mappedPreferredNameDD = new Mapped<PreferredNameDataDefinition>();
		mappedPreferredNameDD.setParameterizable(preferredNameDD);
		
		// Create the list of mapped PersonAttributeDataDefinition to be fed to the
		// PersonNameAndAttributesDD
		// Given name local
		PersonAttributeDataDefinition givenNameLocalDD = new PersonAttributeDataDefinition();
		givenNameLocalDD.setPersonAttributeType(inizService
		        .getPersonAttributeTypeFromKey("report.opdrecbook.givenNameLocal.pat"));
		Mapped<PersonAttributeDataDefinition> mappedGivenNameLocalDD = new Mapped<PersonAttributeDataDefinition>();
		mappedGivenNameLocalDD.setParameterizable(givenNameLocalDD);
		
		// Middle name local
		PersonAttributeDataDefinition middleNameLocalDD = new PersonAttributeDataDefinition();
		middleNameLocalDD.setPersonAttributeType(inizService
		        .getPersonAttributeTypeFromKey("report.opdrecbook.middleNameLocal.pat"));
		Mapped<PersonAttributeDataDefinition> mappedMiddleNameLocalDD = new Mapped<PersonAttributeDataDefinition>();
		mappedMiddleNameLocalDD.setParameterizable(middleNameLocalDD);
		
		// Family name local
		PersonAttributeDataDefinition familyNameLocalDD = new PersonAttributeDataDefinition();
		familyNameLocalDD.setPersonAttributeType(inizService
		        .getPersonAttributeTypeFromKey("report.opdrecbook.familyNameLocal.pat"));
		Mapped<PersonAttributeDataDefinition> mappedFamilyNameLocalDD = new Mapped<PersonAttributeDataDefinition>();
		mappedFamilyNameLocalDD.setParameterizable(familyNameLocalDD);
		
		List<Mapped<? extends PersonAttributeDataDefinition>> attributes = new ArrayList<Mapped<? extends PersonAttributeDataDefinition>>();
		attributes.add(mappedGivenNameLocalDD);
		attributes.add(mappedMiddleNameLocalDD);
		attributes.add(mappedFamilyNameLocalDD);
		
		nameDD.setPreferredNameDefinition(mappedPreferredNameDD);
		nameDD.setPersonAttributeDefinitions(attributes);
		
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.patientName.label"), nameDD, null);
		
		// Guardian Name
		PersonAttributeDataDefinition paDD1 = new PersonAttributeDataDefinition();
		paDD1.setPersonAttributeType(inizService.getPersonAttributeTypeFromKey("report.opdrecbook.guardian.pat"));
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.guardianName.label"), paDD1, null);
		
		String isOfCategoryLabel = MessageUtil.translate("mksreports.report.outpatientRecordBook.isOfCategory.label");
		
		// Age Categories
		AgeDataDefinition ageDD = new AgeDataDefinition();
		
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
		
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory1.label"), ageDD,
		    (String) null, ageConverter1);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory2.label"), ageDD,
		    (String) null, ageConverter2);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory3.label"), ageDD,
		    (String) null, ageConverter3);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory4.label"), ageDD,
		    (String) null, ageConverter4);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory5.label"), ageDD,
		    (String) null, ageConverter5);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory6.label"), ageDD,
		    (String) null, ageConverter6);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory7.label"), ageDD,
		    (String) null, ageConverter7);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.ageCategory8.label"), ageDD,
		    (String) null, ageConverter8);
		
		// Gender categories
		GenderConverter maleConverter = new GenderConverter(Arrays.asList("M"), isOfCategoryLabel, null);
		GenderConverter femaleConverter = new GenderConverter(Arrays.asList("F"), isOfCategoryLabel, null);
		GenderConverter otherConverter = new GenderConverter(Arrays.asList("O"), isOfCategoryLabel, null);
		
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.genderCategoryMale.label"),
		    builtInPatientData.getGender(), (String) null, maleConverter);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.genderCategoryFemale.label"),
		    builtInPatientData.getGender(), (String) null, femaleConverter);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.genderCategoryOther.label"),
		    builtInPatientData.getGender(), (String) null, otherConverter);
		
		// Distance from Health Center zones
		PersonAttributeDataDefinition paDD2 = new PersonAttributeDataDefinition();
		PersonAttributeType distanceHC = inizService.getPersonAttributeTypeFromKey("report.opdrecbook.distanceHC.pat");
		paDD2.setPersonAttributeType(distanceHC);
		
		Concept distanceFromHCConcept = conceptService.getConcept(distanceHC.getForeignKey());
		// Dynamically create the columns based on the Distance From HC concept
		if (distanceFromHCConcept != null) {
			for (ConceptAnswer answer : distanceFromHCConcept.getAnswers()) {
				Concept zone = answer.getAnswerConcept();
				DistanceFromHealthCenterConverter zoneConverter = new DistanceFromHealthCenterConverter(Arrays.asList(zone),
				        isOfCategoryLabel, "");
				vdsd.addColumn(zone.getShortNameInLocale(Context.getLocale()).getName(), paDD2, null, zoneConverter);
			}
		}
		
		CollectionConverter obsListValueConverter = new CollectionConverter(new ObsValueConverter(), false, null);
		
		// Gestational Age
		ObsForVisitDataDefinition gestationObsDD = new ObsForVisitDataDefinition();
		gestationObsDD.setQuestion(inizService.getConceptFromKey("report.opdrecbook.gestation.concept"));
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.gestationalAge.label"), gestationObsDD,
		    null, obsListValueConverter);
		
		// Address and phone
		ContactInfoDataDefinition ciDD = new ContactInfoDataDefinition();
		AddressAndPhoneConverter addressAndPhoneConverter = new AddressAndPhoneConverter();
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.addressAndPhone.label"), ciDD, null,
		    addressAndPhoneConverter);
		
		// Referred From (Referred From observation)
		ObsForVisitDataDefinition referredFromDD = new ObsForVisitDataDefinition();
		referredFromDD.setQuestion(inizService.getConceptFromKey("report.opdrecbook.referredFrom.concept"));
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.referredFrom.label"), referredFromDD,
		    null, obsListValueConverter);
		
		// New Case/Old Case categories
		ObsBooleanConverter trueConverter = new ObsBooleanConverter("", isOfCategoryLabel);
		ObsBooleanConverter falseConverter = new ObsBooleanConverter(isOfCategoryLabel, "");
		
		ObsForVisitDataDefinition newCaseDD = new ObsForVisitDataDefinition();
		newCaseDD.setQuestion(inizService.getConceptFromKey("report.opdrecbook.newCase.concept"));
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.newCase.label"), newCaseDD, null,
		    falseConverter);
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.oldCase.label"), newCaseDD, null,
		    trueConverter);
		
		// Symptoms (Chief complaint observation)
		ObsForVisitDataDefinition symptomsDD = new ObsForVisitDataDefinition();
		symptomsDD.setQuestion(inizService.getConceptFromKey("report.opdrecbook.symptoms.concept"));
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.symptoms.label"), symptomsDD, null,
		    obsListValueConverter);
		
		// Diagnosis (Diagnosis observation)
		ObsForVisitDataDefinition diagnosisDD = new ObsForVisitDataDefinition();
		diagnosisDD.setQuestion(inizService.getConceptFromKey("report.opdrecbook.diagnosis.concept"));
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.diagnosis.label"), diagnosisDD, null,
		    obsListValueConverter);
		
		// Treatment (Orders)
		OrderForVisitDataDefinition orderDD = new OrderForVisitDataDefinition();
		orderDD.setTypes(Arrays.asList(orderService.getOrderTypeByUuid(inizService
		        .getValueFromKey("report.opdrecbook.drugOrder.orderType"))));
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.treatment.label"), orderDD,
		    (String) null, new CollectionConverter(new OrderConverter(), false, null));
		
		NullToNAConverter nullToNAConverter = new NullToNAConverter(
		        MessageUtil.translate("mksreports.report.outpatientRecordBook.na.label"));
		// IMCI program started
		// TODO: Currently the IMCI program is not implemented. Returning "N/A" in each
		// cell
		IMCIProgramDataDefinition imciDD = new IMCIProgramDataDefinition();
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.imciProgram.label"), imciDD,
		    (String) null, nullToNAConverter);
		
		// Nutritional Weight/Age
		{
			ObsForVisitDataDefinition obsVisitDD = new ObsForVisitDataDefinition();
			// Use the most recent Weight
			obsVisitDD.setWhich(TimeQualifier.LAST);
			obsVisitDD.setQuestion(inizService.getConceptFromKey("report.opdrecbook.weight.concept"));
			
			AgeDataDefinition agePersonDD = new AgeDataDefinition();
			agePersonDD.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
			{
				Map<String, Object> parameterMappings = new HashMap<String, Object>();
				parameterMappings.put("effectiveDate", "${endDate}");
				
				ObsOnAgeDataDefinition obsOnAge = new ObsOnAgeDataDefinition();
				obsOnAge.setObsDefinition(Mapped.mapStraightThrough(obsVisitDD));
				obsOnAge.setAgeDefinition(Mapped.mapStraightThrough(agePersonDD));
				obsOnAge.addParameter(new Parameter("effectiveDate", "Effective Date", Date.class));
				
				vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.weightOnAge.label"), obsOnAge,
				    ObjectUtil.toString(parameterMappings, "=", ","), new RoundNumber(2));
			}
		}
		
		// Nutritional Weight:Height
		{
			ObsForVisitDataDefinition weightDD = new ObsForVisitDataDefinition();
			// Use the most recent Weight
			weightDD.setWhich(TimeQualifier.LAST);
			weightDD.setQuestion(inizService.getConceptFromKey("report.opdrecbook.weight.concept"));
			
			Mapped<ObsForVisitDataDefinition> mappedWeightDD = new Mapped<ObsForVisitDataDefinition>();
			mappedWeightDD.setParameterizable(weightDD);
			
			ObsForVisitDataDefinition heightDD = new ObsForVisitDataDefinition();
			// Use the most recent Height
			heightDD.setWhich(TimeQualifier.LAST);
			heightDD.setQuestion(inizService.getConceptFromKey("report.opdrecbook.height.concept"));
			Mapped<ObsForVisitDataDefinition> mappedHeightDD = new Mapped<ObsForVisitDataDefinition>();
			mappedHeightDD.setParameterizable(weightDD);
			
			CalculatedObsDataDefinition calculatedDD = new CalculatedObsDataDefinition();
			calculatedDD.setOperator(Operator.DIVISION);
			calculatedDD.setObsDefinition1(mappedWeightDD);
			calculatedDD.setObsDefinition2(mappedHeightDD);
			
			vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.weightOnHeight.label"),
			    calculatedDD, null, new RoundNumber(2));
		}
		
		// Referred To
		ObsForVisitDataDefinition referredToDD = new ObsForVisitDataDefinition();
		referredToDD.setQuestion(inizService.getConceptFromKey("report.opdrecbook.referredTo.concept"));
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.referredTo.label"), referredToDD, null,
		    obsListValueConverter);
		
		// Payment Type
		ObsForVisitDataDefinition paymentTypeDD = new ObsForVisitDataDefinition();
		paymentTypeDD.setQuestion(inizService.getConceptFromKey("report.opdrecbook.paymentType.concept"));
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.paymentType.label"), paymentTypeDD,
		    null, new CodedToShortNameConverter());
		
		// Other notes (Past medical history observation)
		ObsForVisitDataDefinition otherNotesDD = new ObsForVisitDataDefinition();
		otherNotesDD.setQuestion(inizService.getConceptFromKey("report.opdrecbook.otherNotes.concept"));
		vdsd.addColumn(MessageUtil.translate("mksreports.report.outpatientRecordBook.otherNotes.label"), otherNotesDD, null,
		    obsListValueConverter);
		
		return rd;
	}
	
	@Override
	public List<ReportDesign> constructReportDesigns(ReportDefinition reportDefinition) {
		return Arrays.asList(ReportManagerUtil.createCsvReportDesign("9873e45d-f8a0-4682-be78-243b8c9b848c",
		    reportDefinition));
	}
}
