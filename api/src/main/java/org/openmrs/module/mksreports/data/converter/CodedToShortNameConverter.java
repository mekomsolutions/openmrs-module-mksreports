package org.openmrs.module.mksreports.data.converter;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converts an Obs to its value
 */
public class CodedToShortNameConverter implements DataConverter {
	
	public CodedToShortNameConverter() {
	}
	
	@Override
	public Object convert(Object original) {
		
		if (original == null) {
			return null;
		}
		if (original instanceof Obs) {
			Obs o = (Obs) original;
			return getShortName(o);
		} else if (original instanceof List) {
			List<Object> obsValues = new ArrayList<Object>();
			for (Obs o : ((List<Obs>) original)) {
				obsValues.add(getShortName(o));
			}
			return obsValues;
		}
		return original;
	}
	
	private Object getShortName(Obs o) {
		if (o.getValueCoded() != null) {
			return o.getValueCoded().getShortNameInLocale(Context.getLocale());
		}
		return o.getValueAsString(Context.getLocale());
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
