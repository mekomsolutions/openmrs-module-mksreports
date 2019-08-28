package org.openmrs.module.mksreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.annotation.Handler;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.mksreports.definition.data.ObsOnAgeDataDefinition;
import org.openmrs.module.reporting.common.Age;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.service.PersonDataService;
import org.openmrs.module.reporting.data.visit.EvaluatedVisitData;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.data.visit.evaluator.VisitDataEvaluator;
import org.openmrs.module.reporting.data.visit.service.VisitDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.springframework.beans.factory.annotation.Autowired;

@Handler(supports = ObsOnAgeDataDefinition.class)
public class ObsOnAgeDataEvaluator implements VisitDataEvaluator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Autowired
	VisitDataService visitDataService;
	
	@Autowired
	PersonDataService personDataService;
	
	@Autowired
	VisitService visitService;
	
	@Autowired
	PatientService patientService;
	
	@Override
	public EvaluatedVisitData evaluate(VisitDataDefinition definition, EvaluationContext context) throws EvaluationException {
		
		ObsOnAgeDataDefinition obsOnAgeDD = (ObsOnAgeDataDefinition) definition;
		
		EvaluatedVisitData evaluatedVisitData = new EvaluatedVisitData(obsOnAgeDD, context);
		
		EvaluatedVisitData obsData = visitDataService.evaluate(obsOnAgeDD.getObsDefinition(), context);
		EvaluatedPersonData ageData = personDataService.evaluate(obsOnAgeDD.getAgeDefinition(), context);
		
		for (Integer vid : obsData.getData().keySet()) {
			
			Obs obs = (Obs) obsData.getData().get(vid);
			
			Double result = (Double) null;
			
			if (obs != null) {
				Patient patient = visitService.getVisit(vid).getPatient();
				Age age = (Age) ageData.getData().get(patient.getId());
				
				if (obs.getValueNumeric() == null) {
					log.warn("The observation on which to apply the division operation "
					        + "is a non-numeric observation: [Obs #" + obs.getObsId() + "]. Returning null.");
				} else if (age.getFullYears() == 0) {
					log.warn("The patient's age is 0 years [Patient #" + patient.getId() + "]. Returning null.");
				} else {
					result = obs.getValueNumeric() / age.getFullYears();
				}
			}
			evaluatedVisitData.getData().put(vid, result);
		}
		
		return evaluatedVisitData;
	}
	
}
