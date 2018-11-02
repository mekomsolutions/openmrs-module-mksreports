package org.openmrs.module.mksreports.dataset.definition;

import org.openmrs.module.reporting.cohort.definition.CompositionCohortDefinition;
import org.openmrs.module.reporting.dataset.definition.BaseDataSetDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;

import java.util.HashMap;
import java.util.Map;

public class ObsSummaryRowDataSetDefinition extends BaseDataSetDefinition {
	
	@ConfigurationProperty
	private Map<String, CompositionCohortDefinition> columns = new HashMap<String, CompositionCohortDefinition>();
	
	public void addColumn(String name, CompositionCohortDefinition cohortDefinition) {
		columns.put(name, cohortDefinition);
	}
	
	public Map<String, CompositionCohortDefinition> getColumns() {
		return columns;
	}
	
	public void setColumns(Map<String, CompositionCohortDefinition> columns) {
		this.columns = columns;
	}
}
