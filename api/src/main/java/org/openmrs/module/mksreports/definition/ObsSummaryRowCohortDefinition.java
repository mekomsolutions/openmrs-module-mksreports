package org.openmrs.module.mksreports.definition;

import org.openmrs.Concept;
import org.openmrs.module.reporting.cohort.definition.BaseObsCohortDefinition;
import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.common.SetComparator;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

import java.util.ArrayList;
import java.util.List;

@Caching(strategy = ConfigurationPropertyCachingStrategy.class)
@Localized("mksreports.ObsSummaryRowCohortDefinition")
public class ObsSummaryRowCohortDefinition extends BaseObsCohortDefinition {
	
	private long obsCount;
	
	public static final long serialVersionUID = 1L;
	
	@ConfigurationProperty(group = "constraint")
	SetComparator operator;
	
	@ConfigurationProperty(group = "constraint")
	List<Concept> valueList;
	
	public ObsSummaryRowCohortDefinition() {
	}
	
	public SetComparator getOperator() {
		return this.operator;
	}
	
	public void setOperator(SetComparator operator) {
		this.operator = operator;
	}
	
	public List<Concept> getValueList() {
		return this.valueList;
	}
	
	public void setValueList(List<Concept> valueList) {
		this.valueList = valueList;
	}
	
	public void addValue(Concept concept) {
		if (this.valueList == null) {
			this.valueList = new ArrayList();
		}
		
		this.valueList.add(concept);
	}
	
	public long getObsCount() {
		return obsCount;
	}
	
	public void setObsCount(long obsCount) {
		this.obsCount = obsCount;
	}
}
