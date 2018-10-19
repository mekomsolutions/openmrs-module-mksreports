package org.openmrs.module.mksreports;

import org.openmrs.Cohort;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.evaluation.EvaluationContext;

public class ObsSummaryEvaluatedCohort extends EvaluatedCohort {
	
	private long obsCount;
	
	public ObsSummaryEvaluatedCohort() {
	}
	
	public ObsSummaryEvaluatedCohort(Cohort c, CohortDefinition definition, EvaluationContext context, long obsCount) {
		super(c, definition, context);
		setObsCount(obsCount);
	}
	
	public long getObsCount() {
		return obsCount;
	}
	
	public void setObsCount(long obsCount) {
		this.obsCount = obsCount;
	}
}
