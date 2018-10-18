package org.openmrs.module.mksreports;

import org.openmrs.Cohort;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;

public class ObsSummaryEvaluatedCohort extends EvaluatedCohort {
	
	public ObsSummaryEvaluatedCohort() {
	}
	
	public ObsSummaryEvaluatedCohort(Cohort c, CohortDefinition definition, EvaluationContext context) {
		super(c, definition, context);
	}
}
