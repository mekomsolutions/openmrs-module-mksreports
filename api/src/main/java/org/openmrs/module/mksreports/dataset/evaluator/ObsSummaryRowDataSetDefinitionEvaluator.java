package org.openmrs.module.mksreports.dataset.evaluator;

import org.openmrs.annotation.Handler;
import org.openmrs.module.mksreports.dataset.definition.ObsSummaryRowDataSetDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
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
import java.util.Set;
import java.util.stream.Collectors;

@Handler(supports = ObsSummaryRowDataSetDefinition.class)
public class ObsSummaryRowDataSetDefinitionEvaluator implements DataSetEvaluator {
	
	@Autowired
	private CohortDefinitionService cohortDefinitionService;
	
	@Autowired
	private EvaluationService evaluationService;
	
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		ObsSummaryRowDataSetDefinition definition = (ObsSummaryRowDataSetDefinition) dataSetDefinition;
		
		List<Integer> patientsWithObs = getPatientsWithObs(context);
		
		DataSetRow row = new DataSetRow();
		Map<String, CohortDefinition> columnDefinitions = definition.getCohortColumnDefinitions();
		for (Map.Entry<String, CohortDefinition> entry : columnDefinitions.entrySet()) {
			Set<Integer> patientsInCohort = cohortDefinitionService.evaluate(entry.getValue(), context).getMemberIds();
			
			DataSetColumn col = new DataSetColumn();
			col.setName(entry.getKey());
			col.setDataType(List.class);
			
			row.addColumnValue(col,
			    patientsWithObs.stream().filter(patientsInCohort::contains).collect(Collectors.toList()));
		}
		SimpleDataSet dataSet = new SimpleDataSet(definition, context);
		dataSet.addRow(row);
		return dataSet;
	}
	
	/**
	 * Get patients with one or more diagnosis
	 */
	protected List<Integer> getPatientsWithObs(EvaluationContext context) {
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append("select o.person_id from obs o  inner join patient p on o.person_id = p.patient_id  "
		        + "where o.voided = false and p.voided = false and concept_id IN (:questionConceptIds) "
		        + "and o.obs_datetime >= :onOrAfter  and o.obs_datetime <= :onOrBefore "
		        + "and o.value_coded IN (:diagnosisList)");
		queryBuilder.setParameters(context.getParameterValues());
		List<Object[]> result = evaluationService.evaluateToList(queryBuilder, context);
		List<Integer> patientIds = result.stream().map(x -> (Integer) x[0]).collect(Collectors.toList());
		return patientIds;
	}
}
