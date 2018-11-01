package org.openmrs.module.mksreports.dataset.evaluator;

import org.openmrs.Cohort;
import org.openmrs.annotation.Handler;
import org.openmrs.module.mksreports.dataset.definition.ObsSummaryRowDataSetDefinition;
import org.openmrs.module.reporting.cohort.definition.AgeCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.GenderCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.service.CohortDefinitionService;
import org.openmrs.module.reporting.common.DurationUnit;
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

@Handler(supports = ObsSummaryRowDataSetDefinition.class)
public class ObsSummaryRowDataSetDefinitionEvaluator implements DataSetEvaluator {
	
	@Autowired
	private CohortDefinitionService cohortDefinitionService;
	
	@Autowired
	private EvaluationService evaluationService;
	
	@Override
	public DataSet evaluate(DataSetDefinition dataSetDefinition, EvaluationContext context) throws EvaluationException {
		ObsSummaryRowDataSetDefinition definition = (ObsSummaryRowDataSetDefinition) dataSetDefinition;
		
		GenderCohortDefinition males = new GenderCohortDefinition();
		males.setMaleIncluded(true);
		
		GenderCohortDefinition females = new GenderCohortDefinition();
		males.setFemaleIncluded(true);
		
		AgeCohortDefinition _0To1m = new AgeCohortDefinition();
		_0To1m.setMinAge(0);
		_0To1m.setMinAgeUnit(DurationUnit.DAYS);
		_0To1m.setMaxAge(1);
		_0To1m.setMaxAgeUnit(DurationUnit.MONTHS);
		
		AgeCohortDefinition _1mTo1y = new AgeCohortDefinition();
		_1mTo1y.setMinAge(1);
		_1mTo1y.setMinAgeUnit(DurationUnit.MONTHS);
		_1mTo1y.setMaxAge(11);
		_1mTo1y.setMaxAgeUnit(DurationUnit.MONTHS);
		
		AgeCohortDefinition _1To5y = new AgeCohortDefinition();
		_1To5y.setMinAge(1);
		_1To5y.setMinAgeUnit(DurationUnit.YEARS);
		_1To5y.setMaxAge(4);
		_1To5y.setMaxAgeUnit(DurationUnit.YEARS);
		
		AgeCohortDefinition _5To15y = new AgeCohortDefinition();
		_5To15y.setMinAge(5);
		_5To15y.setMinAgeUnit(DurationUnit.YEARS);
		_5To15y.setMaxAge(14);
		_5To15y.setMaxAgeUnit(DurationUnit.YEARS);
		
		AgeCohortDefinition _15To25y = new AgeCohortDefinition();
		_15To25y.setMinAge(15);
		_15To25y.setMinAgeUnit(DurationUnit.YEARS);
		_15To25y.setMaxAge(24);
		_15To25y.setMaxAgeUnit(DurationUnit.YEARS);
		
		AgeCohortDefinition _25To50y = new AgeCohortDefinition();
		_25To50y.setMinAge(25);
		_25To50y.setMinAgeUnit(DurationUnit.YEARS);
		_25To50y.setMaxAge(49);
		_25To50y.setMaxAgeUnit(DurationUnit.YEARS);
		
		AgeCohortDefinition _50To65y = new AgeCohortDefinition();
		_50To65y.setMinAge(50);
		_50To65y.setMinAgeUnit(DurationUnit.YEARS);
		_50To65y.setMaxAge(64);
		_50To65y.setMaxAgeUnit(DurationUnit.YEARS);
		
		AgeCohortDefinition moreThan65y = new AgeCohortDefinition();
		moreThan65y.setMinAge(65);
		moreThan65y.setMinAgeUnit(DurationUnit.YEARS);
		moreThan65y.setMaxAge(200);
		moreThan65y.setMaxAgeUnit(DurationUnit.YEARS);
		
		SqlQueryBuilder queryBuilder = new SqlQueryBuilder();
		queryBuilder.append(" select o.person_id from obs o  inner join patient p on o.person_id = p.patient_id  "
		        + "where o.voided = false and p.voided = false and concept_id = :questionConceptId "
		        + "and o.obs_datetime >= :onOrAfter  and o.obs_datetime <= :onOrBefore "
		        + "and o.value_coded IN (:diagnosisList)");
		queryBuilder.setParameters(context.getParameterValues());
		
		List<Object[]> personIds = evaluationService.evaluateToList(queryBuilder, context);
		
		DataSetRow row = new DataSetRow();
		
		DataSetColumn col1 = new DataSetColumn();
		col1.setName("0-28days Male");
		col1.setLabel("0-28days Male");
		col1.setDataType(List.class);
		row.addColumnValue(col1,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(_0To1m, males), context), personIds));
		
		DataSetColumn col2 = new DataSetColumn();
		col2.setName("0-28days Female");
		col2.setLabel("0-28days Female");
		col2.setDataType(List.class);
		row.addColumnValue(col2,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(_0To1m, females), context), personIds));
		
		DataSetColumn col3 = new DataSetColumn();
		col3.setName("29days-11months Male");
		col3.setLabel("29days-11months Male");
		col3.setDataType(List.class);
		row.addColumnValue(col3,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(_1mTo1y, males), context), personIds));
		
		DataSetColumn col4 = new DataSetColumn();
		col4.setName("29days-11months Female");
		col4.setName("29days-11months Female");
		col4.setDataType(List.class);
		row.addColumnValue(col4,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(_1mTo1y, females), context), personIds));
		
		DataSetColumn col5 = new DataSetColumn();
		col5.setName("1-4years Male");
		col5.setLabel("1-4years Male");
		col5.setDataType(List.class);
		row.addColumnValue(col5,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(_1To5y, males), context), personIds));
		
		DataSetColumn col6 = new DataSetColumn();
		col6.setName("1-4years Female");
		col6.setLabel("1-4years Female");
		col6.setDataType(List.class);
		row.addColumnValue(col6,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(_1To5y, females), context), personIds));
		
		DataSetColumn col7 = new DataSetColumn();
		col7.setName("5-14years Male");
		col7.setName("5-14years Male");
		col7.setDataType(List.class);
		row.addColumnValue(col7,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(_5To15y, males), context), personIds));
		
		DataSetColumn col8 = new DataSetColumn();
		col8.setName("5-14years Female");
		col8.setLabel("5-14years Female");
		col8.setDataType(List.class);
		row.addColumnValue(col8,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(_5To15y, females), context), personIds));
		
		DataSetColumn col9 = new DataSetColumn();
		col9.setName("15-24years Male");
		col9.setLabel("15-24 years Male");
		col9.setDataType(List.class);
		row.addColumnValue(col9,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(_15To25y, males), context), personIds));
		
		DataSetColumn col10 = new DataSetColumn();
		col10.setName("15-24years Female");
		col10.setLabel("15-24years Female");
		col10.setDataType(List.class);
		row.addColumnValue(col10,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(_15To25y, females), context), personIds));
		
		DataSetColumn col11 = new DataSetColumn();
		col11.setName("25-49years Male");
		col11.setLabel("25-49years Male");
		col11.setDataType(List.class);
		row.addColumnValue(col11,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(_25To50y, males), context), personIds));
		
		DataSetColumn col12 = new DataSetColumn();
		col12.setName("25-49years Female");
		col12.setLabel("25-49years Female");
		col12.setDataType(List.class);
		row.addColumnValue(col12,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(_25To50y, females), context), personIds));
		
		DataSetColumn col13 = new DataSetColumn();
		col13.setName("50-64years Male");
		col13.setLabel("50-64years Male");
		col13.setDataType(List.class);
		row.addColumnValue(col13,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(_50To65y, males), context), personIds));
		
		DataSetColumn col14 = new DataSetColumn();
		col14.setName("50-64years Female");
		col14.setLabel("50-64years Female");
		col14.setDataType(List.class);
		row.addColumnValue(col14,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(_50To65y, females), context), personIds));
		
		DataSetColumn col15 = new DataSetColumn();
		col15.setName(">=65years Male");
		col15.setLabel(">=65years Male");
		col15.setDataType(List.class);
		row.addColumnValue(col15, getPersonsWithDiagnosisInCohort(
		    evaluateCohort(createCohortComposition(moreThan65y, males), context), personIds));
		
		DataSetColumn col16 = new DataSetColumn();
		col16.setName(">=65years Female");
		col16.setLabel(">=65years Female");
		col16.setDataType(List.class);
		row.addColumnValue(col16, getPersonsWithDiagnosisInCohort(
		    evaluateCohort(createCohortComposition(moreThan65y, females), context), personIds));
		
		DataSetColumn col17 = new DataSetColumn();
		col17.setName("Total Males");
		col17.setLabel("Total Males");
		col17.setDataType(List.class);
		row.addColumnValue(col17,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(males), context), personIds));
		
		DataSetColumn col18 = new DataSetColumn();
		col18.setName("Total Females");
		col18.setLabel("Total Females");
		col18.setDataType(List.class);
		row.addColumnValue(col18,
		    getPersonsWithDiagnosisInCohort(evaluateCohort(createCohortComposition(females), context), personIds));
		
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
	
	private CompositionCohortDefinition createCohortComposition(Object... elements) {
		CompositionCohortDefinition compCD = new CompositionCohortDefinition();
		compCD.initializeFromElements(elements);
		return compCD;
	}
}
