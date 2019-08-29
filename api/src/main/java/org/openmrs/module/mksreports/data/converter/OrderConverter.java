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

import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converter to return a formated order
 */
public class OrderConverter implements DataConverter {
	
	/**
	 * @return returns a formatted Order string based on a provided order UUID.
	 */
	@Override
	public Object convert(Object original) {
		
		String formattedOrder = "";
		if (original != null) {
			Order order = (Order) original;
			if (order instanceof DrugOrder) {
				DrugOrder drugOrder = (DrugOrder) order;
				String drugName = (drugOrder.isNonCodedDrug() ? drugOrder.getDrugNonCoded()
				        : (drugOrder.getDrug() != null ? drugOrder.getDrug().getName() : "[no drug]"));
				formattedOrder = drugName;
			} else {
				formattedOrder = (order.getConcept().getName(Context.getLocale()).getName());
			}
			return formattedOrder;
		}
		return null;
	}
	
	@Override
	public Class<?> getInputDataType() {
		return List.class;
	}
	
	@Override
	public Class<?> getDataType() {
		return List.class;
	}
	
}
