package org.openmrs.module.mksreports.definition.data.evaluator;

import org.openmrs.Cohort;
import org.openmrs.Concept;
import org.openmrs.annotation.Handler;
import org.openmrs.api.context.Context;
import org.openmrs.module.mksreports.ObsSummaryEvaluatedCohort;
import org.openmrs.module.mksreports.definition.ObsSummaryRowCohortDefinition;
import org.openmrs.module.reporting.cohort.EvaluatedCohort;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.cohort.definition.evaluator.BaseObsCohortDefinitionEvaluator;
import org.openmrs.module.reporting.common.DateUtil;
import org.openmrs.module.reporting.common.RangeComparator;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.module.reporting.evaluation.querybuilder.SqlQueryBuilder;
import org.openmrs.module.reporting.evaluation.service.EvaluationService;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Handler(supports = ObsSummaryRowCohortDefinition.class, order = 50)
public class ObsSummaryRowCohortDefinitionEvaluator extends BaseObsCohortDefinitionEvaluator {
	
	@Override
	public EvaluatedCohort evaluate(CohortDefinition cohortDefinition, EvaluationContext context)
	        throws EvaluationException {
		ObsSummaryRowCohortDefinition cd = (ObsSummaryRowCohortDefinition) cohortDefinition;
		long obsCount = getObsCount(cd, null, null, null, null, cd.getOperator(), cd.getValueList(), context);
		Cohort c = getPatientsHavingObs(cd, (RangeComparator) null, (Object) null, (RangeComparator) null, (Object) null,
		    cd.getOperator(), cd.getValueList(), context);
		return new ObsSummaryEvaluatedCohort(c, cohortDefinition, context, obsCount);
	}
	
	private long getObsCount(BaseObsCohortDefinition cd, RangeComparator operator1, Object value1, RangeComparator operator2,
	        Object value2, SetComparator setOperator, List<? extends Object> valueList, EvaluationContext context) {
		if (cd.getGroupingConcept() != null) {
			throw new RuntimeException("grouping concept not yet implemented");
		} else {
			boolean joinOnEncounter = cd.getEncounterTypeIds() != null;
			String dateAndLocationSql = "";
			String dateAndLocationSqlForSubquery = "";
			if (cd.getOnOrAfter() != null) {
				dateAndLocationSql = dateAndLocationSql + " and o.obs_datetime >= :onOrAfter ";
				dateAndLocationSqlForSubquery = dateAndLocationSqlForSubquery + " and obs.obs_datetime >= :onOrAfter ";
			}
			
			if (cd.getOnOrBefore() != null) {
				dateAndLocationSql = dateAndLocationSql + " and o.obs_datetime <= :onOrBefore ";
				dateAndLocationSqlForSubquery = dateAndLocationSqlForSubquery + " and obs.obs_datetime <= :onOrBefore ";
			}
			
			if (cd.getLocationIds() != null) {
				dateAndLocationSql = dateAndLocationSql + " and o.location_id in (:locationIds) ";
				dateAndLocationSqlForSubquery = dateAndLocationSqlForSubquery + " and obs.location_id in (:locationIds) ";
			}
			
			if (cd.getEncounterTypeIds() != null) {
				dateAndLocationSql = dateAndLocationSql + " and e.encounter_type in (:encounterTypeIds) ";
				dateAndLocationSqlForSubquery = dateAndLocationSqlForSubquery
				        + " and encounter.encounter_type in (:encounterTypeIds) ";
			}
			
			BaseObsCohortDefinition.TimeModifier tm = cd.getTimeModifier();
			if (tm == null) {
				tm = BaseObsCohortDefinition.TimeModifier.ANY;
			}
			
			boolean doSqlAggregation = tm == BaseObsCohortDefinition.TimeModifier.MIN
			        || tm == BaseObsCohortDefinition.TimeModifier.MAX || tm == BaseObsCohortDefinition.TimeModifier.AVG;
			boolean doInvert = tm == BaseObsCohortDefinition.TimeModifier.NO;
			String valueSql = null;
			List<String> valueClauses = new ArrayList();
			List<Object> valueListForQuery = null;
			if (value1 == null && value2 == null) {
				if (valueList != null && valueList.size() > 0) {
					valueListForQuery = new ArrayList();
					Iterator i$;
					Object o;
					if (valueList.get(0) instanceof String) {
						valueSql = " o.value_text ";
						i$ = valueList.iterator();
						
						while (i$.hasNext()) {
							o = i$.next();
							valueListForQuery.add(o);
						}
					} else {
						valueSql = " o.value_coded ";
						i$ = valueList.iterator();
						
						while (i$.hasNext()) {
							o = i$.next();
							if (o instanceof Concept) {
								valueListForQuery.add(((Concept) o).getConceptId());
							} else {
								if (!(o instanceof Number)) {
									throw new IllegalArgumentException(
									        "Don't know how to handle " + o.getClass() + " in valueList");
								}
								
								valueListForQuery.add(((Number) o).intValue());
							}
						}
					}
				}
			} else {
				valueSql = value1 != null && value1 instanceof Number ? " o.value_numeric " : " o.value_datetime ";
			}
			
			if (doSqlAggregation) {
				valueSql = " " + tm.toString() + "(" + valueSql + ") ";
			}
			
			if (value1 == null && value2 == null) {
				if (valueList != null && valueList.size() > 0) {
					valueClauses.add(valueSql + setOperator.getSqlRepresentation() + " (:valueList) ");
				}
			} else {
				if (value1 != null) {
					valueClauses.add(valueSql + operator1.getSqlRepresentation() + " :value1 ");
				}
				
				if (value2 != null) {
					valueClauses.add(valueSql + operator2.getSqlRepresentation() + " :value2 ");
				}
			}
			
			StringBuilder sql = new StringBuilder();
			sql.append(" select count(o.obs_id) from obs o ");
			sql.append(" inner join patient p on o.person_id = p.patient_id ");
			if (joinOnEncounter) {
				sql.append(" inner join encounter e on o.encounter_id = e.encounter_id ");
			}
			
			if (tm != BaseObsCohortDefinition.TimeModifier.ANY && tm != BaseObsCohortDefinition.TimeModifier.NO) {
				if (tm != BaseObsCohortDefinition.TimeModifier.FIRST && tm != BaseObsCohortDefinition.TimeModifier.LAST) {
					if (!doSqlAggregation) {
						throw new IllegalArgumentException("TimeModifier '" + tm + "' not recognized");
					}
					
					sql.append(" where o.voided = false and p.voided = false and concept_id = :questionConceptId "
					        + dateAndLocationSql);
					sql.append(" group by o.person_id ");
				} else {
					boolean isFirst = tm == BaseObsCohortDefinition.TimeModifier.FIRST;
					sql.append(" inner join ( ");
					sql.append("    select person_id, " + (isFirst ? "MIN" : "MAX") + "(obs_datetime) as odt ");
					sql.append("    from obs ");
					if (joinOnEncounter) {
						sql.append(" inner join encounter on obs.encounter_id = encounter.encounter_id ");
					}
					
					sql.append("             where obs.voided = false and obs.concept_id = :questionConceptId "
					        + dateAndLocationSqlForSubquery + " group by person_id ");
					sql.append(" ) subq on o.person_id = subq.person_id and o.obs_datetime = subq.odt ");
					sql.append(" where o.voided = false and p.voided = false and o.concept_id = :questionConceptId ");
					sql.append(dateAndLocationSql);
				}
			} else {
				sql.append(" where o.voided = false and p.voided = false ");
				if (cd.getQuestion() != null) {
					sql.append(" and concept_id = :questionConceptId ");
				}
				
				sql.append(dateAndLocationSql);
			}
			
			if (valueClauses.size() > 0) {
				sql.append(doSqlAggregation ? " having " : " and ");
				Iterator i = valueClauses.iterator();
				
				while (i.hasNext()) {
					sql.append((String) i.next());
					if (i.hasNext()) {
						sql.append(" and ");
					}
				}
			}
			
			log.debug("sql: " + sql);
			SqlQueryBuilder qb = new SqlQueryBuilder();
			qb.append(sql.toString());
			if (cd.getQuestion() != null) {
				qb.addParameter("questionConceptId", cd.getQuestion());
			}
			
			if (value1 != null) {
				qb.addParameter("value1", value1);
			}
			
			if (value2 != null) {
				qb.addParameter("value2", value2);
			}
			
			if (valueListForQuery != null) {
				qb.addParameter("valueList", valueListForQuery);
			}
			
			if (cd.getOnOrAfter() != null) {
				qb.addParameter("onOrAfter", cd.getOnOrAfter());
			}
			
			if (cd.getOnOrBefore() != null) {
				qb.addParameter("onOrBefore", DateUtil.getEndOfDayIfTimeExcluded(cd.getOnOrBefore()));
			}
			
			if (cd.getLocationIds() != null) {
				qb.addParameter("locationIds", cd.getLocationIds());
			}
			
			if (cd.getEncounterTypeIds() != null) {
				qb.addParameter("encounterTypeIds", cd.getEncounterTypeIds());
			}
			EvaluationService evaluationService = Context.getService(EvaluationService.class);
			Long result = evaluationService.evaluateToObject(qb, Long.class, context);
			return result;
		}
	}
	
}
