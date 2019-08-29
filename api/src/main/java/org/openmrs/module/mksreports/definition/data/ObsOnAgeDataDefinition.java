package org.openmrs.module.mksreports.definition.data;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.person.definition.AgeDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.ObsForVisitDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;

public class ObsOnAgeDataDefinition extends BaseDataDefinition implements VisitDataDefinition {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@ConfigurationProperty(required = true)
	private Mapped<? extends ObsForVisitDataDefinition> obsDefinition;
	
	@ConfigurationProperty(required = true)
	private Mapped<? extends AgeDataDefinition> ageDefinition;
	
	/**
	 * Default Constructor
	 */
	public ObsOnAgeDataDefinition() {
		super();
	}
	
	@Override
	public Class<?> getDataType() {
		return Double.class;
	}
	
	/**
	 * Getters and setters
	 */
	public Mapped<? extends ObsForVisitDataDefinition> getObsDefinition() {
		return obsDefinition;
	}
	
	public void setObsDefinition(Mapped<? extends ObsForVisitDataDefinition> obsDef) {
		this.obsDefinition = obsDef;
	}
	
	public Mapped<? extends AgeDataDefinition> getAgeDefinition() {
		return ageDefinition;
	}
	
	public void setAgeDefinition(Mapped<? extends AgeDataDefinition> ageDef) {
		this.ageDefinition = ageDef;
	}
	
}
