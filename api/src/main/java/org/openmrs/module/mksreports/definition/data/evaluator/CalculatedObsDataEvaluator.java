package org.openmrs.module.mksreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Obs;
import org.openmrs.annotation.Handler;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.module.mksreports.definition.data.CalculatedObsDataDefinition;
import org.openmrs.module.mksreports.definition.data.CalculatedObsDataDefinition.Operator;
import org.openmrs.module.reporting.data.person.service.PersonDataService;
import org.openmrs.module.reporting.data.visit.EvaluatedVisitData;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.data.visit.evaluator.VisitDataEvaluator;
import org.openmrs.module.reporting.data.visit.service.VisitDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Evaluates a {@link CalculatedObsDataDefiniton} to produce VisitData that contains the result of
 * the arithmetic operation
 */
@Handler(supports = CalculatedObsDataDefinition.class)
public class CalculatedObsDataEvaluator implements VisitDataEvaluator {
	
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
		
		CalculatedObsDataDefinition calculatedObsDD = (CalculatedObsDataDefinition) definition;
		Operator operator = calculatedObsDD.getOperator();
		
		EvaluatedVisitData evaluatedVisitData = new EvaluatedVisitData(calculatedObsDD, context);
		
		EvaluatedVisitData obsData1 = visitDataService.evaluate(calculatedObsDD.getObsDefinition1(), context);
		EvaluatedVisitData obsData2 = visitDataService.evaluate(calculatedObsDD.getObsDefinition2(), context);
		
		if (obsData1.getData() == null || obsData2.getData() == null) {
			log.warn("The concept observation on which to apply the arithmetic operation " + operator.toString()
			        + " is null: [Concept #" + obsData1 == null ? calculatedObsDD.getObsDefinition1().getParameterizable()
			        .getQuestion().getConceptId() : calculatedObsDD.getObsDefinition2().getParameterizable().getQuestion()
			        .getConceptId()
			        + "]. Skipping it.");
		}
		for (Integer vid : obsData1.getData().keySet()) {
			
			Obs obs1 = (Obs) obsData1.getData().get(vid);
			Obs obs2 = (Obs) obsData2.getData().get(vid);
			
			if (obs1 != null && obs2 != null) {
				
				boolean isNA = false;
				Double result = new Double(0);
				
				Double o1 = obs1.getValueNumeric();
				Double o2 = obs2.getValueNumeric();
				
				if (o1 != null && o2 != null) {
					
					if (operator.equals(Operator.ADDITION)) {
						result = o1 + o2;
					}
					if (operator.equals(Operator.SUBSTRACTION)) {
						result = o1 - o2;
					}
					if (operator.equals(Operator.MULTIPLICATION)) {
						result = o1 * o2;
					}
					if (operator.equals(Operator.DIVISION)) {
						if (o2 == 0) {
							isNA = true;
						} else {
							result = o1 / o2;
						}
					}
				} else {
					log.warn("The observations on which to apply the arithmetic operation " + operator.toString()
					        + " is not a numeric observation: [Obs #" + o1 == null ? obs1.getObsId() : obs2.getObsId()
					        + "]. Skipping it.");
				}
				evaluatedVisitData.addData(vid, isNA ? null : result);
			}
		}
		return evaluatedVisitData;
	}
}
