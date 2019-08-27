package org.openmrs.module.mksreports.patientsummary.patienthistory;

import org.openmrs.module.mksreports.patientsummary.patienthistory.exception.PatientSummaryException;
import org.openmrs.module.reporting.common.Localized;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.PatientDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.report.definition.ReportDefinition;

@Localized("mksreports.patientsummary.PatientSummaryReportDefinition")
public class PatientSummaryReportDefinition extends ReportDefinition {

	private static final long serialVersionUID = 1L;
	public static final String DEFAULT_DATASET_KEY = "patient";

	/**
	 * Default Constructor
	 */
	public PatientSummaryReportDefinition() {
		setPatientDataSetDefinition(new PatientDataSetDefinition());
	}

	/**
	 * Overrides the default behavior, such that only a single
	 * PatientDataSetDefinition is supported
	 */
	@Override
	public void addDataSetDefinition(String key, Mapped<? extends DataSetDefinition> definition) {
		throw new PatientSummaryException(
				"The PatientSummaryReportDefinition does not support multiple DataSetDefinitions");
	}

	/**
	 * @return the underlying PatientDataSetDefinition
	 */
	public PatientDataSetDefinition getPatientDataSetDefinition() {
		return (PatientDataSetDefinition) getDataSetDefinitions().get(DEFAULT_DATASET_KEY).getParameterizable();

	}

	/**
	 * @return the underlying PatientDataSetDefinition
	 */
	public void setPatientDataSetDefinition(PatientDataSetDefinition pdsd) {
		getDataSetDefinitions().put(DEFAULT_DATASET_KEY, new Mapped<DataSetDefinition>(pdsd, null));
	}

}
