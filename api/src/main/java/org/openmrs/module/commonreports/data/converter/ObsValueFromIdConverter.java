package org.openmrs.module.commonreports.data.converter;

import org.openmrs.ConceptDatatype;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.common.ObjectUtil;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converts an Obs to it's value using its ID
 */
public class ObsValueFromIdConverter implements DataConverter {
	
	public ObsValueFromIdConverter() {
	}
	
	@Override
	public Object convert(Object original) {
		Obs o = Context.getObsService().getObs((Integer) original);
		if (o == null) {
			return null;
		}
		
		if (o.hasGroupMembers()) {
			return "";
		}
		
		if (o.getValueDrug() != null) {
			return ObjectUtil.format(o.getValueDrug());
		}
		
		ConceptDatatype dt = o.getConcept().getDatatype();
		
		if (dt.isBoolean()) {
			return o.getValueBoolean();
		} else if (dt.isCoded()) {
			return ObjectUtil.format(o.getValueCoded());
		} else if (dt.isComplex()) {
			return o.getValueComplex();
		} else if (dt.isDateTime()) {
			return o.getValueDatetime();
		} else if (dt.isDate()) {
			return o.getValueDate();
		} else if (dt.isNumeric()) {
			return o.getValueNumeric();
		} else if (dt.isText()) {
			return o.getValueText();
		} else if (dt.isTime()) {
			return o.getValueTime();
		}
		
		return o.getValueAsString(Context.getLocale());
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
