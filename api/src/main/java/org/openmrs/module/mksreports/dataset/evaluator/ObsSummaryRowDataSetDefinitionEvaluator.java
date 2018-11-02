package org.openmrs.module.mksreports.dataset.evaluator;

import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.module.mksreports.dataset.definition.ObsSummaryRowDataSetDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetColumn;
import org.openmrs.module.reporting.dataset.DataSetRow;
import org.openmrs.module.reporting.dataset.SimpleDataSet;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.evaluator.DataSetEvaluator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Handler(supports = ObsSummaryRowDataSetDefinition.class)
public class ObsSummaryRowDataSetDefinitionEvaluator implements DataSetEvaluator {
	
	@Autowired
	private CohortDefinitionService cohortDefinitionService;
	
	@Autowired
	private EvaluationService evaluationService;
	
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		ObsSummaryRowDataSetDefinition definition = (ObsSummaryRowDataSetDefinition) dataSetDefinition;
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(" select o.person_id from obs o  inner join patient p on o.person_id = p.patient_id  "
		        + "where o.voided = false and p.voided = false and concept_id = :questionConceptId "
		        + "and o.obs_datetime >= :onOrAfter  and o.obs_datetime <= :onOrBefore "
		        + "and o.value_coded IN (:diagnosisList)");
		queryBuilder.setParameters(context.getParameterValues());
		
		List<Object[]> personIds = evaluationService.evaluateToList(queryBuilder, context);
		DataSetRow row = new DataSetRow();
		Map<String, CompositionCohortDefinition> columnDefinitions = definition.getColumns();
		for (Map.Entry<String, CompositionCohortDefinition> entry : columnDefinitions.entrySet()) {
			DataSetColumn col = new DataSetColumn();
			col.setName(entry.getKey());
			col.setDataType(List.class);
			row.addColumnValue(col, getPersonsWithDiagnosisInCohort(evaluateCohort(entry.getValue(), context), personIds));
		}
		SimpleDataSet dataSet = new SimpleDataSet(definition, context);
		dataSet.addRow(row);
		return dataSet;
	}
	
	private Cohort evaluateCohort(CompositionCohortDefinition cohortComposition, EvaluationContext context)
	        throws EvaluationException {
		return cohortDefinitionService.evaluate(cohortComposition, context);
	}
	
	private Object getPersonsWithDiagnosisInCohort(Cohort cohort, List<Object[]> patientsWithDiagnosis) {
		List<Integer> personIds = new ArrayList<Integer>();
		for (Object[] patient : patientsWithDiagnosis) {
			Integer personId = (Integer) patient[0];
			if (cohort.contains(personId))
				personIds.add(personId);
		}
		return personIds;
	}
}
