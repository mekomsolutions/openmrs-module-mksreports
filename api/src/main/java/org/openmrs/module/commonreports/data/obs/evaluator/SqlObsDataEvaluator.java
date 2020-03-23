package org.openmrs.module.commonreports.data.obs.evaluator;

import java.util.Map;

import org.openmrs.annotation.Handler;
import org.openmrs.module.commonreports.data.obs.definition.SqlObsDataDefinition;
import org.openmrs.module.reporting.data.obs.EvaluatedObsData;
import org.openmrs.module.reporting.data.obs.ObsDataUtil;
import org.openmrs.module.reporting.data.obs.definition.ObsDataDefinition;
import org.openmrs.module.reporting.data.obs.evaluator.ObsDataEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.openmrs.module.reporting.query.obs.ObsIdSet;
import org.springframework.beans.factory.annotation.Autowired;

@Handler(supports = SqlObsDataDefinition.class)
public class SqlObsDataEvaluator implements ObsDataEvaluator {
	
	@Autowired
	private EvaluationService evaluationService;
	
	@Override
	public EvaluatedObsData evaluate(ObsDataDefinition definition, EvaluationContext context) throws EvaluationException {
		SqlObsDataDefinition sqlDefinition = (SqlObsDataDefinition) definition;
		EvaluatedObsData data = new EvaluatedObsData();
		
		ObsIdSet obsIds = new ObsIdSet(ObsDataUtil.getObsIdsForContext(context, false));
		if (obsIds.getSize() == 0) {
			return data;
		}
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(sqlDefinition.getSql());
		for (Parameter parameter : sqlDefinition.getParameters()) {
			queryBuilder.addParameter(parameter.getName(), context.getParameterValue(parameter.getName()));
		}
		queryBuilder.addParameter("obsIds", obsIds);
		
		Map<Integer, Object> results = this.evaluationService.evaluateToMap(queryBuilder, Integer.class, Object.class,
		    context);
		data.setData(results);
		return data;
	}
	
}
