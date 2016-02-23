package org.openmrs.module.mksreports.data.converter;

import org.openmrs.Concept;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.reporting.data.converter.DataConverter;

/**
 * Converts an Obs to it's value
 */
public class ConceptNameConverter implements DataConverter {

    public ConceptNameConverter() { }

    @Override
    public Object convert(Object original) {
    	ConceptService cs  = Context.getConceptService();
		Concept c = cs.getConcept((Integer) original);	    	
		return c.getName(Context.getLocale());
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
