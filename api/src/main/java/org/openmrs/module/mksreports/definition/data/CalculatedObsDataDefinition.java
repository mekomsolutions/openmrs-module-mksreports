package org.openmrs.module.mksreports.definition.data;

import org.openmrs.module.reporting.common.TimeQualifier;
import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.ObsForVisitDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;

public class CalculatedObsDataDefinition extends BaseDataDefinition implements VisitDataDefinition {
	
	/**
	 * Obs visit data definition that will evaluate two operands ({@link Obs}) against an
	 * {@link Operator}
	 */
	private static final long serialVersionUID = 1L;
	
	public enum Operator {
		ADDITION, SUBSTRACTION, MULTIPLICATION, DIVISION
	}
	
	@ConfigurationProperty(required = true)
	private Mapped<? extends ObsForVisitDataDefinition> obsDefinition1;
	
	@ConfigurationProperty(required = true)
	private Mapped<? extends ObsForVisitDataDefinition> obsDefinition2;
	
	@ConfigurationProperty(required = true)
	private Operator operator;
	
	// ***** CONSTRUCTORS *****
	
	public CalculatedObsDataDefinition() {
	}
	
	public CalculatedObsDataDefinition(Operator operator) {
		this.operator = operator;
	}
	
	/**
	 * Getters and setters
	 */
	
	public Operator getOperator() {
		return operator;
	}
	
	public Mapped<? extends ObsForVisitDataDefinition> getObsDefinition1() {
		return obsDefinition1;
	}
	
	public void setObsDefinition1(Mapped<? extends ObsForVisitDataDefinition> obsDefinition1) {
		this.obsDefinition1 = obsDefinition1;
		// default to using the most recent observation only (TimeQualifier.LAST)
		if (obsDefinition1.getParameterizable().getWhich() == null) {
			obsDefinition1.getParameterizable().setWhich(TimeQualifier.LAST);
		}
	}
	
	public Mapped<? extends ObsForVisitDataDefinition> getObsDefinition2() {
		return obsDefinition2;
	}
	
	public void setObsDefinition2(Mapped<? extends ObsForVisitDataDefinition> obsDefinition2) {
		this.obsDefinition2 = obsDefinition2;
		// default to using the most recent observation only (TimeQualifier.LAST)
		if (obsDefinition2.getParameterizable().getWhich() == null) {
			obsDefinition2.getParameterizable().setWhich(TimeQualifier.LAST);
		}
		
	}
	
	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	
	@Override
	public Class<?> getDataType() {
		return Double.class;
	}
	
}
