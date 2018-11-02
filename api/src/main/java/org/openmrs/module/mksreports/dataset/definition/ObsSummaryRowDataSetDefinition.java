package org.openmrs.module.mksreports.dataset.definition;

import org.openmrs.module.reporting.cohort.definition.CohortDefinition;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

public class ObsSummaryRowDataSetDefinition extends BaseDataSetDefinition {
	
	/**
	 * Column Definitions where the keys are the column names and values are CohortDefinitions which are
	 * used to evaluate the value of the column
	 */
	@ConfigurationProperty
	private Map<String, CohortDefinition> cohortColumnDefinitions = new HashMap<String, CohortDefinition>();
	
	public void addColumn(String name, CohortDefinition cohortDefinition) {
		cohortColumnDefinitions.put(name, cohortDefinition);
	}
	
	public Map<String, CohortDefinition> getCohortColumnDefinitions() {
		return cohortColumnDefinitions;
	}
	
	public void setCohortColumnDefinitions(Map<String, CohortDefinition> columns) {
		this.cohortColumnDefinitions = columns;
	}
}
