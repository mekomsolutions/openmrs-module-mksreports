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

import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converter to evaluate a passed gender against a configured list of matching genders
 */
public class GenderConverter implements DataConverter {
	
	// ***** PROPERTIES *****
	
	private List<String> matchingGenders;
	
	private String trueLabel;
	
	private String falseLabel;
	
	public List<String> getMatchingGenders() {
		return matchingGenders;
	}
	
	public void setMatchingGenders(List<String> matchingGenders) {
		this.matchingGenders = matchingGenders;
	}
	
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
	public GenderConverter(List<String> matchingGenders, String trueLabel, String falseLabel) {
		this.matchingGenders = matchingGenders;
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
		String gender = (String) original;
		for (String genderProperty : getMatchingGenders()) {
			if (genderProperty.equals(gender)) {
				return getTrueLabel();
			}
		}
		return getFalseLabel();
	}
	
	@Override
	public Class<?> getInputDataType() {
		return String.class;
	}
	
	@Override
	public Class<?> getDataType() {
		return String.class;
	}
}
