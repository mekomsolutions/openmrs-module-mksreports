package org.openmrs.module.commonreports.data.converter;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converts a visit id to visit location name
 */
public class VisitLocationFromIdConverter implements DataConverter {
	
	public VisitLocationFromIdConverter() {
	}
	
	@Override
	public Object convert(Object original) {
		if (original != null) {
			VisitService vs = Context.getVisitService();
			Visit v = vs.getVisit((Integer) original);
			// A visit can exist without location
			return (v.getLocation() == null) ? StringUtils.EMPTY : v.getLocation().getName();
		} else {
			return StringUtils.EMPTY;
		}
	}
	
	@Override
	public Class<?> getInputDataType() {
		return Integer.class;
	}
	
	@Override
	public Class<?> getDataType() {
		return Object.class;
	}
}
