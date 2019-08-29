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

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Obs;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converter to evaluate a passed Boolean obs and return the configured true/false labels
 */
public class ObsBooleanConverter implements DataConverter {
	
	// ***** PROPERTIES *****
	
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
	public ObsBooleanConverter() {
		
	}
	
	public ObsBooleanConverter(String trueLabel, String falseLabel) {
		this.trueLabel = trueLabel;
		this.falseLabel = falseLabel;
	}
	
	// ***** INSTANCE METHODS *****
	
	/**
	 * @see DataConverter#converter(Object)
	 */
	public Object convert(Object original) {
		if (original == null) {
			return null;
		}
		if (original instanceof Obs) {
			Obs o = (Obs) original;
			return getBooleanText(o);
		} else if (original instanceof List) {
			List<Object> obsValues = new ArrayList<Object>();
			for (Obs o : ((List<Obs>) original)) {
				obsValues.add(getBooleanText(o));
			}
			return obsValues;
		}
		return original;
	}
	
	private String getBooleanText(Obs obs) {
		if (obs.getValueBoolean()) {
			return trueLabel;
		} else {
			return falseLabel;
		}
	}
	
	@Override
	public Class<?> getInputDataType() {
		return Obs.class;
	}
	
	@Override
	public Class<?> getDataType() {
		return String.class;
	}
}
