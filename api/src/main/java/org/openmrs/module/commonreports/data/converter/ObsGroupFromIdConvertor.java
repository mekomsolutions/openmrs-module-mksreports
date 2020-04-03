package org.openmrs.module.commonreports.data.converter;

import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.converter.DataConverter;

public class ObsGroupFromIdConvertor implements DataConverter {
	
	@Override
	public Object convert(Object original) {
		Obs o = Context.getObsService().getObs((Integer) original);
		if (o != null && o.getObsGroup() != null) {
			return o.getObsGroup().getId();
		}
		
		return null;
	}
	
	@Override
	public Class<?> getInputDataType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Class<?> getDataType() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
