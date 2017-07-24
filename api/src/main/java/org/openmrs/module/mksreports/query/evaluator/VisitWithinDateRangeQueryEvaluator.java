package org.openmrs.module.mksreports.query.evaluator;

import java.util.Date;
import java.util.List;

import org.openmrs.Visit;
import org.openmrs.annotation.Handler;
import org.openmrs.module.mksreports.query.VisitWithinDateRangeQuery;
import org.openmrs.module.reporting.evaluation.Evaluated;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.HqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.reporting.query.visit.VisitQueryResult;
import org.openmrs.module.reporting.query.visit.definition.VisitQuery;
import org.openmrs.module.reporting.query.visit.evaluator.VisitQueryEvaluator;
import org.springframework.beans.factory.annotation.Autowired;

@Handler(supports = VisitWithinDateRangeQuery.class)
public class VisitWithinDateRangeQueryEvaluator implements VisitQueryEvaluator {
	
	@Autowired
	EvaluationService evaluationService;
	
	@Override
	public Evaluated<VisitQuery> evaluate(VisitQuery visitQuery, EvaluationContext evaluationContext)
	        throws EvaluationException {
		
		VisitWithinDateRangeQuery vq = (VisitWithinDateRangeQuery) visitQuery;
		
		List<Integer> visitIds = getAllVisitsWithinDateRange(vq, evaluationContext, evaluationService);
		
		VisitQueryResult result = new VisitQueryResult(visitQuery, evaluationContext);
		result.add(visitIds.toArray(new Integer[visitIds.size()]));
		
		return result;
		
	}
	
	protected List<Integer> getAllVisitsWithinDateRange(VisitWithinDateRangeQuery query,
	        EvaluationContext evaluationContext, EvaluationService evaluationService) {
		
		HqlQueryBuilder qb = new HqlQueryBuilder();
		qb.select("v.visitId");
		qb.from(Visit.class, "v");
		qb.whereGreaterOrEqualTo("v.stopDatetime", query.getStartDate());
		qb.whereLess("v.stopDatetime", query.getEndDate());
		qb.orderAsc("v.startDatetime");
		
		List<Integer> visitIds = evaluationService.evaluateToList(qb, Integer.class, evaluationContext);
		
		return visitIds;
		
	}
}
