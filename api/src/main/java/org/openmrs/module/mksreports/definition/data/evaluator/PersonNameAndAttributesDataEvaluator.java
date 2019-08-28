package org.openmrs.module.mksreports.definition.data.evaluator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonName;
import org.openmrs.annotation.Handler;
import org.openmrs.module.mksreports.definition.data.PersonNameAndAttributesDataDefinition;
import org.openmrs.module.reporting.data.person.EvaluatedPersonData;
import org.openmrs.module.reporting.data.person.PersonData;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.evaluator.PersonDataEvaluator;
import org.openmrs.module.reporting.data.person.service.PersonDataService;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Evaluates a {@link PersonNameAndAtributesDataDefiniton} to produce {@link PersonData} that
 * contains the patient's name appended with provided {@link PersonAttribute} values
 */
@Handler(supports = PersonNameAndAttributesDataDefinition.class)
public class PersonNameAndAttributesDataEvaluator implements PersonDataEvaluator {
	
	@Autowired
	PersonDataService personDataService;
	
	@Override
	public EvaluatedPersonData evaluate(PersonDataDefinition definition, EvaluationContext context)
	        throws EvaluationException {
		
		PersonNameAndAttributesDataDefinition nameDD = (PersonNameAndAttributesDataDefinition) definition;
		
		EvaluatedPersonData evaluatedNameData = new EvaluatedPersonData(nameDD, context);
		
		// Evaluate the PreferredNameDD
		EvaluatedPersonData evaluatedPreferredNameData = personDataService.evaluate(nameDD.getPreferredNameDefinition(),
		    context);
		
		// Evaluate the list of PersonAttributeDDs and return the evaluated data as
		// another list
		List<EvaluatedPersonData> evaluedPAData = new ArrayList<EvaluatedPersonData>();
		for (Mapped<? extends PersonAttributeDataDefinition> mappedDef : nameDD.getPersonAttributeDefinitions()) {
			EvaluatedPersonData evaluatedDef = personDataService.evaluate(mappedDef, context);
			if (evaluatedDef.getData() != null) {
				evaluedPAData.add(personDataService.evaluate(mappedDef, context));
			}
		}
		
		for (Integer pid : evaluatedPreferredNameData.getData().keySet()) {
			PersonName name = (PersonName) evaluatedPreferredNameData.getData().get(pid);
			String nameStr = name.getFullName();
			
			String paStr = "";
			for (EvaluatedPersonData paData : evaluedPAData) {
				if (paData.getData().get(pid) != null) {
					paStr = paStr.equals("") ? paData.getData().get(pid).toString() : paStr + ", "
					        + paData.getData().get(pid).toString();
				}
			}
			if (paStr != "") {
				evaluatedNameData.addData(pid, nameStr + " (" + paStr + ")");
			} else {
				evaluatedNameData.addData(pid, nameStr);
			}
		}
		return evaluatedNameData;
	}
}
