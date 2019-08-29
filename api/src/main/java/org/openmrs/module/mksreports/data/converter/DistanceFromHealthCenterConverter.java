/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.mksreports.data.converter;

import java.util.List;

import org.openmrs.Concept;
import org.openmrs.PersonAttribute;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converter to evaluate a passed distance from Health center value against a configured list of
 * matching distances
 */
public class DistanceFromHealthCenterConverter implements DataConverter {
	
	// ***** PROPERTIES *****
	
	private List<Concept> matchingDistances;
	
	public List<Concept> getMatchingDistances() {
		return matchingDistances;
	}
	
	public void setMatchingDistances(List<Concept> matchingDistances) {
		this.matchingDistances = matchingDistances;
	}
	
	private String trueLabel;
	
	private String falseLabel;
	
	public String getTrueLabel() {
		return trueLabel;
	}
	
	public void setTrueLabel(String trueLabel) {
		this.trueLabel = trueLabel;
	}
	
	public String getFalseLabel() {
		return falseLabel;
	}
	
	public void setFalseLabel(String falseLabel) {
		this.falseLabel = falseLabel;
	}
	
	// ***** CONSTRUCTORS *****
	public DistanceFromHealthCenterConverter(List<Concept> matchingDistances, String trueLabel, String falseLabel) {
		this.matchingDistances = matchingDistances;
		this.trueLabel = trueLabel;
		this.falseLabel = falseLabel;
	}
	
	// ***** INSTANCE METHODS *****
	
	/**
	 * @see DataConverter#converter(Object)
	 * @should return trueLabel if the gender matches one of the genders configured in the
	 *         matchingGenders list
	 * @should return falseLabel if the gender does not match one of the genders configured in the
	 *         matchingGenders list
	 */
	public Object convert(Object original) {
		PersonAttribute distance = (PersonAttribute) original;
		
		for (Concept distanceProperty : getMatchingDistances()) {
			if (distanceProperty.equals((Concept) distance.getHydratedObject())) {
				return getTrueLabel();
			}
		}
		return getFalseLabel();
	}
	
	@Override
	public Class<?> getInputDataType() {
		return Concept.class;
	}
	
	@Override
	public Class<?> getDataType() {
		return String.class;
	}
}
