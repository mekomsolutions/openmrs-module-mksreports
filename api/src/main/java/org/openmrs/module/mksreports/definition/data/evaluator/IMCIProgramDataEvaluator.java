package org.openmrs.module.mksreports.definition.data.evaluator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.annotation.Handler;
import org.openmrs.module.mksreports.definition.data.IMCIProgramDataDefinition;
import org.openmrs.module.reporting.data.visit.EvaluatedVisitData;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.data.visit.evaluator.VisitDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;

/**
 * TODO: Not implemented. Should evaluate a {@link IMCIProgramDataDefiniton} to produce VisitData
 * that contains the status of the IMCI program
 */
@Handler(supports = IMCIProgramDataDefinition.class)
public class IMCIProgramDataEvaluator implements VisitDataEvaluator {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public EvaluatedVisitData evaluate(VisitDataDefinition definition, EvaluationContext context) throws EvaluationException {
		
		IMCIProgramDataDefinition imciDD = (IMCIProgramDataDefinition) definition;
		
		EvaluatedVisitData evaluatedIMCIData = new EvaluatedVisitData(imciDD, context);
		
		return evaluatedIMCIData;
	}
}
