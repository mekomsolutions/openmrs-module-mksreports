package org.openmrs.module.mksreports.patientsummary.patienthistory.exception;

import org.openmrs.api.APIException;

public class PatientSummaryException extends APIException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor that takes in a message
	 */
	public PatientSummaryException(String message) {
		super(message);
	}

	/**
	 * Constructor that takes in a message and a cause
	 */
	public PatientSummaryException(String message, Throwable cause) {
		super(message, cause);
	}

}
