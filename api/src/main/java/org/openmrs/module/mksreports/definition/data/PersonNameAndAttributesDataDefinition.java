package org.openmrs.module.mksreports.definition.data;

import java.util.List;

import org.openmrs.module.reporting.data.BaseDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.definition.configuration.ConfigurationProperty;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;

public class PersonNameAndAttributesDataDefinition extends BaseDataDefinition implements PersonDataDefinition {
	
	/**
	 * Person data definition that will return a person's name appended with optional person
	 * attributes values
	 */
	private static final long serialVersionUID = 1L;
	
	@ConfigurationProperty(required = false)
	private Mapped<? extends PreferredNameDataDefinition> preferredNameDefinition;
	
	@ConfigurationProperty(required = false)
	private List<Mapped<? extends PersonAttributeDataDefinition>> personAttributeDefinitions;
	
	// ***** CONSTRUCTORS *****
	
	public PersonNameAndAttributesDataDefinition() {
	}
	
	public PersonNameAndAttributesDataDefinition(Mapped<? extends PreferredNameDataDefinition> preferredNameDataDef) {
		this.preferredNameDefinition = preferredNameDataDef;
	}
	
	/**
	 * Getters and setters
	 */
	
	@Override
	public Class<?> getDataType() {
		return String.class;
	}
	
	public List<Mapped<? extends PersonAttributeDataDefinition>> getPersonAttributeDefinitions() {
		return personAttributeDefinitions;
	}
	
	public void setPersonAttributeDefinitions(
	        List<Mapped<? extends PersonAttributeDataDefinition>> personAttributeDefinitions) {
		this.personAttributeDefinitions = personAttributeDefinitions;
	}
	
	public Mapped<? extends PreferredNameDataDefinition> getPreferredNameDefinition() {
		return preferredNameDefinition;
	}
	
	public void setPreferredNameDefinition(Mapped<? extends PreferredNameDataDefinition> preferredNameDefinition) {
		this.preferredNameDefinition = preferredNameDefinition;
	}
	
}
