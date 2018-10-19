package org.openmrs.module.mksreports.definition;

import org.openmrs.module.reporting.cohort.definition.CodedObsCohortDefinition;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.definition.configuration.ConfigurationPropertyCachingStrategy;
import org.openmrs.module.reporting.evaluation.caching.Caching;

@Caching(strategy = ConfigurationPropertyCachingStrategy.class)
@Localized("mksreports.ObsSummaryRowCohortDefinition")
public class ObsSummaryRowCohortDefinition extends CodedObsCohortDefinition {
	
}
