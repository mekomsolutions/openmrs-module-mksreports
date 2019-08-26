package org.openmrs.module.mksreports.patienthistory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openmrs.module.reporting.report.ReportDesign;

public class PatientSummaryTemplate {

	protected final Log log = LogFactory.getLog(this.getClass());

	// ***** PROPERTIES *****

	private ReportDesign reportDesign;

	// ***** CONSTRUCTORS *****

	/**
	 * Default constructor
	 */
	public PatientSummaryTemplate() {
	}

	/**
	 * Full Constructor
	 */
	public PatientSummaryTemplate(ReportDesign reportDesign) {
		this.reportDesign = reportDesign;
	}

	// ***** METHODS *****

	/**
	 * @return the contentType for this PatientSummary
	 */
	public String getContentType() {
		return "text/html";
	}

	/**
	 * @return the filename for this PatientSummary for use when exporting
	 */
	public String getExportFilename() {
		return reportDesign.getName() + ".html";
	}

	/**
	 * @return the primary key id
	 */
	public Integer getId() {
		return getReportDesign() != null ? getReportDesign().getId() : null;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return getReportDesign() != null ? getReportDesign().getUuid() : null;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return getReportDesign() != null ? getReportDesign().getName() : null;
	}

	/**
	 * @return the associated patient summary report definition
	 */
	public PatientSummaryReportDefinition getReportDefinition() {
		return getReportDesign() != null ? (PatientSummaryReportDefinition) getReportDesign().getReportDefinition()
				: null;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof PatientSummaryTemplate) {
			PatientSummaryTemplate that = (PatientSummaryTemplate) o;
			if (this.getReportDesign() != null && that.getReportDesign() != null) {
				return this.getReportDesign().equals(that.getReportDesign());
			}
		}
		return super.equals(o);
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		if (this.getReportDesign() != null) {
			return this.getReportDesign().hashCode();
		}
		return super.hashCode();
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		if (getReportDesign() != null) {
			if (getReportDesign().getReportDefinition() != null) {
				return getReportDesign().getReportDefinition().getName() + " (" + getReportDesign().getName() + ")";
			}
			return getReportDesign().getName();
		}
		return super.toString();
	}

	// ***** PROPERTY ACCESS *****

	/**
	 * @return the reportDesign
	 */
	public ReportDesign getReportDesign() {
		return reportDesign;
	}

	/**
	 * @param reportDesign the reportDesign to set
	 */
	public void setReportDesign(ReportDesign reportDesign) {
		this.reportDesign = reportDesign;
	}

}
