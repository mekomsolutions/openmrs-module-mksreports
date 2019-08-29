package org.openmrs.module.mksreports.definition.data.evaluator;

import java.util.Arrays;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.PersonAddress;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.annotation.Handler;
import org.openmrs.api.PatientService;
import org.openmrs.api.PersonService;
import org.openmrs.module.initializer.api.InitializerService;
import org.openmrs.module.mksreports.common.ContactInfo;
import org.openmrs.module.mksreports.definition.data.ContactInfoDataDefinition;
import org.openmrs.module.reporting.data.patient.EvaluatedPatientData;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdDataDefinition;
import org.openmrs.module.reporting.data.patient.evaluator.PatientDataEvaluator;
import org.openmrs.module.reporting.data.patient.service.PatientDataService;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.definition.PreferredAddressDataDefinition;
import org.openmrs.module.reporting.data.person.service.PersonDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.springframework.beans.factory.annotation.Autowired;

@Handler(supports = ContactInfoDataDefinition.class)
public class ContactInfoDataEvaluator implements PatientDataEvaluator {
	
	@Autowired
	private PatientDataService patientDataService;
	
	@Autowired
	private PersonDataService personDataService;
	
	@Autowired
	private PatientService patientService;
	
	@Autowired
	private InitializerService inizService;
	
	@Override
	public EvaluatedPatientData evaluate(PatientDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		
		ContactInfoDataDefinition contactDD = (ContactInfoDataDefinition) definition;
		EvaluatedPatientData evaluatedPatientData = new EvaluatedPatientData(contactDD, context);
		
		EvaluatedPatientData patientData = patientDataService.evaluate(new PatientIdDataDefinition(), context);
		EvaluatedPersonData addressData = personDataService.evaluate(new PreferredAddressDataDefinition(), context);
		
		for (Integer pid : patientData.getData().keySet()) {
			List<PersonAttribute> phoneNumbers = getPhoneNumbers(patientService.getPatient(pid));
			PersonAddress address = (PersonAddress) addressData.getData().get(pid);
			ContactInfo object = new ContactInfo();
			object.setAddress(address);
			object.setPhoneNumbers(phoneNumbers);
			
			evaluatedPatientData.getData().put(pid, object);
		}
		
		return evaluatedPatientData;
	}
	
	private List<PersonAttribute> getPhoneNumbers(Patient patient) {
		// Get the phones numbers attributes and concatenate them in one single string
		PersonAttributeType primaryPhoneNumberType = inizService
		        .getPersonAttributeTypeFromKey("report.contactInfo.primaryPhone.pat");
		PersonAttribute primaryPhoneNumber = patient.getAttribute(primaryPhoneNumberType);
		PersonAttributeType secondaryPhoneNumberType = inizService
		        .getPersonAttributeTypeFromKey("report.contactInfo.secondaryPhone.pat");
		PersonAttribute secondaryPhoneNumber = patient.getAttribute(secondaryPhoneNumberType);
		
		return Arrays.asList(primaryPhoneNumber, secondaryPhoneNumber);
	}
	
}
