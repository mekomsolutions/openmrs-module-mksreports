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

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converter to return a formated order
 */
public class RoundNumber implements DataConverter {
	
	private Log log = LogFactory.getLog(this.getClass());
	
	private Integer decimals;
	
	public Integer getDecimals() {
		return decimals;
	}
	
	public void setDecimals(Integer decimals) {
		this.decimals = decimals;
	}
	
	public RoundNumber(Integer decimals) {
		this.decimals = decimals;
	}
	
	public RoundNumber() {
		if (this.decimals == null) {
			// Default 'decimals' value to 2
			this.decimals = new Integer(2);
		}
	}
	
	/**
	 * @return returns a value rounded to the number of 'decimals' provided
	 */
	@Override
	public Object convert(Object original) {
		
		Double result = null;
		Double number = null;
		if (original != null) {
			if (original instanceof Double) {
				number = (Double) original;
			} else if (original instanceof String) {
				number = Double.parseDouble((String) original);
			} else {
				log.info("Input passed to the Converter is not of any supported type (String or Double). Returning as is.");
				return original;
			}
			
			BigDecimal rounded = new BigDecimal(number).setScale(decimals, RoundingMode.HALF_UP);
			result = rounded.doubleValue();
			return result;
		}
		
		return original;
	}
	
	@Override
	public Class<?> getInputDataType() {
		return Object.class;
	}
	
	@Override
	public Class<?> getDataType() {
		return Object.class;
	}
	
}
